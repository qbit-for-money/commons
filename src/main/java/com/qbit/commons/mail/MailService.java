package com.qbit.commons.mail;

import com.qbit.commons.env.EnvUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class MailService {

	private final Logger logger = LoggerFactory.getLogger(MailService.class);

	@Inject
	private EnvUtil env;

	@Context
	private ServletContext servletContext;

	private static final Configuration FREE_MAKER_CFG = new Configuration();

	@PostConstruct
	public void init() {
		FREE_MAKER_CFG.setServletContextForTemplateLoading(
				servletContext, "WEB-INF/templates");
		FREE_MAKER_CFG.setDefaultEncoding("UTF-8");
		FREE_MAKER_CFG.setLocale(Locale.US);
		FREE_MAKER_CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	public void send(String to, String subject, String templateName, Map<String, Object> templateInput) {
		try {
			Template template = FREE_MAKER_CFG.getTemplate(templateName + ".tmpl");
			Writer text = new StringWriter();
			template.process(templateInput, text);
			send(to, subject, text.toString());
		} catch (Exception ex) {
			if (logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public void send(String to, String subject, String text) {
		try {
			Email email = new SimpleEmail();
			email.setHostName(env.getMailHost());
			email.setSmtpPort(465);
			email.setAuthenticator(new DefaultAuthenticator(env.getMailBotAddress(),
					env.getMailBotPersonal()));
			email.setSSL(true);
			email.setFrom(env.getMailBotAddress());
			email.addTo(to);
			email.setSubject(subject);
			email.setMsg(text);
			email.send();
		} catch (Exception ex) {
			if (logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}
}
