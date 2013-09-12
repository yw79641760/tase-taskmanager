package com.softsec.tase.task.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wanghouming
 * 
 */

public class DateUtils {

	private static DateFormat FormatyyyyMMdd_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static DateFormat FormatyyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");

	private static DateFormat FormatyyyyMMdd = new SimpleDateFormat("yyyyMMdd");

	private static DateFormat Formatyyyy_MM_dd = new SimpleDateFormat("yyyy/MM/dd");
	
	public static DateFormat getFormatyyyyMMdd_HHmmss() {
		return FormatyyyyMMdd_HHmmss;
	}

	public static DateFormat getFormatyyyyMMddHHmmss() {
		return FormatyyyyMMddHHmmss;
	}

	public static DateFormat getFormatyyyyMMdd() {
		return FormatyyyyMMdd;
	}

	public static DateFormat getFormatyyyy_MM_dd() {
		return Formatyyyy_MM_dd;
	}

	public static String toString(Date date, String pattern){
		if(pattern == null){
			pattern = "yyyy-MM-dd";
		}
		DateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}
}
