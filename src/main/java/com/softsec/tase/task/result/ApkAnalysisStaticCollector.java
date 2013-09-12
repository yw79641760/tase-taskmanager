/**
 * 
 */
package com.softsec.tase.task.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.domain.result.AndroidStaticResult;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.mongo.AndroidStaticResultService;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.task.exception.ResultException;

/**
 * 
 * @author yanwei
 * @date 2013-1-16 上午10:47:03
 * 
 */
public class ApkAnalysisStaticCollector extends ResultCollectorService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkAnalysisStaticCollector.class);
	
	private AndroidStaticResult androidStaticResult = null;

	/**
	 * parse result to android static result
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#parse(java.lang.String, java.nio.ByteBuffer, java.lang.String)
	 */
	@Override
	public int parse(Result result) throws ResultException {
		
		int retValue = -1;
		try {
			androidStaticResult = (AndroidStaticResult) IOUtils.getObject(result.getContent().array());
			retValue = 0;
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to extract android static result from bytes : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to extract android static result from bytes : " + ioue.getMessage(), ioue);
		}
		return retValue;
	}

	/**
	 * save android static result into mongodb
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultService#save()
	 */
	@Override
	public int save() throws ResultException {
		if (androidStaticResult != null) {
			AndroidStaticResultService androidStaticResultService = new AndroidStaticResultService();
			androidStaticResultService.insert(androidStaticResult);
		}
		return 0;
	}


}
