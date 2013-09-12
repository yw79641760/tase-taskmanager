/**
 * 
 */
package com.softsec.tase.task.pool;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.JobStorageService;

/**
 * JobMapper.java
 * @author yanwei
 * @date 2013-3-13 上午9:34:17
 * @description
 */
public class JobMapper {

	/**
	 * Map<JobType, JobCount>
	 */
	private Map<Long, AtomicInteger> jobCountMap	= new ConcurrentHashMap<Long, AtomicInteger>();
	
	/**
	 * Map<JobId, Map<TaskId, TaskStatus>>
	 */
	private Map<Long, Map<Long, JobStatus>> jobMonitorMap	= new ConcurrentHashMap<Long, Map<Long, JobStatus>>();
	
	private static final JobMapper jobCountMapper = new JobMapper();
	
	public JobMapper() {
	}
	
	public static JobMapper getInstance() {
		return jobCountMapper;
	}
	
	public synchronized Map<Long, AtomicInteger> getJobCountMap() {
		return jobCountMap;
	}
	
	public synchronized Map<Long, Map<Long, JobStatus>> getJobMonitorMap() {
		return jobMonitorMap;
	}
	
	/**
	 * init job count map
	 * @param jobType
	 * @param jobCount
	 */
	public synchronized void initJobCountMap(Long jobType, Integer jobCount) {
		if (jobCountMap.get(jobType) == null) {
			jobCountMap.put(jobType, new AtomicInteger(jobCount));
		}
	}
	
	/**
	 * add new job phase in mapper and database
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhaseCode
	 * @param masterId
	 */
	public synchronized void addJobType(AppType appType, JobLifecycle jobLifecycle, int jobPhaseCode, int masterId)
		throws DbUtilsException {
		long jobType = JobUtils.getJobType(appType, jobLifecycle, jobPhaseCode);
		
		if (jobCountMap.get(jobType) == null) {
			initJobCountMap(jobType, 0);
		
			// insert new job type into database
			new JobStorageService().addJobType(jobType, masterId);
		}
	}
	
	/**
	 * get job count
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhaseCode
	 * @return
	 */
	public synchronized int getJobCount(AppType appType, JobLifecycle jobLifecycle, int jobPhaseCode) {
		long jobType = JobUtils.getJobType(appType, jobLifecycle, jobPhaseCode);
		if (jobCountMap.get(jobType) != null) {
			return jobCountMap.get(jobType).get();
		} else {
			return 0;
		}
	}
	
	/**
	 * increase and get job phase count
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhaseCode
	 * @param masterId
	 * @return
	 */
	public synchronized int increaseAndGetJobCount(AppType appType, JobLifecycle jobLifecycle, int jobPhaseCode, int masterId) 
			throws DbUtilsException {
		addJobType(appType, jobLifecycle, jobPhaseCode, masterId);
		long jobType = JobUtils.getJobType(appType, jobLifecycle, jobPhaseCode);
		int jobCount = jobCountMap.get(jobType).incrementAndGet(); 

		// update job count in database
		new JobStorageService().updateJobCount(jobType, masterId, jobCount);
		return jobCount;
	}
	
	/**
	 * generate new job id
	 * <p> JOB ID FORMAT </p>
	 * | AppType(1 digit) | JobLifecycle(1 digit) | JobPhaseCode(6 digits) | MasterId(2 digits) | JobCount(6 digits) |
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhaseCode
	 * @return
	 */
	public synchronized long generateJobId(AppType appType, JobLifecycle jobLifecycle, int jobPhaseCode) 
			throws DbUtilsException {
		
		StringBuilder sbuilder = new StringBuilder();
		
		sbuilder.append(appType.getValue());
		sbuilder.append(jobLifecycle.getValue());
		sbuilder.append(new DecimalFormat("000000").format(jobPhaseCode));
		int masterId = NodeMapper.getInstance().getMasterId();
		sbuilder.append(new DecimalFormat("00").format(masterId));
		
		int jobCount = JobMapper.getInstance().increaseAndGetJobCount(appType, jobLifecycle, jobPhaseCode, masterId); 
		sbuilder.append(new DecimalFormat("000000").format(jobCount));
		
		return Long.parseLong(sbuilder.toString());
	}
	
	/**
	 * add task to task monitor map
	 * @param jobId
	 * @param taskId
	 * @param jobStatus
	 */
	public synchronized void addTaskMonitoring(long taskId, JobStatus jobStatus) {
		long jobId = taskId / 100;
		if (jobMonitorMap.get(jobId) == null) {
			ConcurrentHashMap<Long, JobStatus> taskMonitorMap = new ConcurrentHashMap<Long, JobStatus>();
			taskMonitorMap.put(taskId, jobStatus);
			jobMonitorMap.put(jobId, taskMonitorMap);
		} else {
			Map<Long, JobStatus> taskMonitorMap = jobMonitorMap.get(jobId);
			taskMonitorMap.put(taskId, jobStatus);
			jobMonitorMap.put(jobId, taskMonitorMap);
		}
	}

	/**
	 * @param taskId
	 * @param taskStatus
	 */
	public synchronized void updateTaskStatus(long taskId, JobStatus taskStatus) {
		long jobId = taskId / 100;
		if (jobMonitorMap.get(jobId) != null) {
			jobMonitorMap.get(jobId).put(taskId, taskStatus);
		}
	}
}
