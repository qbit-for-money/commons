package com.qbit.commons.xss.util;

import java.util.regex.Pattern;

/**
 * @author Alex
 */
public final class XSSRequestFilter {
	
	private XSSRequestFilter(){
	}

	private static Pattern[] patterns = new Pattern[]{
		Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
		Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
		Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
	};

	public static String stripXSS(String value) {
		if (value == null) {
			return null;
		}
		value = value.replaceAll("\0", "");
		for (Pattern scriptPattern : patterns) {
			value = scriptPattern.matcher(value).replaceAll("");
		}
		return value;
	}
}
