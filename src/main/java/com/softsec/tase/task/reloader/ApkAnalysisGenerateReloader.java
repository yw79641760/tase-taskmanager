/**
 * 
 */
package com.softsec.tase.task.reloader;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.task.exception.ReloadException;

/**
 * ApkDefaultGenerateReloader.java
 * @description
 * @todo
 * @author yanwei
 * @date 2013-4-10 下午2:23:14
 */
public class ApkAnalysisGenerateReloader extends TaskReloaderService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkAnalysisGenerateReloader.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.reloader.TaskReloaderService#reloadParameter(java.lang.String)
	 */
	@Override
	public void reloadParameter(Result result) throws ReloadException {
		
		JobParameter taskParameter = new JobParameter();
		taskParameter.setJobPhase(newJobPhase);
		
		List<ContextParameter> parameterList = new ArrayList<ContextParameter>();
		ContextParameter parameter = new ContextParameter();
		parameter.setSequenceNum(1);
		parameter.setContent(((Apk)IOUtils.getObject(result.getContent().array())).getFileMetadata().getFilePath());
		parameter.setNeedDownload(true);
		
		parameterList.add(parameter);
		taskParameter.setContextParameterList(parameterList);
		task.setTaskParameter(taskParameter);
		
		LOGGER.info("Reload task [ " + result.getTaskId() + " ] 's parameter : " + task.getTaskParameter());
	}

}
