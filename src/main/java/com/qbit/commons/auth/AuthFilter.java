package com.qbit.commons.auth;

import com.qbit.commons.env.EnvUtil;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Alexander_Sergeev
 */
public class AuthFilter implements Filter {

	public static final String USER_ID_KEY = "user_id";

	private EnvUtil env;
	private FilterConfig filterConfig;

	public static String getUserId(HttpServletRequest request) {
		return (String) request.getSession().getAttribute(USER_ID_KEY);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		env = new EnvUtil();
		filterConfig = fc;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		String userId = (String) httpRequest.getSession().getAttribute(USER_ID_KEY);
		boolean isAdmin = env.getAdminMail().equals(EncryptionUtil.getMD5(userId));

		boolean isRequestToAdminPage = (httpRequest.getPathInfo() != null) && httpRequest.getPathInfo().startsWith("/admin");

		Enumeration<String> parameterNamesForAdmin = filterConfig.getInitParameterNames();
		while (parameterNamesForAdmin.hasMoreElements()) {
			String parameterName = parameterNamesForAdmin.nextElement();
			if (parameterName.startsWith("admin-starts")) {
				boolean isParameterSatisfies = httpRequest.getRequestURI().startsWith(
						filterConfig.getInitParameter(parameterName));
				isRequestToAdminPage = isRequestToAdminPage || isParameterSatisfies;
			} else if (parameterName.startsWith("admin-ends")) {
				boolean isParameterSatisfies = httpRequest.getRequestURI().endsWith(
						filterConfig.getInitParameter(parameterName));
				isRequestToAdminPage = isRequestToAdminPage || isParameterSatisfies;
			}
		}

		boolean isAuthRequest = (httpRequest.getPathInfo() == null);

		if (!isAuthRequest) {
			Enumeration<String> parameterNamesForAuth = filterConfig.getInitParameterNames();
			boolean isEmptyPath = httpRequest.getPathInfo().equals("/");
			boolean isParametersSatisfies = false;
			while (parameterNamesForAuth.hasMoreElements()) {
				String parameterName = parameterNamesForAuth.nextElement();
				if (parameterName.startsWith("auth")) {
					boolean isParameterSatisfies = httpRequest.getPathInfo().startsWith(
							filterConfig.getInitParameter(parameterName));
					isParametersSatisfies = isParametersSatisfies || isParameterSatisfies; 
				}
			}
			isAuthRequest = isParametersSatisfies || isEmptyPath;
		}

		String contextPath = ((HttpServletRequest) servletRequest).getContextPath();
		if (isRequestToAdminPage && !isAdmin) {
			if (contextPath.startsWith(filterConfig.getInitParameter("context-path"))) {
				((HttpServletResponse) servletResponse).sendRedirect(contextPath);
			} else {
				((HttpServletResponse) servletResponse).sendRedirect("/");
			}
		} else if ((userId == null) && !isAuthRequest) {
			((HttpServletResponse) servletResponse).sendRedirect(((HttpServletRequest) servletRequest).getContextPath());
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	@Override
	public void destroy() {
	}
}
