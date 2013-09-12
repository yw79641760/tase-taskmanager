/**
 * 
 */
package com.softsec.tase.task.reloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobOperationRequirement;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobPriority;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.JobStorageService;
import com.softsec.tase.task.exception.ReloadException;
import com.softsec.tase.task.queue.TaskObservable;
import com.softsec.tase.task.queue.TaskObserver;

/**
 * ReloadService.java
 * @description
 * @todo
 * @author yanwei
 * @date 2013-4-10 上午11:39:18
 */
public abstract class TaskReloaderService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReloaderService.class);
	
	protected Task task = null;
	
	protected JobPhase newJobPhase = null;
	
	/**
	 * reload task operation & resource requirement from result
	 * @param taskId
	 * @param currentJobPhase
	 * @throws ReloadException
	 */
	public void reloadRequirement(Long taskId, JobPhase currentJobPhase) throws ReloadException {
		
		this.newJobPhase = JobUtils.getNextJobPhase(taskId, currentJobPhase);
		
		JobOperationRequirement taskOperationRequirement = null;
		JobResourceRequirement taskResourceRequirement = null;
		JobPriority taskPriority = null;
		JobStorageService jobStorageService = new JobStorageService();
		try {
			taskOperationRequirement = jobStorageService.getJobOperationRequirementByJobIdAndJobPhase(taskId / 100, newJobPhase);
			taskResourceRequirement = jobStorageService.getJobResourceRequirementByJobIdAndJobPhase(taskId / 100, newJobPhase);
			taskPriority = jobStorageService.getJobPriorityByJobId(taskId / 100);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to get job requirements of job : " 
					+ taskId / 100 + " : " + due.getMessage(), due);
			throw new ReloadException("Failed to get job requirements of job : " 
					+ taskId / 100 + " : " + due.getMessage(), due);
		} finally {
			jobStorageService = null;
		}
		
		task = new Task();
		
		if (taskOperationRequirement != null) {
			task.setTaskOperationRequirement(taskOperationRequirement);
		}
		if (taskResourceRequirement != null) {
			task.setTaskResourceRequirement(taskResourceRequirement);
		}
		if (taskPriority != null) {
			task.setTaskPriority(taskPriority);
		}
		
		
		task.setTaskId(taskId);
		task.setJobPhase(newJobPhase);
		task.setTaskStatus(JobStatus.COMMITTED);
		task.setLoadedTime(System.currentTimeMillis());
	}
	
	public abstract void reloadParameter(Result result) throws ReloadException;
	
	public void reloadTask() throws ReloadException {
		// TODO TaskObserver should not be multiple.
		TaskObservable taskObservable = new TaskObservable();
		TaskObserver taskObserver = new TaskObserver();
		taskObservable.addObserver(taskObserver);
		taskObservable.received(task);
	}
	
}
