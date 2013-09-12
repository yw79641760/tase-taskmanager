/**
 * 
 */
package com.softsec.tase.task.util;

import java.text.DecimalFormat;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * AppFilterTest
 * <p> </p>
 * @author yanwei
 * @since 2013-6-7 上午8:32:46
 * @version
 */
public class AppFilterTest extends TestCase {
	
	@Test
	public void testParseAppInfo() {
		String description = "Down In Ashes\\\\\\";
		while(description.endsWith("\\")) {
			description = description.substring(0, description.length() - 1);
		}
		System.out.println(description.endsWith("\\"));
		
		String url = "http://www.softsec.com/it'satest'\\\\".trim().replaceAll("'", "");
//		while(url.endsWith("\\")) {
//			url = url.substring(0, url.length() - 1);
//		}
		url.replaceAll("\\\\", "");
		System.out.println(url.endsWith("\\"));
	}
	
	@Test
	public void testFilePath() {
		String resultPath = "/path/to/source.apk";
		int lastSlashIndex = resultPath.lastIndexOf("/") == -1 ? 0 : resultPath.lastIndexOf("/") + 1;
		int lastDotIndex = resultPath.lastIndexOf(".") == -1 ? resultPath.length() : resultPath.lastIndexOf(".");
		String sourceApkFileName = resultPath.substring(lastSlashIndex, lastDotIndex);
		System.out.println(sourceApkFileName);
		System.out.println(new DecimalFormat("##").format(199 % 100));
	}

}
