/**
 * 
 */
package com.softsec.tase.task.util;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.task.util.app.AppPriceParser;

/**
 * AppPriceTest.java
 * @author yanwei
 * @date 2013-2-1 下午3:00:40
 * @description
 */
public class AppPriceParserTest extends TestCase {

	@Test
	public void testAppPriceParser() {
		String rawPrice = "¥6.00";
		AppPriceParser parser = new AppPriceParser(rawPrice);
		System.out.println(parser.getCurrencyUnit());
		System.out.println(parser.getPrice());
	}
}
