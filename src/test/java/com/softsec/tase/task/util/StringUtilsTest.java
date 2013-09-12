/**
 * 
 */
package com.softsec.tase.task.util;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * StringUtilsTest.java
 * @author yanwei
 * @date 2013-1-29 下午4:15:26
 * @description
 */
public class StringUtilsTest extends TestCase {

	@Test
	public void testPreprocessAppVersion() {
		String appVersion = ".Build 3.6.5-beta2";
		System.out.println(StringUtils.preprocessAppVersion(appVersion));
	}
}
