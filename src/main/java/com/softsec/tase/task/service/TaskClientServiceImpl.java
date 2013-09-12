/**
 * 
 */
package com.softsec.tase.task.service;

import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Job;
import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobDistributionMode;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobOperationRequirement;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobPriority;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.NotFoundException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.task.TaskClientService;
import com.softsec.tase.common.util.domain.AppUtils;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.JobStorageService;
import com.softsec.tase.task.pool.JobMapper;
import com.softsec.tase.task.queue.JobObservable;
import com.softsec.tase.task.queue.JobObserver;

/**
 * Task Client Service实现类
 * @author yanwei
 * @date 2012-12-27 上午11:23:25
 * 
 */
public class TaskClientServiceImpl implements TaskClientService.Iface{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskClientServiceImpl.class);
	
	/**
	 * check network connectivity manually
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.ClientService.Iface#ping()
	 */
	@Override
	public String ping() throws UnavailableException, TimeoutException, TException {
		return new Date().toString();
	}

	/**
	 * receive job submission from client
	 * @param userName
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhaseList
	 * @param jobDistributionMode
	 * @param jobOperationRequirement
	 * @param jobResourceRequirement
	 * @param priority
	 * @param parameters
	 * @param impatienceTime
	 * @return jobId
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.TaskClientService.Iface#submitJob(int, com.softsec.tase.rpc.domain.app.AppType, com.softsec.tase.rpc.domain.job.JobLifecycle, java.util.List, com.softsec.tase.rpc.domain.job.JobDistributionMode, com.softsec.tase.rpc.domain.job.JobPriority, java.util.List, java.util.List, java.util.List, long)
	 */
	@Override
	public long submitJob(int userId, AppType appType,
			JobLifecycle jobLifecycle, List<JobPhase> jobPhaseList,
			JobDistributionMode jobDistributionMode, JobPriority jobPriority,
			List<JobOperationRequirement> jobOperationRequirementList,
			List<JobResourceRequirement> jobResourceRequirementList,
			List<JobParameter> parameterList, long impatienceTime)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		if(!AppUtils.isAppTypeMember(appType)
				|| !JobUtils.isJobLifecycleMember(jobLifecycle)) {
			
			LOGGER.error("Invalid submission parameters : " + appType.name() + " : " + " : " + jobLifecycle.name());
			throw new InvalidRequestException("Invalid submission parameters : " + appType.name() + " : " + jobLifecycle.name());
		}
		Job job = new Job();
		try {
			job.setJobId(JobMapper.getInstance().generateJobId(appType, jobLifecycle, JobUtils.getJobPhaseCode(jobPhaseList)));
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to generate new job id and update database : " + due.getMessage(), due);
			throw new UnavailableException("Failed to generate new job id and update database : " + due.getMessage());
		}
		
		job.setSubmitterId(userId);
		job.setJobDistributionMode(jobDistributionMode);
		job.setJobPriority(jobPriority);
		job.setJobOperationRequirementList(jobOperationRequirementList);
		job.setJobResourceRequirementList(jobResourceRequirementList);
		job.setJobStatus(JobUtils.getJobStatusCode(jobPhaseList.get(0), JobStatus.COMMITTED));
		job.setJobParameterList(parameterList);
		job.setImpatienceTime(impatienceTime);
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
			throw new UnavailableException("Failed to save job into database : " + job + " : " + due.getMessage());
		} finally {
			jobStorageService = null;
		}
		
		return job.getJobId();
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.ClientService.Iface#obtainQueueInfo()
	 */
	@Override
	public String obtainQueueInfo() throws UnavailableException,
			TimeoutException, TException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.ClientService.Iface#obtainClusterStat()
	 */
	@Override
	public String obtainClusterStat() throws UnavailableException,
			TimeoutException, TException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.TaskClientService.Iface#terminateJobs(java.util.List)
	 */
	@Override
	public int terminateJobs(List<Long> jobIdList)
			throws InvalidRequestException, UnavailableException,
			NotFoundException, TimeoutException, TException {
		// TODO Auto-generated method stub
		return 0;
	}

}
