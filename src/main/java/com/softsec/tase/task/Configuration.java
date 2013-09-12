package com.softsec.tase.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class Configuration {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class);

	private static final Properties properties = new Properties();

	static {
		try {
			InputStream input = Configuration.class.getClassLoader().getResourceAsStream("config.properties");
			if (input == null) {
				input = Configuration.class.getResourceAsStream("config.properties");
			}
			LOGGER.info("Loading configure file from :" + input);
			properties.load(input);
			printAllConfigure();
		} catch (IOException e) {
			throw new RuntimeException("Load config file failed", e);
		}
	}

	private Configuration() {
	}

	private static void printAllConfigure() {
		Set<Object> keys = properties.keySet();
		Iterator<Object> iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			LOGGER.info(key + " = " + properties.getProperty(key));
		}
	}

	public static String get(String key, String defaultValue) {
		String value = properties.getProperty(key, defaultValue);
		if (!StringUtils.isEmpty(value)) {
			value = value.replaceAll("\\s", "");
		}
		return value;
	}

	public static int getInt(String key, int defaultValue) {
		String value = properties.getProperty(key);
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		return Integer.parseInt(value.replaceAll("\\s", ""));
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		String value = properties.getProperty(key);
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value.replaceAll("\\s", ""));
	}
}
