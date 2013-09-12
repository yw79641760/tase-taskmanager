/**
 * 
 */
package com.softsec.tase.task.result;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.domain.app.App;
import com.softsec.tase.common.rpc.domain.app.AppWeb;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.service.AppStorageService;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.task.exception.ResultException;
import com.softsec.tase.task.pool.AppMapper;
import com.softsec.tase.task.util.app.AppPriceParser;
import com.softsec.tase.task.util.app.AppVersionParser;

/**
 * 
 * @author yanwei
 * @date 2013-1-16 上午10:44:25
 * 
 */
public class CommonAnalysisInitializeCollector extends ResultCollectorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonAnalysisInitializeCollector.class);
	
	private AppWeb appWeb = null;
	
	/**
	 * parse result to app web
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#parse(java.lang.String, java.nio.ByteBuffer, java.lang.String)
	 */
	@Override
	public int parse(Result result) throws ResultException {
		
		int retValue = -1;
		try {
			appWeb = (AppWeb)IOUtils.getObject(result.getContent().array());
			retValue = 0;
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to extract app web from bytes : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to extract app web from bytes : " + ioue.getMessage(), ioue);
		}
		return retValue;
	}

	/**
	 * save app into database
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#save()
	 */
	@Override
	public int save() throws ResultException {
		
		int retValue = 0;
		AppStorageService appStorageService = new AppStorageService();
		
		if (appWeb != null) {
			
			long appId = 0L;

			// check for duplication
			try {
				appId = appStorageService.checkAppDuplication(appWeb.getAppType(), appWeb.getOriginType(), StringUtils.md5Encode(appWeb.getStoreName()
						 + appWeb.getUrl() + appWeb.getAppVersion()));
			} catch (NullPointerException npe) {
				// ignore exception
			} catch (UnsupportedEncodingException uee) {
				// ignore exception
			} catch (NoSuchAlgorithmException nae) {
				// ignore exception
			} catch (DbUtilsException de) {
				LOGGER.error("Failed to query duplication of app : " + appWeb.getAppName() + " : " + de.getMessage(), de);
				throw new ResultException("Failed to query duplication of app : " + appWeb.getAppName() + " : " + de.getMessage(), de);
			}
			
			//
			if (appId != 0) {
				LOGGER.info("Duplicated app collected : [ " + appWeb.getStoreName() + " : " + appWeb.getAppName() + " : "
						+ appWeb.getAppVersion() + " : " + appWeb.getUrl());
				return retValue;
			}
			
			// generate app id
			App app = new App(appWeb);
			try {
				appId = AppMapper.getInstance().generateAppId(appWeb.getAppType(), appWeb.getOriginType(), 
						appWeb.getStoreName(), appWeb.getStoreDisplayName(), appWeb.getStoreUrl(), 
						appWeb.getStoreType(), appWeb.getCategory());
				app.setAppId(appId);
			} catch (DbUtilsException de) {
				LOGGER.error("Failed to generate id of app : " + app.getAppName() + " : " + de.getMessage(), de);
				throw new ResultException("Failed to generate id of app : " + app.getAppName() + " : " + de.getMessage(), de);
			}
			
			AppPriceParser priceParser = new AppPriceParser(appWeb.price);
			app.setCurrencyUnit(priceParser.getCurrencyUnit());
			app.setPrice(priceParser.getPrice());

			AppVersionParser versionParser = new AppVersionParser(app.getAppVersion());
			app.setMajorVersion(versionParser.getMajorVersion());
			app.setMinorVersioin(versionParser.getMinorVersion());
			app.setRevisionVersion(versionParser.getRevisionVersion());
			app.setBuildVersion(versionParser.getBuildVersion());
			app.setExtraVersion(versionParser.getExtraVersion());
			
			// save app into database
			try {
				retValue = appStorageService.save(app);
			} catch (DbUtilsException de) {
				LOGGER.error("Failed to save app : " + app.getAppId() + " : " + de.getMessage(), de);
				throw new ResultException("Failed to save app : " + app.getAppId() + " : " + de.getMessage(), de);
			}
		}
		return retValue;
	}
}
