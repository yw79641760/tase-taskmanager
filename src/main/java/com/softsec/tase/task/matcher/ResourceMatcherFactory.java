/**
 * 
 */
package com.softsec.tase.task.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;

/**
 * Resource matcher factory
 * 		get resource matcher
 * @author yanwei
 * @date 2013-1-9 上午9:11:08
 * 
 */
public class ResourceMatcherFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMatcherFactory.class);
	
	/**
	 * get default resource matcher
	 * @return
	 */
	public static ResourceMatcher getResourceMatcher() {
		String defaultResourceMatcher = Configuration.get(Constants.DEFAULT_RESOURCE_MATCHER, null);
		try {
			return (ResourceMatcher) Class.forName(defaultResourceMatcher).newInstance();
		} catch (InstantiationException ie) {
			LOGGER.error("Failed in class instantiation : " + ie.getMessage(), ie);
			throw new RuntimeException("Failed in class instantiation : " + ie.getMessage(), ie);
		} catch (IllegalAccessException iae) {
			LOGGER.error("Failed in access class : " + iae.getMessage(), iae);
			throw new RuntimeException("Failed in access class : " + iae.getMessage(), iae);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error("Failed to find class : " + defaultResourceMatcher + " : " + cnfe.getMessage(), cnfe);
			throw new RuntimeException("Failed to find class : " + defaultResourceMatcher + " : " + cnfe.getMessage(), cnfe);
		}
	}
	
	/**
	 * get resource matcher by specific matcher name
	 * @param matcherName
	 * @return
	 */
	public static ResourceMatcher getResourceMatcher(String matcherName) {
		if (!StringUtils.isEmpty(matcherName)) {
			try {
				return (ResourceMatcher) Class.forName(matcherName).newInstance();
			} catch (InstantiationException ie) {
				LOGGER.error("Failed in class instantiation : " + ie.getMessage(), ie);
				throw new RuntimeException("Failed in class instantiation : " + ie.getMessage(), ie);
			} catch (IllegalAccessException iae) {
				LOGGER.error("Failed in access class : " + iae.getMessage(), iae);
				throw new RuntimeException("Failed in access class : " + iae.getMessage(), iae);
			} catch (ClassNotFoundException cnfe) {
				LOGGER.error("Failed to find class : " + matcherName + " : " + cnfe.getMessage(), cnfe);
				throw new RuntimeException("Failed to find class : " + matcherName + " : " + cnfe.getMessage(), cnfe);
			}
		}
		return null;
	}
}
