package com.qbit.commons.env;

import java.io.IOException;
import java.util.Properties;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Exchanger properties
 *
 * @author Alexander_Sergeev
 */
@Singleton
@XmlAccessorType(XmlAccessType.PROPERTY)
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
}
