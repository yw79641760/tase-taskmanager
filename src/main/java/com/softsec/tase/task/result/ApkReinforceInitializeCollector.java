/**
 * 
 */
package com.softsec.tase.task.result;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.domain.schedule.Job;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobReinforceRequest;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.util.domain.AppUtils;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.service.JobStorageService;
import com.softsec.tase.store.service.OrderStorageService;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.task.exception.ResultException;
import com.softsec.tase.task.pool.JobMapper;
import com.softsec.tase.task.queue.JobObservable;
import com.softsec.tase.task.queue.JobObserver;

/**
 * ApkReinforceInitializeCollector
 * <p> </p>
 * @author yanwei
 * @since 2013-8-29 下午9:12:12
 * @version
 */
public class ApkReinforceInitializeCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkReinforceInitializeCollector.class);
	
	private JobReinforceRequest jobReinforceRequest = null;

	private Apk apk = null;
	
	private String orderId = null;
	
	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultCollectorService#parse(com.softsec.tase.common.domain.Result)
	 */
	@Override
	public int parse(Result result) throws ResultException {
		
		int retCode = -1;
		
		try {
			jobReinforceRequest = (JobReinforceRequest) IOUtils.getObject((result.getContent().array()));
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to get job reinforce request [ " + jobReinforceRequest.getAppPath() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to get job reinforce request [ " + jobReinforceRequest.getAppPath() + " ] : " + ioue.getMessage(), ioue);
		}
		
		try {
			apk = (Apk) IOUtils.getObject(jobReinforceRequest.getAppInfo());
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to get apk info [ " + apk.getFileMetadata().getFilePath() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to get apk info [ " + apk.getFileMetadata().getFilePath() + " ] : " + ioue.getMessage(), ioue);
		}
		
		orderId = result.getIdentifier();

		// update result task id to avoid reload process
		StringBuilder taskIdBuilder = new StringBuilder();
		taskIdBuilder.append(result.getAppType().getValue());
		taskIdBuilder.append(result.getJobLifecycle().getValue());
		// jobPhaseCode only include ONE phase
		// so there 's no the next phase
		taskIdBuilder.append(new DecimalFormat("000000").format(result.getResultType().getValue()));
		taskIdBuilder.append("00");
		taskIdBuilder.append("000000");
		result.setTaskId(Long.parseLong(taskIdBuilder.toString()));
		
		return retCode;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.result.ResultCollectorService#save()
	 */
	@Override
	public int save() throws ResultException {
		
		int retCode = -1;
		
		// generate job and insert it into global task queue
		long jobId = generateJob();
		
		// update order and job info in database
		if (jobId != 0L && !StringUtils.isEmpty(orderId)) {
			OrderStorageService orderStorageService = new OrderStorageService();
			try {
				retCode += orderStorageService.saveOrderAndJob(orderId, jobId);
				retCode += orderStorageService.saveInputApkInfo(apk);
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to save job and uploaded apk info [ " + apk.getFileMetadata().getFileName() + " ] : " + due.getMessage(), due);
				throw new ResultException("Failed to save job and uploaded apk info [ " + apk.getFileMetadata().getFileName() + " ] : " + due.getMessage(), due);
			}
		} else {
			LOGGER.error("Failed to generate job [ " + orderId + " : " + jobId + " ].");
			throw new ResultException("Failed to generate job [ " + orderId + " : " + jobId + " ].");
		}
		
		return retCode;
	}

	/**
	 * construct JobReinforceRequest from result
	 * @throws ResultException
	 */
	private long generateJob() throws ResultException {
		
		if(!AppUtils.isAppTypeMember(jobReinforceRequest.getAppType())
				|| !JobUtils.isJobLifecycleMember(jobReinforceRequest.getJobLifecycle())) {
			
			LOGGER.error("Invalid submission parameters : " + jobReinforceRequest.getAppType().name() + " : " + " : " + jobReinforceRequest.getJobLifecycle().name());
			throw new ResultException("Invalid submission parameters : " + jobReinforceRequest.getAppType().name() + " : " + jobReinforceRequest.getJobLifecycle().name());
		}
		Job job = new Job();
		try {
			job.setJobId(JobMapper.getInstance().generateJobId(jobReinforceRequest.getAppType(), 
																jobReinforceRequest.getJobLifecycle(), 
																JobUtils.getJobPhaseCode(jobReinforceRequest.getJobPhaseList())));
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to generate new job id and update database : " + due.getMessage(), due);
			throw new ResultException("Failed to generate new job id and update database : " + due.getMessage());
		}
		
		job.setSubmitterId(jobReinforceRequest.getUserId());
		job.setJobDistributionMode(jobReinforceRequest.getJobDistributionMode());
		job.setJobPriority(jobReinforceRequest.getJobPriority());
		job.setJobOperationRequirementList(jobReinforceRequest.getJobOperationRequirementList());
		job.setJobResourceRequirementList(jobReinforceRequest.getJobResourceRequirementList());
		job.setJobStatus(JobUtils.getJobStatusCode(jobReinforceRequest.getJobPhaseList().get(0), JobStatus.COMMITTED));
		job.setJobParameterList(jobReinforceRequest.getJobParameterList());
		// modify job parameter
		List<JobParameter> jobParameterList = jobReinforceRequest.getJobParameterList();
		List<ContextParameter> contextParameterList = new ArrayList<ContextParameter>();
		// parameter1 : source.apk
		ContextParameter parameter1 = new ContextParameter();
		parameter1.setSequenceNum(1);
		parameter1.setContent(apk.getFileMetadata().getFilePath());
		parameter1.setNeedDownload(true);
		contextParameterList.add(parameter1);
		// parameter2 : target.apk
		ContextParameter parameter2 = new ContextParameter();
		parameter2.setSequenceNum(2);
		parameter2.setContent("./target.apk");
		parameter2.setNeedDownload(false);
		contextParameterList.add(parameter2);
		
		if (jobParameterList != null && jobParameterList.size() != 0) {
			for (JobParameter jobParameter : jobParameterList) {
				if (jobParameter.getJobPhase().equals(JobPhase.GENERATE)) {
					List<ContextParameter> parameterList = jobParameter.getContextParameterList();
					if (parameterList != null && parameterList.size() != 0) {
						for (int index = 0; index < parameterList.size(); index++) {
							ContextParameter parameter = parameterList.get(index);
							parameter.setSequenceNum(index + 2);
							contextParameterList.add(parameter);
						}
					}
				}
			}
		}
		
		job.setImpatienceTime(jobReinforceRequest.getImpatienceTime());
		job.setCommittedTime(System.currentTimeMillis());
		
		JobObservable jobObservable = new JobObservable();
		JobObserver jobObserver = new JobObserver();
		jobObservable.addObserver(jobObserver);
		jobObservable.received(job);
		
		JobStorageService jobStorageService = new JobStorageService();
		try {
			jobStorageService.save(job);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to save job into database : " + job + " : " + due.getMessage(), due);
			throw new ResultException("Failed to save job into database : " + job + " : " + due.getMessage());
		} finally {
			jobStorageService = null;
		}
		
		return job.getJobId();
	}
}
