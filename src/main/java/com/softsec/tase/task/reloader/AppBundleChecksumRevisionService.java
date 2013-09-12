/**
 * 
 */
package com.softsec.tase.task.reloader;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.store.mongo.AndroidStaticResultService;
import com.softsec.tase.store.service.FileStorageService;

/**
 * AppBundleChecksumRevisionService
 * <p> fix md5 error in android static analysis result</p>
 * @author yanwei
 * @since 2013-6-11 下午7:03:43
 * @version
 * @deprecated
 */
public class AppBundleChecksumRevisionService implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AppBundleChecksumRevisionService.class);
	
	private static FileStorageService fileStorageService = new FileStorageService();
	
	private static AndroidStaticResultService androidStaticResultService = new AndroidStaticResultService();

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			List<String> bundleChecksumList = fileStorageService.getBundleChecksumListByFileStatus(AppType.APK, 1, 0, 0, 20000);
			if (bundleChecksumList != null && bundleChecksumList.size() != 0) {
				for (String bundleChecksum : bundleChecksumList) {
					String faultBundleChecksum = null;
					int retValue = 0;
					try {
						faultBundleChecksum = StringUtils.md5Encode(bundleChecksum);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					retValue = androidStaticResultService.updateBundleChecksumByBundleChecksum(faultBundleChecksum, bundleChecksum);
					List<String> fileChecksumList = fileStorageService.getFileChecksumByBundleChecksum(AppType.APK, bundleChecksum);
					if (fileChecksumList != null && fileChecksumList.size() != 0) {
						if (retValue != 0) {
							fileStorageService.updateFileStatusByFileChecksum(AppType.APK, fileChecksumList, 0, 1);
							LOGGER.info("Revise bundle checksum [ " + faultBundleChecksum + " ] to [ " + bundleChecksum + " ] of file : " + fileChecksumList);
						} else {
							LOGGER.info("No file checksum found of bundle checksum : " + bundleChecksum);
						}
					}
				}
				bundleChecksumList.clear();
			} else {
				LOGGER.info("No static result need to be revise ... Done.");
				Thread.currentThread().interrupt();
			}
		}
	}

}
