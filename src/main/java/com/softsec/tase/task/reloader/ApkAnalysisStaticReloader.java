/**
 * 
 */
package com.softsec.tase.task.reloader;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.task.exception.ReloadException;

/**
 * ApkDefaultStaticService.java
 * @description
 * @todo
 * @author yanwei
 * @date 2013-4-10 下午2:00:41
 */
public class ApkAnalysisStaticReloader extends TaskReloaderService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkAnalysisStaticReloader.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.reloader.ReloadService#reloadParameter()
	 */
	@Override
	public void reloadParameter(Result result) throws ReloadException {
		
		JobParameter taskParameter = new JobParameter();
		taskParameter.setJobPhase(newJobPhase);
		
		List<ContextParameter> parameterList = new ArrayList<ContextParameter>();
		ContextParameter parameter = new ContextParameter();
		
		parameter.setSequenceNum(1);
		StringBuilder sbuilder = new StringBuilder(result.getJobLifecycle().toString().toLowerCase() + "/");
		sbuilder.append(result.getIdentifier().substring(0, 2) + "/");
		sbuilder.append(result.getIdentifier().substring(2, 4) + "/");
		sbuilder.append(result.getIdentifier());
		parameter.setContent(sbuilder.toString());
		parameter.setNeedDownload(true);
		
		parameterList.add(parameter);
		taskParameter.setContextParameterList(parameterList);
		task.setTaskParameter(taskParameter);
		
		LOGGER.info("Reload task [ " + result.getTaskId() + " ] 's parameter : " + task.getTaskParameter());
	}
}
