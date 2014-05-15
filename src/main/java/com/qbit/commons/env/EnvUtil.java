package com.qbit.commons.env;

import java.io.IOException;
import java.util.Properties;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Exchanger properties
 *
 * @author Alexander_Sergeev
 */
@Singleton
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnvUtil {

	private final Properties properties;

	public EnvUtil() {
		properties = new Properties();
		try {
			properties.load(EnvUtil.class.getResourceAsStream("/com/qbit/commons.properties"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@XmlElement
	public boolean isDemoEnabled() {
		return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("demo.enabled"));
	}

	@XmlElement
	public String getMailBotAddress() {
		return properties.getProperty("mail.bot.address");
	}

	@XmlTransient
	public String getMailBotPersonal() {
		return properties.getProperty("mail.bot.personal");
	}

	@XmlTransient
	public String getMailHost() {
		return properties.getProperty("mail.host");
	}

	@XmlTransient
	public int getOrderWorkerPeriodSecs() {
		return Integer.parseInt(properties.getProperty("order.worker.period.secs"));
	}

	@XmlTransient
	public String getGoogleClientId() {
		return properties.getProperty("auth.google.clientId");
	}

	@XmlTransient
	public String getGoogleClientSecret() {
		return properties.getProperty("auth.google.clientSecret");
	}

	@XmlTransient
	public String getGoogleScope() {
		return properties.getProperty("auth.google.scope");
	}

	@XmlTransient
	public String getGoogleUserInfoUrl() {
		return properties.getProperty("auth.google.userInfoUrl");
	}

	@XmlTransient
	public String getGoogleAuthorizeRoute() {
		return properties.getProperty("auth.google.authorizeRoute");
	}

	@XmlTransient
	public String getAdminMail() {
		return properties.getProperty("admin.mail");
	}

	@XmlTransient
	public String getMailTemplatesPath() {
		return properties.getProperty("mail.templatesPath");
	}
}
