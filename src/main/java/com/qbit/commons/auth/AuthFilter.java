package com.qbit.commons.auth;

import com.qbit.commons.env.CommonsEnv;
import com.qbit.commons.user.UserDAO;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
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
public class AuthFilter implements ContainerResponseFilter {

	public static final String USER_ID_KEY = "user_id";

	@Inject
	private CommonsEnv env;
	@Inject
	private UserDAO userDAO;

	@Context
	private HttpServletRequest httpRequest;
	@Context
	private HttpServletResponse httpResponse;

	public static String getUserId(HttpServletRequest request) {
		return (String) request.getSession().getAttribute(USER_ID_KEY);
	}

	private boolean processNotAuthorizeUser(HttpServletRequest httpRequest) {
		String accessToken = httpRequest.getHeader("Access-Token");
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		try {
			OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(env.getGoogleUserInfoUrl())
					.setAccessToken(accessToken)
					.buildQueryMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET,
					OAuthResourceResponse.class);
			String userId = null;

			try {
				userId = GoogleResource.getGoogleProfileEmail(resourceResponse);
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}
			if (userId != null) {
				httpRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, userId);
				if (userDAO.find(userId) == null) {
					userDAO.create(userId);
				}
				return true;
			} else {
				return false;
			}
		} catch (OAuthSystemException | OAuthProblemException e) {
			throw new WebApplicationException(e);
		}
	}

	private boolean isRequestToAdminPage() {
		boolean isRequestToAdminPage = (httpRequest.getPathInfo() != null) && httpRequest.getPathInfo().startsWith("/admin");
		if (isRequestToAdminPage) {
			return true;
		}
		Map<String, String> adminPathMap = env.getAdminPath();

		for (Map.Entry<String, String> entry : adminPathMap.entrySet()) {
			if (entry.getKey().startsWith("filter.path.admin-starts")) {
				boolean isParameterSatisfies = httpRequest.getRequestURI().startsWith(
						entry.getValue());
				isRequestToAdminPage = isRequestToAdminPage || isParameterSatisfies;
			} else if (entry.getKey().startsWith("filter.path.admin-ends")) {
				boolean isParameterSatisfies = httpRequest.getRequestURI().endsWith(
						entry.getValue());
				isRequestToAdminPage = isRequestToAdminPage || isParameterSatisfies;
			}
		}
		return isRequestToAdminPage;
	}

	private boolean isAuthRequest() {
		boolean isAuthRequest = (httpRequest.getPathInfo() == null);
		if (isAuthRequest) {
			return true;
		}
		Map<String, String> authPathMap = env.getAuthPath();
		for (Map.Entry<String, String> entry : authPathMap.entrySet()) {
			if (entry.getKey().startsWith("filter.path.auth")) {
				boolean isParameterSatisfies = httpRequest.getPathInfo().startsWith(
						entry.getValue());
				isAuthRequest = isAuthRequest || isParameterSatisfies;
			}
		}
		return isAuthRequest;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		String userId = (String) httpRequest.getSession().getAttribute(USER_ID_KEY);
		boolean isAdmin = env.getAdminMail().equals(EncryptionUtil.getMD5(userId));
		String contextPath = httpRequest.getContextPath();

		if (isRequestToAdminPage() && !isAdmin) {
			if (contextPath.startsWith(env.getContextPath())) {
				httpResponse.sendRedirect(contextPath);
			} else {
				httpResponse.sendRedirect("/");
			}
		} else if ((userId == null) && !isAuthRequest()) {
			if ((httpRequest.getHeader("Access-Token") != null) && !httpRequest.getHeader("Access-Token").isEmpty()) {
				if (!processNotAuthorizeUser(httpRequest)) {
					responseContext.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} else {
					responseContext.setStatus(HttpServletResponse.SC_OK);
				}
			} else {
				httpResponse.sendRedirect(contextPath);
			}
		}
	}
}
