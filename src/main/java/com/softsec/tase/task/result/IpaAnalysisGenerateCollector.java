/**
 * 
 */
package com.softsec.tase.task.result;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.dto.app.ipa.Ipa;
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
public class IpaAnalysisGenerateCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IpaAnalysisGenerateCollector.class);
	
	private Long appId = null;
	
	private String appPath = null;
	
	private String appMd5 = null;

	private Ipa ipa = null;
	
	/**
	 * parse app id and app path and app md5
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#parse(java.lang.String, java.nio.ByteBuffer, java.lang.String)
	 */
	@Override
	public int parse(Result result) throws ResultException {
		
		int retValue = -1;
		try {
			ipa = (Ipa) IOUtils.getObject(result.getContent().array());
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to extract ipa info object from result byte array : " + result.getTaskId() + " : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to extract ipa info object from result byte array : " + result.getTaskId() + " : " + ioue.getMessage(), ioue);
		}
		
		this.appId = Long.parseLong(result.getIdentifier());
		if (ipa != null) {
			this.appPath = ipa.getFileMetadata().getFilePath();
			this.appMd5 = ipa.getExecutableMd5();
			retValue = 0;
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
//						if (ipa != null) {
//							retValue += service.addIpaInfo(appMd5, ipa);
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
//					// duplicate file , delete ipa file from ftp server
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
