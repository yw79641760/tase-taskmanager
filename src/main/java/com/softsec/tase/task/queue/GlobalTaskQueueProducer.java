/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.TaskStorageService;

/**
 * Global Task Queue Producer
 * 		thread for generating and keeping global task queue
 * @author yanwei
 * @date 2013-1-6 上午11:22:20
 * 
 */
public class GlobalTaskQueueProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTaskQueueProducer.class);
	
	private final BlockingQueue<Task> queue;
	
	private Task task;
	
	private List<Task> taskList;
	
	/**
	 * 
	 */
	public GlobalTaskQueueProducer(BlockingQueue<Task> queue) {
		this.queue = queue;
	}
	
	public GlobalTaskQueueProducer(BlockingQueue<Task> queue, Task task) {
		this.queue = queue;
		this.task = task;
	}
	
	public GlobalTaskQueueProducer(BlockingQueue<Task> queue, List<Task> taskList) {
		this.queue = queue;
		this.taskList = taskList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Global task queue producer start ...");
		Set<Task> taskSet = produce();
		
		if(taskSet != null && taskSet.size() != 0 && queue.addAll(taskSet)) {
			LOGGER.info("Succeed to update global task queue, size : [ " + queue.size() + " ].");
		} else {
			LOGGER.info("No task will be added to global task queue, size : [ " + queue.size() + " ].");
		}
		taskSet = null;
	}

	/**
	 * add missions to global mission queue which status is COMMITTED and SCHEDULED
	 * @return
	 */
	private Set<Task> produce() {
		
		Set<Task> taskSet = new HashSet<Task>();
		List<Task> previousTaskList = null;
		
		// get upper bound of task num to be added
		int limit = TaskQueue.getInstance().getMaxQueueSize();
		if (task != null) {
			limit = limit - queue.size() - 1;
			taskSet.add(task);
		} else if (taskList != null && taskList.size() != 0) {
			limit = limit - queue.size() - taskList.size();
			taskSet.addAll(taskList);
		}
		List<Long> taskIdList = TaskQueue.getInstance().getGlobalTaskIdList();
		LOGGER.info("The num of tasks can be added : [ " + limit + " ].");
		
		// get unfinished task in database
		TaskStorageService taskStorageService = new TaskStorageService();
		List<JobStatus> taskStatusList = new ArrayList<JobStatus>();
		taskStatusList.add(JobStatus.COMMITTED);
		taskStatusList.add(JobStatus.SCHEDULED);
		try {
			previousTaskList = taskStorageService.getTaskListByTaskStatusList(taskStatusList, limit);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to get tasks by status in producing global task queue : " + due.getMessage(), due);
		} finally {
			taskStorageService = null;
			taskStatusList = null;
		}
		
		// task duplication removal
		if (previousTaskList != null && previousTaskList.size() != 0) {
			for (Task task : previousTaskList) {
				if (!taskIdList.contains(task.getTaskId())) {
					taskSet.add(task);
				}
			}
		}
		
		LOGGER.info("The num of tasks to be added : [ " + taskSet.size() + " ].");
		previousTaskList = null;
		taskIdList = null;
		return taskSet;
	}

	/**
	 * 
	 */
	public void shutdown() {
		Thread.currentThread().interrupt();
	}
}
