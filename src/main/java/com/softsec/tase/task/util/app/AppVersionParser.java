/**
 * 
 */
package com.softsec.tase.task.util.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.softsec.tase.task.util.StringUtils;

/**
 * app version info parser
 * @author yanwei
 * @date 2013-1-16 下午5:14:05
 * 
 */
public class AppVersionParser {

	private long majorVersion;
	
	private long minorVersion;
	
	private long revisionVersion;
	
	private long buildVersion;
	
	private long extraVersion;
	
	public AppVersionParser(String appVersion) {
		
		if (!StringUtils.isEmpty(StringUtils.preprocessAppVersion(appVersion))) {
		
			int dotCount = StringUtils.countOccurrence(appVersion, "\\.");
			String regex = null;
			Pattern pattern = null;
			Matcher matcher = null;
			
			switch(dotCount) {
			case(4):
				regex = "(\\d+)[.*](\\d*)[.*](\\d*)[.*](\\d*)[.*](\\d+)";
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(appVersion);
				if (matcher.find()) {
					majorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(1)) ? new Integer(0).toString() : matcher.group(1)));
					minorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(2)) ? new Integer(0).toString() : matcher.group(2)));
					revisionVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(3)) ? new Integer(0).toString() : matcher.group(3)));
					buildVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(4)) ? new Integer(0).toString() : matcher.group(4)));
					extraVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(5)) ? new Integer(0).toString() : matcher.group(5)));
				}
				break;
			case(3):
				regex = "(\\d*)[.*](\\d*)[.*](\\d*)[.*](\\d+)";
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(appVersion);
				if (matcher.find()) {
					majorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(1)) ? new Integer(0).toString() : matcher.group(1)));
					minorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(2)) ? new Integer(0).toString() : matcher.group(2)));
					revisionVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(3)) ? new Integer(0).toString() : matcher.group(3)));
					buildVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(4)) ? new Integer(0).toString() : matcher.group(4)));
				}
				break;
			case(2):
				regex = "(\\d*)[.*](\\d*)[.*](\\d+)";
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(appVersion);
				if (matcher.find()) {
					majorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(1)) ? new Integer(0).toString() : matcher.group(1)));
					minorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(2)) ? new Integer(0).toString() : matcher.group(2)));
					revisionVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(3)) ? new Integer(0).toString() : matcher.group(3)));
				}
				break;
			case(1):
				regex = "(\\d*)[.*](\\d+)";
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(appVersion);
				if (matcher.find()) {
					majorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(1)) ? new Integer(0).toString() : matcher.group(1)));
					minorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(2)) ? new Integer(0).toString() : matcher.group(2)));
				}
				break;
			case(0):
				regex = "(\\d+)";
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(appVersion);
				if (matcher.find()) {
					majorVersion = Long.parseLong((StringUtils.isEmpty(matcher.group(1)) ? new Integer(0).toString() : matcher.group(1)));
				}
				break;
			}
		}
	}
	
	public long getMajorVersion() {
		return majorVersion;
	}
	
	public long getMinorVersion() {
		return minorVersion;
	}
	
	public long getRevisionVersion() {
		return revisionVersion;
	}
	
	public long getBuildVersion() {
		return buildVersion;
	}
	
	public long getExtraVersion() {
		return extraVersion;
	}
}
