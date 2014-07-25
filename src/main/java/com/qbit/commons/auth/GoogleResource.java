package com.qbit.commons.auth;

import com.qbit.commons.env.CommonsEnv;
import com.qbit.commons.user.UserDAO;
import java.io.IOException;
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
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Alexander_Sergeev
 */
@Path("oauth2")
@Singleton
public class GoogleResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private CommonsEnv env;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("authenticate")
	@Produces("text/html")
	public Response authenticate(@QueryParam("redirect") String redirectUri) {
		try {
			OAuthClientRequest request = OAuthClientRequest
					.authorizationProvider(OAuthProviderType.GOOGLE)
					.setClientId(env.getGoogleClientId())
					.setResponseType("code")
					.setScope(env.getGoogleScope())
					.setState(redirectUri)
					.setRedirectURI(
							UriBuilder.fromUri(uriInfo.getBaseUri())
							.path(env.getGoogleAuthorizeRoute()).build().toString())
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
		try {
			OAuthClientRequest request = OAuthClientRequest
					.tokenProvider(OAuthProviderType.GOOGLE)
					.setCode(code)
					.setClientId(env.getGoogleClientId())
					.setClientSecret(env.getGoogleClientSecret())
					.setRedirectURI(UriBuilder.fromUri(uriInfo.getBaseUri())
							.path(env.getGoogleAuthorizeRoute()).build().toString())
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.buildBodyMessage();

			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
			OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

			OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(env.getGoogleUserInfoUrl())
					.setAccessToken(oAuthResponse.getAccessToken())
					.buildQueryMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET,
					OAuthResourceResponse.class);
			String userId = getGoogleProfileEmail(resourceResponse);
			if (userId != null) {
				httpServletRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, userId);
				if (userDAO.find(userId) == null) {
					userDAO.create(userId);

				}
				if ("profile".equals(state)) {
					uri = UriBuilder.fromUri(new URI(newURI)).fragment("/users/" + userId).build("/", "/users/" + userId);
				} else if (!(state == null) && !state.isEmpty()) {
					uri = UriBuilder.fromUri(new URI(newURI)).fragment(state).build("", state);
				} else {
					uri = UriBuilder.fromUri(new URI(newURI)).path("/").build();
				}
			}
		} catch (OAuthSystemException | OAuthProblemException | IOException e) {
			throw new WebApplicationException(e);
		}
		return Response.seeOther(uri).build();
	}

	public static String getGoogleProfileEmail(OAuthResourceResponse resourceResponse) throws IOException {
		String resourceResponseBody = resourceResponse.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(resourceResponseBody);
		JsonNode idNode = jsonNode.get("email");

		return (idNode != null) ? idNode.asText() : null;
	}
}
