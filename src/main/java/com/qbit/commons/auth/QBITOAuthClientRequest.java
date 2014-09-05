package com.qbit.commons.auth;

import java.util.HashMap;
import java.util.Map;
import org.apache.oltu.oauth2.client.request.ClientHeaderParametersApplier;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthMessage;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.parameters.BodyURLEncodedParametersApplier;
import org.apache.oltu.oauth2.common.parameters.OAuthParametersApplier;
import org.apache.oltu.oauth2.common.parameters.QueryParameterApplier;

/**
 *
 * @author Alex
 */
public class QBITOAuthClientRequest extends org.apache.oltu.oauth2.client.request.OAuthClientRequest {

	public QBITOAuthClientRequest(String url) {
		super(url);
	}

	public static OAuthClientRequest.AuthenticationRequestBuilder authorizationLocation(String url) {
		return new OAuthClientRequest.AuthenticationRequestBuilder(url);
	}

	public static OAuthClientRequest.AuthenticationRequestBuilder authorizationProvider(OAuthProviderType provider) {
		return authorizationLocation(provider.getAuthzEndpoint());
	}

	public static OAuthClientRequest.TokenRequestBuilder tokenProvider(OAuthProviderType provider) {
		return tokenLocation(provider.getTokenEndpoint());
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public void addHeader(String name, String header) {
		this.headers.put(name, header);
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getLocationUri() {
		return url;
	}

	public void setLocationUri(String uri) {
		this.url = uri;
	}

	public String getHeader(String name) {
		return this.headers.get(name);
	}

	public void setHeader(String name, String value) {
		this.headers.put(name, value);
	}

	public abstract static class OAuthRequestBuilder {

		protected OAuthParametersApplier applier;
		protected Map<String, Object> parameters = new HashMap<String, Object>();

		protected String url;

		protected OAuthRequestBuilder(String url) {
			this.url = url;
		}

		public QBITOAuthClientRequest buildQueryMessage() throws OAuthSystemException {
			QBITOAuthClientRequest request = new QBITOAuthClientRequest(url);
			this.applier = new QueryParameterApplier();
			return (QBITOAuthClientRequest) applier.applyOAuthParameters(request, parameters);
		}

		public QBITOAuthClientRequest buildBodyMessage() throws OAuthSystemException {
			QBITOAuthClientRequest request = new QBITOAuthClientRequest(url);
			this.applier = new BodyURLEncodedParametersApplier();
			return (QBITOAuthClientRequest) applier.applyOAuthParameters(request, parameters);
		}

		public OAuthClientRequest buildHeaderMessage() throws OAuthSystemException {
			QBITOAuthClientRequest request = new QBITOAuthClientRequest(url);
			this.applier = new ClientHeaderParametersApplier();
			return (QBITOAuthClientRequest) applier.applyOAuthParameters(request, parameters);
		}
	}

	public static class AuthenticationRequestBuilder extends OAuthRequestBuilder {

		public AuthenticationRequestBuilder(String url) {
			super(url);
		}

		public AuthenticationRequestBuilder setResponseType(String type) {
			this.parameters.put(OAuth.OAUTH_RESPONSE_TYPE, type);
			return this;
		}

		public AuthenticationRequestBuilder setClientId(String clientId) {
			this.parameters.put(OAuth.OAUTH_CLIENT_ID, clientId);
			return this;
		}

		public AuthenticationRequestBuilder setRedirectURI(String uri) {
			this.parameters.put(OAuth.OAUTH_REDIRECT_URI, uri);
			return this;
		}

		public AuthenticationRequestBuilder setState(String state) {
			this.parameters.put(OAuth.OAUTH_STATE, state);
			return this;
		}

		public AuthenticationRequestBuilder setScope(String scope) {
			this.parameters.put(OAuth.OAUTH_SCOPE, scope);
			return this;
		}

		public AuthenticationRequestBuilder setParameter(String paramName, String paramValue) {
			this.parameters.put(paramName, paramValue);
			return this;
		}
	}

	public static class TokenRequestBuilder extends OAuthRequestBuilder {

		public TokenRequestBuilder(String url) {
			super(url);
		}

		public TokenRequestBuilder setGrantType(GrantType grantType) {
			this.parameters.put(OAuth.OAUTH_GRANT_TYPE, grantType == null ? null : grantType.toString());
			return this;
		}

		public TokenRequestBuilder setClientId(String clientId) {
			this.parameters.put(OAuth.OAUTH_CLIENT_ID, clientId);
			return this;
		}

		public TokenRequestBuilder setClientSecret(String secret) {
			this.parameters.put(OAuth.OAUTH_CLIENT_SECRET, secret);
			return this;
		}

		public TokenRequestBuilder setUsername(String username) {
			this.parameters.put(OAuth.OAUTH_USERNAME, username);
			return this;
		}

		public TokenRequestBuilder setPassword(String password) {
			this.parameters.put(OAuth.OAUTH_PASSWORD, password);
			return this;
		}

		public TokenRequestBuilder setScope(String scope) {
			this.parameters.put(OAuth.OAUTH_SCOPE, scope);
			return this;
		}

		public TokenRequestBuilder setCode(String code) {
			this.parameters.put(OAuth.OAUTH_CODE, code);
			return this;
		}

		public TokenRequestBuilder setRedirectURI(String uri) {
			this.parameters.put(OAuth.OAUTH_REDIRECT_URI, uri);
			return this;
		}

		public TokenRequestBuilder setAssertion(String assertion) {
			this.parameters.put(OAuth.OAUTH_ASSERTION, assertion);
			return this;
		}

		public TokenRequestBuilder setAssertionType(String assertionType) {
			this.parameters.put(OAuth.OAUTH_ASSERTION_TYPE, assertionType);
			return this;
		}

		public TokenRequestBuilder setRefreshToken(String token) {
			this.parameters.put(OAuth.OAUTH_REFRESH_TOKEN, token);
			return this;
		}

		public TokenRequestBuilder setParameter(String paramName, String paramValue) {
			this.parameters.put(paramName, paramValue);
			return this;
		}

	}
}
