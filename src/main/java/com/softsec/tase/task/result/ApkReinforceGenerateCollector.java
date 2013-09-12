/**
 * 
 */
package com.softsec.tase.task.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.service.OrderStorageService;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.task.exception.ResultException;

/**
 * ApkReinforceGenerateCollector
 * <p> </p>
 * @author yanwei
 * @since 2013-9-1 下午2:50:07
 * @version
 */
public class ApkReinforceGenerateCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkReinforceGenerateCollector.class);
	
	private Apk apk = null;
	
	private String sourceFileChecksum = null;

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultCollectorService#parse(com.softsec.tase.common.domain.Result)
	 */
	@Override
	public int parse(Result result) throws ResultException {
		
		int retValue = -1;
		
		try {
			apk = (Apk) IOUtils.getObject(result.getContent().array());
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to parse object apk from result [ " + result.getTaskId() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to parse object apk from result [ " + result.getTaskId() + " ] : " + ioue.getMessage(), ioue);
		}
		
		int lastSlashIndex = result.getIdentifier().lastIndexOf("/") == -1 ? 0 : result.getIdentifier().lastIndexOf("/") + 1;
		int lastDotIndex = result.getIdentifier().lastIndexOf(".") == -1 ? result.getIdentifier().length() : result.getIdentifier().lastIndexOf(".");
		sourceFileChecksum = result.getIdentifier().substring(lastSlashIndex, lastDotIndex);
		if (!StringUtils.isEmpty(sourceFileChecksum) 
				&& sourceFileChecksum.length() == 32) {
			retValue = 0;
		}
		
		return retValue;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultCollectorService#save()
	 */
	@Override
	public int save() throws ResultException {
		int retValue = 0;
		OrderStorageService orderStorageService = new OrderStorageService();
		try {
			retValue += orderStorageService.saveOutputApkResourceInfo(apk.getFileMetadata());
			retValue += orderStorageService.updateFileStatus(sourceFileChecksum, apk.getFileMetadata().getFileChecksum(), 1);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to save output file metadata [ " + apk.getFileMetadata().getFileName() + " ] : " + due.getMessage(), due);
			throw new ResultException("Failed to save output file metadata [ " + apk.getFileMetadata().getFileName() + " ] : " + due.getMessage(), due);
		}
		return retValue;
	}

}
