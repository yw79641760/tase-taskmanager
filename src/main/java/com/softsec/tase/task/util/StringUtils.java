/**
 * 
 */
package com.softsec.tase.task.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *@author yanwei
 *@time: 2012-6-16 下午3:11:23
 *@description 字符串工具类
 *
 */
/**
 *
 */
public class StringUtils {
	/**
	 * is string empty or null
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}
	/**
	 * make dir path ends with "/" 
	 * @param str
	 * @return
	 */
	public static String validateDir(String str) {
		return str.endsWith("/") ? str: str + "/";
	}
	
	public static String getZipfileDir(String zipFilePath) {
		return zipFilePath.substring(0, zipFilePath.lastIndexOf('.')) + "/";
	}
	
	/**
	 * 匹配形如“Payload/8684公交.app/8684公交”的字符串
	 * @param path
	 * @return 匹配成功则返回字符串本身，否则返回null
	 */
	public static String parseAppExecutablePath(String path) {
		
		String regex = "Payload\\/(.*)\\.app\\/\\1";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(path);
		
		if (!matcher.find()) {
			return null;
		} else {
			return path;
		}
	}
	
	/**
	 * Count the occurrence of target by regex
	 * @param source
	 * @param target
	 * @return
	 */
	public static int countOccurrence(String source, String target) {
		if(source.endsWith(target)) {
			return source.split(target).length;
		} else {
			return source.split(target).length - 1;
		}
	}
	
	/**
	 * extract app version which starts with number and ends with number and divided by dot
	 * @param appVersion
	 * @return
	 */
	public static String preprocessAppVersion(String appVersion) {
		// only match version start with digit and end with digit and divided by dot
		// or only digits
		String regex = "(\\d+\\.)*(\\d+)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		try {
			matcher = pattern.matcher(appVersion);
		} catch (Exception e) {
			// can not parse app version
			// just ignore error and do nothing
		}
		if (matcher != null && matcher.find()) {
			return matcher.group(0);
		}
		
		return null;
	}
}