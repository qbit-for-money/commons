package com.qbit.commons.auth;

import com.qbit.commons.env.CommonsEnv;
import com.qbit.commons.log.model.Log;
import com.qbit.commons.log.model.OperationType;
import com.qbit.commons.log.service.LogScheduler;
import com.qbit.commons.user.UserDAO;
import com.qbit.commons.user.UserInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Alex
 */
@Path("facebook-oauth2")
@Singleton
public class FacebookResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private CommonsEnv env;

	@Inject
	private LogScheduler logScheduler;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("authenticate")
	@Produces("text/html")
	public Response authenticate(@QueryParam("redirect") String redirectUri) {
		try {
			OAuthClientRequest request = OAuthClientRequest
					.authorizationProvider(org.apache.oltu.oauth2.common.OAuthProviderType.FACEBOOK)
					.setClientId(env.getFacebookClientId())
					.setResponseType("code")
					.setState(redirectUri)
					.setRedirectURI(
							UriBuilder.fromUri(uriInfo.getBaseUri())
							.path(env.getFacebookAuthorizeRoute()).build().toString())
					.buildQueryMessage();
			URI redirect = new URI(request.getLocationUri());
			return Response.seeOther(redirect).build();
		} catch (OAuthSystemException | URISyntaxException e) {
			throw new WebApplicationException(e);
		}
	}

	@GET
	@Path("authorize")
	@Produces("text/html")
	public Response authorize(@QueryParam("code") String code, @QueryParam("state") String state) throws URISyntaxException {
		String newURI = uriInfo.getBaseUri().toString();
		newURI = newURI.substring(0, newURI.indexOf("webapi"));
		URI uri = null;
		if((code == null) || code.isEmpty()) {
			uri = UriBuilder.fromUri(new URI(newURI)).path("/").build();
			return Response.seeOther(uri).build();
		}
		try {
			OAuthClientRequest request = OAuthClientRequest
					.tokenProvider(org.apache.oltu.oauth2.common.OAuthProviderType.FACEBOOK)
					.setCode(code)
					.setClientId(env.getFacebookClientId())
					.setClientSecret(env.getFacebookClientSecret())
					.setRedirectURI(UriBuilder.fromUri(uriInfo.getBaseUri())
							.path(env.getFacebookAuthorizeRoute()).build().toString())
					.buildBodyMessage();

			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
			String token = getAccessToken(request.getBody());
			OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(env.getFacebookUserInfoUrl())
					.setAccessToken(token)
					.buildQueryMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET,
					OAuthResourceResponse.class);
			String userId = getFacebookProfileId(resourceResponse);

			userId = "fb-" + userId;
			if (httpServletRequest.getSession().getAttribute(AuthFilter.USER_ALT_ID_KEY) != null) {
				String sessionUserId = (String) httpServletRequest.getSession().getAttribute(AuthFilter.USER_ALT_ID_KEY);
				if (!sessionUserId.contains("fb-")) {
					UserInfo userFromAllIds = userDAO.findFromAllIds(sessionUserId);
					UserInfo userIdFromAllIds = userDAO.findFromAllIds(userId);
					if ((userDAO.containsAuthServiceId(userId, sessionUserId) && ((userFromAllIds == null) || (userIdFromAllIds == null))) || ((userFromAllIds != null) && (userIdFromAllIds != null) && !userFromAllIds.getPublicKey().equals(userIdFromAllIds.getPublicKey()))) {
						uri = UriBuilder.fromUri(new URI(newURI)).fragment("/users/" + sessionUserId).build("/", "/users/" + sessionUserId);
						return Response.seeOther(uri).build();
					}
					UserInfo userWithAddId = userDAO.setAdditionalId(userFromAllIds.getPublicKey(), userId);
					if (userWithAddId == null) {
						httpServletRequest.getSession().setAttribute(AuthFilter.USER_ALT_ID_KEY, userId);
						sessionUserId = (String) httpServletRequest.getSession().getAttribute(AuthFilter.USER_ALT_ID_KEY);
						uri = UriBuilder.fromUri(new URI(newURI)).fragment("/users/" + sessionUserId).build("/", "/users/" + sessionUserId);
						return Response.seeOther(uri).build();
					}
					state = "profile";
				} else {
					httpServletRequest.getSession().removeAttribute(AuthFilter.USER_ID_KEY);
					httpServletRequest.getSession().removeAttribute(AuthFilter.USER_ALT_ID_KEY);
					return Response.seeOther(uri).build();
				}
			}
			httpServletRequest.getSession().setAttribute(AuthFilter.USER_ALT_ID_KEY, userId);
			UserInfo user = userDAO.findFromAllIds(userId);
			if ((user == null)) {
				user = userDAO.create(userId);
			}
			httpServletRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, user.getPublicKey());
			if (logScheduler != null) {
				Log log = new Log();
				log.setType(OperationType.LOGIN_LOGOUT);
				log.setUserId(user.getPublicKey());
				String additionalIdsStr = "";
				for(String id : user.getAdditionalIds()) {
					additionalIdsStr += id + ";";
				}
				log.setUserAdditionalIds(additionalIdsStr);
				log.setMachineId(AuthFilter.getMachineId(httpServletRequest));
				log.setLocation(AuthFilter.getUserLocation(httpServletRequest));
				log.setSessionId(httpServletRequest.getSession().getId());
				logScheduler.createLog(log);
			}
			if ("profile".equals(state)) {
				uri = UriBuilder.fromUri(new URI(newURI)).fragment("/users/" + userId).build("/", "/users/" + userId);
			} else if (!(state == null) && !state.isEmpty()) {
				uri = UriBuilder.fromUri(new URI(newURI)).fragment(state).build("", state);
			} else {
				uri = UriBuilder.fromUri(new URI(newURI)).path("/").build();
			}

		} catch (OAuthSystemException | OAuthProblemException e) {
			throw new WebApplicationException(e);
		} catch (MalformedURLException e) {
			throw new WebApplicationException(e);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
		return Response.seeOther(uri).build();
	}

	private String getAccessToken(String requestBody) throws MalformedURLException, IOException {
		URL u = new URL("https://graph.facebook.com/oauth/access_token?" + requestBody);
		URLConnection c = u.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
		String inputLine;
		StringBuilder b = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			b.append(inputLine);
		}
		in.close();
		String respond = b.toString();
		String respondReplacedAccess = respond.replace("access_token=", "");
		String token = respondReplacedAccess.substring(0, respondReplacedAccess.indexOf("&expires="));
		return token;
	}

	private String getFacebookProfileId(OAuthResourceResponse resourceResponse) throws IOException {
		String resourceResponseBody = resourceResponse.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(resourceResponseBody);
		JsonNode idNode = jsonNode.get("id");
		return (idNode != null) ? idNode.asText() : null;
	}
}
