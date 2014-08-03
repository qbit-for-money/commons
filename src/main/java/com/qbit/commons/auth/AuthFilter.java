package com.qbit.commons.auth;

import com.qbit.commons.crypto.util.EncryptionUtil;
import com.qbit.commons.env.CommonsEnv;
import com.qbit.commons.user.UserDAO;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

/**
 * @author Alexander_Sergeev
 */
@Provider
public class AuthFilter implements ContainerRequestFilter {

	public static final String USER_ID_KEY = "user_id";

	@Inject
	private CommonsEnv env;
	@Inject
	private UserDAO userDAO;

	@Context
	private HttpServletRequest httpRequest;

	public static String getUserId(HttpServletRequest request) {
		return (String) request.getSession().getAttribute(USER_ID_KEY);
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		URI baseURI = requestContext.getUriInfo().getBaseUri();
		String userId = getUserId(httpRequest);
		boolean notAuthorized = ((userId == null) || (userId.isEmpty()));
		if (!isAuthRequest(requestContext.getUriInfo()) && notAuthorized) {
			String accessToken = requestContext.getHeaderString("Access-Token");
			if ((accessToken != null) && !accessToken.isEmpty()) {
				if (!processAccessToken(accessToken)) {
					requestContext.abortWith(Response.serverError()
							.entity("Invalid Access-Tokern header").build());
				}
			} else {
				requestContext.abortWith(Response.seeOther(baseURI).build());
			}
		} else {
			boolean isAdmin = env.getAdminMail().equals(EncryptionUtil.getMD5(userId));
			if (isRequestToAdminPage(requestContext.getUriInfo()) && !isAdmin) {
				requestContext.abortWith(Response.seeOther(baseURI).build());
			}
		}
	}

	private boolean isAuthRequest(UriInfo uriInfo) {
		String path = ((uriInfo.getPath() != null) ? uriInfo.getPath() : "");
		if (path.isEmpty()) {
			return true;
		}
		Map<String, String> authPathMap = env.getAuthPath();
		for (String authPathPrefix : authPathMap.values()) {
			if (path.startsWith(authPathPrefix)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRequestToAdminPage(UriInfo uriInfo) {
		String path = ((uriInfo.getPath() != null) ? uriInfo.getPath() : "");
		if (path.startsWith("/admin")) {
			return true;
		}
		Map<String, String> adminPathMap = env.getAdminPath();
		for (Map.Entry<String, String> entry : adminPathMap.entrySet()) {
			if ((entry.getKey().startsWith("filter.path.admin.starts")
					&& path.startsWith(entry.getValue()))
					|| (entry.getKey().startsWith("filter.path.admin.ends")
					&& path.endsWith(entry.getValue()))) {
				return true;
			}
		}
		return false;
	}

	private boolean processAccessToken(String accessToken) {
		if ((accessToken == null) || accessToken.isEmpty()) {
			return false;
		}
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		try {
			OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(env.getGoogleUserInfoUrl())
					.setAccessToken(accessToken)
					.buildQueryMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET,
					OAuthResourceResponse.class);
			String userId = GoogleResource.getGoogleProfileEmail(resourceResponse);
			if (userId != null) {
				userDAO.create(userId);
				httpRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, userId);
				return true;
			} else {
				return false;
			}
		} catch (OAuthSystemException | OAuthProblemException | IOException e) {
			throw new WebApplicationException(e);
		}
	}
}
