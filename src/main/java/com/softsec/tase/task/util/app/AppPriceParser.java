/**
 * 
 */
package com.softsec.tase.task.util.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.softsec.tase.common.util.StringUtils;

/**
 * AppPriceParser.java
 * @author yanwei
 * @date 2013-1-31 下午4:59:54
 * @description
 */
public class AppPriceParser {

	private String currencyUnit;
	
	private double price;
	
	public AppPriceParser(String appPrice) {
		appPrice = appPrice.trim();
		if(!StringUtils.isEmpty(appPrice)) {
			if (appPrice.startsWith("¥") || appPrice.endsWith("RMB")) {
				currencyUnit = "CNY";
			} else if (appPrice.startsWith("$") || appPrice.endsWith("Dollars")) {
				currencyUnit = "USD";
			}
			
			String regex = "(\\d+)\\.*(\\d+)";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(appPrice);
			if (matcher.find()) {
				price = Double.parseDouble(matcher.group(0));
			}
		}
	}
	
	public String getCurrencyUnit() {
		return currencyUnit;
	}
	
	public double getPrice() {
		return price;
	}
}
