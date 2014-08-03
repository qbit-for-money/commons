package com.qbit.commons.env;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.inject.Singleton;

/**
 * Exchanger properties
 *
 * @author Alexander_Sergeev
 */
@Singleton
public class CommonsEnv {

	private final Properties properties;

	public CommonsEnv() {
		properties = new Properties();
		try {
			properties.load(CommonsEnv.class.getResourceAsStream("/com/qbit/commons/commons.properties"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getMailBotAddress() {
		return properties.getProperty("mail.bot.address");
	}

	public String getMailBotPersonal() {
		return properties.getProperty("mail.bot.personal");
	}

	public String getMailHost() {
		return properties.getProperty("mail.host");
	}

	public String getGoogleClientId() {
		return properties.getProperty("auth.google.clientId");
	}

	public String getGoogleClientSecret() {
		return properties.getProperty("auth.google.clientSecret");
	}

	public String getGoogleScope() {
		return properties.getProperty("auth.google.scope");
	}

	public String getGoogleUserInfoUrl() {
		return properties.getProperty("auth.google.userInfoUrl");
	}

	public String getGoogleAuthorizeRoute() {
		return properties.getProperty("auth.google.authorizeRoute");
	}

	public String getAdminMail() {
		return properties.getProperty("admin.mail");
	}

	public String getMailTemplatesPath() {
		return properties.getProperty("mail.templatesPath");
	}

	public Map<String, String> getAdminPath() {
		Map<String, String> adminMap = new HashMap<>();
		for (String name : properties.stringPropertyNames()) {
			if (name.startsWith("filter.path.admin")) {
				adminMap.put(name, properties.getProperty(name));
			}
		}
		return adminMap;
	}

	public Map<String, String> getAuthPath() {
		Map<String, String> authMap = new HashMap<>();
		for (String name : properties.stringPropertyNames()) {
			if (name.startsWith("filter.path.auth")) {
				authMap.put(name, properties.getProperty(name));
			}
		}
		return authMap;
	}

	public String getContextPath() {
		return properties.getProperty("filter.path.context-path");
	}
}
