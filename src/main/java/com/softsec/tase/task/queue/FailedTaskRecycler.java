/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;

/**
 * DistributeFailureReloader.java
 * @author yanwei
 * @date 2013-1-28 上午8:51:13
 * @description
 */
public class FailedTaskRecycler implements StatefulJob{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FailedTaskRecycler.class);
	
	public FailedTaskRecycler() {
	}
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		recycleFailureTasks();
	}
	
	private void recycleFailureTasks() {
		
		List<JobStatus> taskStatusList = getRecycledTaskStatusList();
		
		if(taskStatusList.size() > 0) {
			LOGGER.info("Start to recycle failed tasks ...");
			
			Set<Task> taskSet = new HashSet<Task>();
			List<Task> taskItemList = null;
			List<Long> taskIdList = TaskQueue.getInstance().getGlobalTaskIdList();
			PriorityBlockingQueue<Task> globalTaskQueue = TaskQueue.getInstance().getGlobalTaskQueue();
			
			int limit = TaskQueue.getInstance().getMaxQueueSize() - globalTaskQueue.size();
			
			TaskStorageService service = new TaskStorageService();
			try {
				taskItemList = service.getTaskListByTaskStatusList(taskStatusList, limit);
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to recycle failure tasks : " + due.getMessage(), due);
				Thread.currentThread().interrupt();
			} finally {
				taskStatusList = null;
			}
			
			// task duplication removal
			if(taskItemList != null && taskItemList.size() != 0) {
				for (Task task : taskItemList) {
					if(taskIdList.contains(task.getTaskId())) {
						taskSet.add(task);
					}
				}
			}
			
			if (taskSet.size() != 0 && globalTaskQueue.addAll(taskSet)) {
				LOGGER.info("Succeed to recycle " + taskSet.size() + " distribute failure tasks.");
			} else {
				LOGGER.info("No failed tasks are recycled.");
			}
			taskSet = null;
		}
	}
	
	private List<JobStatus> getRecycledTaskStatusList() {
		
		
		boolean isSchedulingFailureRecycled = Configuration.getBoolean(Constants.SCHEDULING_FAILURE_RECYCLER_ENABLE, false);
		boolean isDistributionFailureRecycled = Configuration.getBoolean(Constants.DISTRIBUTION_FAILURE_RECYCLER_ENABLE, false);
		boolean isExecutionInterruptionRecycled = Configuration.getBoolean(Constants.EXECUTION_INTERRUPTION_RECYCLER_ENABLE, false);
		boolean isExecutionTimeoutRecycled = Configuration.getBoolean(Constants.EXECUTION_TIMEOUT_RECYCLER_ENABLE, false);
		boolean isExecutionFailureRecycled = Configuration.getBoolean(Constants.EXECUTION_FAILURE_RECYCLER_ENABLE, false);
		
		List<JobStatus> taskStatusList = new ArrayList<JobStatus>();
		if (isSchedulingFailureRecycled) {
			taskStatusList.add(JobStatus.SCHEDULING_FAILED);
		}
		if (isDistributionFailureRecycled) {
			taskStatusList.add(JobStatus.ISSUE_FAILED);
		}
		if (isExecutionInterruptionRecycled) {
			taskStatusList.add(JobStatus.INTERRUPTED);
		}
		if (isExecutionTimeoutRecycled) {
			taskStatusList.add(JobStatus.TIMEOUT);
		}
		if (isExecutionFailureRecycled) {
			taskStatusList.add(JobStatus.FAILURE);
		}
		
		return taskStatusList;
	}

}
