package com.qbit.commons.auth;

import com.qbit.commons.env.CommonsEnv;
import com.qbit.commons.log.model.Log;
import com.qbit.commons.log.model.OperationType;
import com.qbit.commons.log.service.LogScheduler;
import com.qbit.commons.socialvalues.SocialNetworkUserDataService;
import com.qbit.commons.socialvalues.SocialValueService;
import com.qbit.commons.socialvalues.VKUserDataService;
import com.qbit.commons.user.UserDAO;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import com.qbit.commons.user.UserInfo;
import java.io.IOException;

/**
 * @author Alexander_Sergeev
 */
@Path("vk-oauth2")
@Singleton
public class VKResource {

	public final static String VK_FRIENDS_API_BASE_URL = "https://api.vk.com/method/";

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private CommonsEnv env;

	@Inject
	private UserDAO userDAO;

	@Inject
	private LogScheduler logScheduler;

	@GET
	@Path("authenticate")
	@Produces("text/html")
	public Response authenticate(@QueryParam("redirect") String redirectUri) {
		try {
			OAuthClientRequest request = QBITOAuthClientRequest
					.authorizationProvider(OAuthProviderType.VK)
					.setClientId(env.getVKClientId())
					.setResponseType("code")
					.setScope(env.getVKScope())
					.setState(redirectUri)
					.setRedirectURI(
							UriBuilder.fromUri(uriInfo.getBaseUri())
							.path(env.getVKAuthorizeRoute()).build().toString())
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
			OAuthClientRequest request = QBITOAuthClientRequest
					.tokenProvider(OAuthProviderType.VK)
					.setCode(code)
					.setClientId(env.getVKClientId())
					.setClientSecret(env.getVKClientSecret())
					.setRedirectURI(UriBuilder.fromUri(uriInfo.getBaseUri())
							.path(env.getVKAuthorizeRoute()).build().toString())
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.buildBodyMessage();

			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
			OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);
			
			String vkUserId = oAuthResponse.getParam("user_id");
			String accessToken = oAuthResponse.getAccessToken();
			if (vkUserId == null) {
				return null;
			}
			uri = UriBuilder.fromUri(new URI(newURI)).path("/").build();
			String userId = "vk-" + vkUserId;
			if (httpServletRequest.getSession().getAttribute(AuthFilter.USER_ALT_ID_KEY) != null) {
				String sessionUserId = (String) httpServletRequest.getSession().getAttribute(AuthFilter.USER_ALT_ID_KEY);
				if (!sessionUserId.contains("vk-")) {
					UserInfo userFromAllIds = userDAO.findFromAllIds(sessionUserId);
					UserInfo userIdFromAllIds = userDAO.findFromAllIds(userId);
					if ((userDAO.containsAuthServiceId(userId, sessionUserId) && ((userFromAllIds == null) || (userIdFromAllIds == null))) || ((userFromAllIds != null) && (userIdFromAllIds != null) && !userFromAllIds.getPublicKey().equals(userIdFromAllIds.getPublicKey()))) {
						uri = UriBuilder.fromUri(new URI(newURI)).fragment("/users/" + sessionUserId).build("/", "/users/" + sessionUserId);
						return Response.seeOther(uri).build();
					}
					UserInfo userWithAddId = userDAO.setAdditionalId(sessionUserId, userId);
					if (userWithAddId == null) {
						logUserSocialValue(vkUserId, accessToken);
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
				logUserSocialValue(vkUserId, accessToken);
			}
			httpServletRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, user.getPublicKey());
			if (logScheduler != null) {
				Log log = new Log();
				log.setType(OperationType.LOGIN_LOGOUT);
				log.setUserId(user.getPublicKey());
				String additionalIdsStr = "";
				for (String id : user.getAdditionalIds()) {
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
		}
		return Response.seeOther(uri).build();
	}

	private void logUserSocialValue(String userId, String accessToken) {
		try {
			SocialNetworkUserDataService vkDataService = new VKUserDataService(userId, accessToken);
			long value = SocialValueService.getValue(vkDataService);
			if (logScheduler != null) {
				Log log = new Log();
				log.setType(OperationType.SOCIAL_VALUE);
				log.setFieldName("money");
				log.setFieldValue(String.valueOf(value));
				log.setUserId(userId);
				log.setMachineId(AuthFilter.getMachineId(httpServletRequest));
				log.setLocation(AuthFilter.getUserLocation(httpServletRequest));
				log.setSessionId(httpServletRequest.getSession().getId());
				logScheduler.createLog(log);
			}
		} catch (IOException ex) {
			//
		}
	}
}
