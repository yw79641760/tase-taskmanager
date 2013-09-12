/**
 * 
 */
package com.softsec.tase.task.result;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.dto.app.FileMetadata;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.dto.app.apk.ApkManifest;
import com.softsec.tase.common.dto.app.apk.ApkSignature;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.exception.FtpUtilsException;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.service.AppStorageService;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.store.util.net.FtpConnFactory;
import com.softsec.tase.store.util.net.FtpUrlParser;
import com.softsec.tase.store.util.net.FtpUtils;
import com.softsec.tase.task.exception.ResultException;

/**
 * 
 * @author yanwei
 * @date 2013-1-16 上午10:46:51
 * 
 */
public class ApkAnalysisGenerateCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkAnalysisGenerateCollector.class);
	
	private long appId = 0L;
	
	private Apk apk = null;
	
	/**
	 * parse app id and app path and app md5
	 * @param result
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#parse(java.lang.String, java.nio.ByteBuffer, java.lang.String)
	 */
	@Override
	public int parse(Result result) throws ResultException {
		
		int retValue = -1;
		try {
			appId = Long.parseLong(result.getIdentifier());
		} catch (NumberFormatException nfe) {
			LOGGER.error("Failed to parse app id from result [ " + result.getTaskId() + " : " + result.getIdentifier() + " ] : " + nfe.getMessage(), nfe);
			throw new ResultException("Failed to parse app id from result [ " + result.getTaskId() + " : " + result.getIdentifier() + " ] : " + nfe.getMessage(), nfe);
		}
		
		if (appId != 0L) {
			try {
				apk = (Apk) IOUtils.getObject(result.getContent().array());
				String resultChecksum = IOUtils.getByteArrayMd5(IOUtils.getBytes(apk));
				if (!result.getMd5().equals(resultChecksum)) {
					LOGGER.error("Failed to match result checksum [ " + resultChecksum + " ], expected [ " + result.getMd5() + " ].");
					throw new ResultException("Failed to match result checksum [ " + resultChecksum + " ], expected [ " + result.getMd5() + " ].");
				} else {
					retValue = 0;
				}
			} catch (IOUtilsException ioue) {
				LOGGER.error("Failed to extract apk info object from result byte array : " + ioue.getMessage(), ioue);
				throw new ResultException("Failed to extract apk info object from result byte array : " + ioue.getMessage(), ioue);
			}
		}
		return retValue;
	}

	/**
	 * save app record into database
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#save()
	 */
	@Override
	public int save() throws ResultException {
		
		int retValue = 0;
		AppStorageService appStorageService = new AppStorageService();
//		if (appPath != null && appId != 0L && appMd5 != null) {
//			AppStorageService service = new AppStorageService();
//			try {
//				
//				retValue = service.updateAppDownloaded(appId, appMd5);
//				
//				if (service.checkFileDuplication(appMd5) == null) {
//					
//					// new md5 means new app downloaded
//					try {
//						retValue = service.updateAppFile(appMd5, appPath);
//						
//						if (apk != null) {
//							retValue += service.addApkInfo(appMd5, apk);
//						}
//						
//					} catch (DbUtilsException due) {
//						LOGGER.error("Failed to update app download info : " 
//								+ appId + " : " + appPath + " : " + appMd5 + " : " + due.getMessage(), due);
//						throw new ResultException("Failed to update app download info : "
//								+ appId + " : " + appPath + " : " + appMd5 + " : " + due.getMessage(), due);
//					}
//					
//				} else {
//					
//					// duplicate file , delete apk file from ftp server
//					try {
//						FtpUrlParser parser = new FtpUrlParser(appPath);
//						String filePath = parser.getPath() + parser.getFileName();
//						FTPClient ftpClient = FtpConnFactory.connect(appPath);
//						
//						if (FtpUtils.isFileExist(ftpClient, filePath)) {
//							FtpUtils.deleteFtpFile(ftpClient, filePath);
//						}
//					} catch (FtpUtilsException fue) {
//						LOGGER.error("Failed to delete duplicated ftp file : " + appPath + " : " + fue.getMessage(), fue);
//						throw new ResultException("Failed to delete duplicated ftp file : " + appPath + " : " + fue.getMessage(), fue);
//					}
//				}
//			} catch (DbUtilsException due) {
//				LOGGER.error("Failed to update app download info : " + due.getMessage(), due);
//				throw new ResultException("Failed to update app download info : " + due.getMessage(), due);
//			}
//		}
		return retValue;
	}

}
