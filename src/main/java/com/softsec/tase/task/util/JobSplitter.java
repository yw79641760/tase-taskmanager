/**
 * 
 */
package com.softsec.tase.task.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.softsec.tase.common.domain.schedule.Job;
import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobDistributionMode;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobOperationRequirement;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.util.domain.JobUtils;

/**
 * JobSplitter.java
 * @author yanwei
 * @date 2013-3-22 下午1:14:05
 * @description
 */
public class JobSplitter {

	/**
	 * split job into task
	 * @param job
	 * @return
	 */
	public static List<Task> split(Job job) {
		
		if (job.getJobDistributionMode().equals(JobDistributionMode.PARALLEL)) {
			return splitParallelJob(job);
		} else {
			return splitSerialJob(job);
		}
	}
	
	/**
	 * split job into parallel task
	 * @param job
	 * @return taskList
	 */
	private static List<Task> splitParallelJob(Job job) {
		
		JobLifecycle targetJobLifecycle = JobUtils.getJobLifecycle(job.getJobId());
		List<JobPhase> jobPhaseList = JobUtils.getJobPhaseList(job.getJobId());
		List<Task> taskList = new ArrayList<Task>();
		
		if (jobPhaseList != null && jobPhaseList.size() != 0) {
			
			AtomicInteger taskCount = new AtomicInteger(0);
			for (JobPhase targetJobPhase : jobPhaseList) {
				
				JobOperationRequirement taskOperationRequirement = 
						getJobOperationRequirement(job, targetJobLifecycle, targetJobPhase);
				JobResourceRequirement taskResourceRequirement = 
						getJobResourceRequirement(job, targetJobLifecycle, targetJobPhase);
				List<JobParameter> targetJobParameterList =
						getJobParameter(job, targetJobPhase);
				
				if (targetJobParameterList != null && targetJobParameterList.size() != 0) {
					for (JobParameter jobParameter : targetJobParameterList) {
						
						Task task = new Task();
						// FIXME if parameter size multiple phase size is larger than 99, then failed
						task.setTaskId(Long.parseLong(String.valueOf(job.getJobId()) 
								+ new DecimalFormat("00").format(taskCount.incrementAndGet() % 100)));
						task.setTaskPriority(job.getJobPriority());
						task.setJobPhase(targetJobPhase);
						task.setTaskOperationRequirement(taskOperationRequirement);
						task.setTaskResourceRequirement(taskResourceRequirement);
						task.setTaskParameter(jobParameter);
						task.setTaskStatus(JobStatus.COMMITTED);
						task.setLoadedTime(job.getCommittedTime());
						
						taskList.add(task);
					}
				}
				
			}
		}
		return taskList;
	}

	/**
	 * split job into serial task
	 * @param job
	 * @return taskList
	 */
	private static List<Task> splitSerialJob(Job job) {
		
		JobLifecycle targetJobLifecycle = JobUtils.getJobLifecycle(job.getJobId());
		JobPhase targetJobPhase = JobUtils.getJobPhase(job.getJobStatus());
		List<Task> taskList = new ArrayList<Task>();
		
		JobOperationRequirement taskOperationRequirement = 
				getJobOperationRequirement(job, targetJobLifecycle, targetJobPhase);
		JobResourceRequirement taskResourceRequirement = 
				getJobResourceRequirement(job, targetJobLifecycle, targetJobPhase);
		List<JobParameter> targetJobParameterList =
				getJobParameter(job, targetJobPhase);
		
		if (targetJobParameterList != null && targetJobParameterList.size() != 0) {
			
			AtomicInteger taskCount = new AtomicInteger(0);
			for (JobParameter jobParameter : targetJobParameterList) {
				
				Task task = new Task();
				// FIXME if parameter size is larger than 99, then failed
				task.setTaskId(Long.parseLong(String.valueOf(job.getJobId()) 
						+ new DecimalFormat("00").format(taskCount.incrementAndGet() % 100)));
				task.setTaskPriority(job.getJobPriority());
				task.setJobPhase(targetJobPhase);
				task.setTaskOperationRequirement(taskOperationRequirement);
				task.setTaskResourceRequirement(taskResourceRequirement);
				task.setTaskParameter(jobParameter);
				task.setTaskStatus(JobStatus.COMMITTED);
				task.setLoadedTime(job.getCommittedTime());
				
				taskList.add(task);
			}
		}
		return taskList;
	}
	
	/**
	 * get task's jobParameter for specific jobPhase
	 * @param job
	 * @param targetJobPhase
	 * @return
	 */
	private static List<JobParameter> getJobParameter(Job job, JobPhase targetJobPhase) {
		List<JobParameter> jobParameterList = job.getJobParameterList();
		List<JobParameter> targetJobParameterList = new ArrayList<JobParameter>();
		if (jobParameterList != null && jobParameterList.size() != 0) {
			for (JobParameter jobParameter : jobParameterList) {
				if (jobParameter.getJobPhase().equals(targetJobPhase)) {
					targetJobParameterList.add(jobParameter);
				}
			}
		}
		return targetJobParameterList;
	}

	/**
	 * get task's jobResourceRequirement for specific jobPhase
	 * @param job
	 * @param targetJobLifecycle
	 * @param targetJobPhase
	 * @return
	 */
	private static JobResourceRequirement getJobResourceRequirement(Job job,
			JobLifecycle targetJobLifecycle, JobPhase targetJobPhase) {
		
		JobResourceRequirement taskResourceRequirement = null;
		if (job.getJobResourceRequirementList() != null && job.getJobResourceRequirementList().size() != 0) {
			for (JobResourceRequirement jobResourceRequirement : job.getJobResourceRequirementList()) {
				if (jobResourceRequirement.getJobPhase() != null
						&& jobResourceRequirement.getJobPhase().equals(targetJobPhase)) {
					taskResourceRequirement = jobResourceRequirement;
				}
			}
		}
		return taskResourceRequirement;
	}

	/**
	 * get task's jobOperationRequirement for specific jobPhase
	 * @param job
	 * @param targetJobLifecycle
	 * @param targetJobPhase
	 * @return
	 */
	private static JobOperationRequirement getJobOperationRequirement(Job job,
			JobLifecycle targetJobLifecycle, JobPhase targetJobPhase) {
		
		JobOperationRequirement taskOperationRequirement = null;
		if (job.getJobOperationRequirementList() != null && job.getJobOperationRequirementList().size() != 0) {
			for (JobOperationRequirement jobOperationRequirement : job.getJobOperationRequirementList()) {
				if (jobOperationRequirement.getJobPhase() != null
						&& jobOperationRequirement.getJobPhase().equals(targetJobPhase)) {
					taskOperationRequirement = jobOperationRequirement;
				}
			}
		}
		return taskOperationRequirement;
	}

}
