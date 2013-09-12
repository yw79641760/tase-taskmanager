/**
 * 
 */
package com.softsec.tase.task.util;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.task.util.app.AppVersionParser;

/**
 * AppVersionParserTest.java
 * @author yanwei
 * @date 2013-1-29 下午1:50:56
 * @description
 */
public class AppVersionParserTest extends TestCase {
	
	@Test
	public void testAppVersionParser() {
		String appVersion = "0";
		AppVersionParser parser = new AppVersionParser(StringUtils.preprocessAppVersion(appVersion));
		System.out.println(parser.getMajorVersion());
		System.out.println(parser.getMinorVersion());
		System.out.println(parser.getRevisionVersion());
		System.out.println(parser.getBuildVersion());
		System.out.println(parser.getExtraVersion());
	}
	
}
