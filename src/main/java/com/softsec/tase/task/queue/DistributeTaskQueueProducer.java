/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.task.exception.ResourceException;
import com.softsec.tase.task.exception.SchedulingException;
import com.softsec.tase.task.matcher.ResourceMatcher;
import com.softsec.tase.task.matcher.ResourceMatcherFactory;
import com.softsec.tase.task.util.StringUtils;

/**
 * 
 * @author yanwei
 * @date 2013-1-6 涓��11:23:17
 * 
 */
public class DistributeTaskQueueProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DistributeTaskQueueProducer.class);

	private BlockingQueue<Task> queue;
	
	private volatile boolean stop = false;
	
	/**
	 * 
	 */
	public DistributeTaskQueueProducer(BlockingQueue<Task> queue) {
		this.queue = queue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Distribute task queue producer start ...");
		TaskStorageService taskStorageService = new TaskStorageService();
		while(!Thread.currentThread().isInterrupted() && !stop) {
			
			Task task = null;
			try {
				// get & remove global task queue 's head
				// waiting until queue is available (not empty)
				task = queue.take();
				if (task != null) {
					LOGGER.info("Scheduling for task [ " + task.getTaskId() + " ] ...");
					if (task.getTaskStatus().equals(JobStatus.SCHEDULED)
							&& task.getProgramId() != 0L
							&& !StringUtils.isEmpty(task.getExecutorId())) {
						
						// direct add scheduled task to distribute task queue
						addToDistributeTaskQueue(task);
					} else {
						
						// schedule task again, cleans other scheduling info
						task.setTaskStatus(JobStatus.COMMITTED);
						task.setProgramId(0L);
						task.setExecutorId(null);
						
						schedule(task);
						
						if (task.getTaskStatus().equals(JobStatus.SCHEDULED)) {
							addToDistributeTaskQueue(task);
							taskStorageService.saveTaskScheduling(task.getTaskId(), task.getJobPhase(), task.getProgramId(), task.getExecutorId());
							taskStorageService.updateTaskStatus(task.getTaskId(), task.getJobPhase(), task.getTaskStatus());
						}
					}
				} else {
					// if global task queue is empty, then do nothing.
					LOGGER.info("Global task queue is EMPTY.");
				}
			} catch (InterruptedException ie) {
				LOGGER.error("Distribute task queue producer interrupted : " + ie.getMessage(), ie);
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to update task [ " + task.getTaskId() + 
						" ] to [ " + task.getTaskStatus() + " ] : " + due.getMessage(), due);
			} catch (SchedulingException se) {
				// recycle task
				if (task.getSchedulingTime() < 5) {
					task.setSchedulingTime(task.getSchedulingTime() + 1);
					queue.add(task);
					LOGGER.error("Failed to schedule task [ " + task.getTaskId() + 
							" ] for " + task.getSchedulingTime() + " times , added it into global task queue again : " + se.getMessage(), se);
				} else {
					taskStorageService.updateTaskStatus(task.getTaskId(), task.getJobPhase(), JobStatus.SCHEDULING_FAILED);
					LOGGER.error("Failed to schedule task [ " + task.getTaskId() + 
							" ] for " + task.getSchedulingTime() + " times , giving up scheduling it at this time : " + se.getMessage(), se);
				}
			} catch (ResourceException re) {
				// recycle task
				if (task.getSchedulingTime() < 5) {
					task.setSchedulingTime(task.getSchedulingTime() + 1);
					queue.add(task);
					LOGGER.error("Failed to schedule task [ " + task.getTaskId() + 
							" ] for " + task.getSchedulingTime() + " times , added it into global task queue again : " + re.getMessage(), re);
				} else {
					taskStorageService.updateTaskStatus(task.getTaskId(), task.getJobPhase(), JobStatus.SCHEDULING_FAILED);
					LOGGER.error("Failed to schedule task [ " + task.getTaskId() + 
							" ] for " + task.getSchedulingTime() + " times , giving up scheduling it at this time : " + re.getMessage(), re);
				}
			}
		}
	}
	
	/**
	 * schedule single task
	 * @param task
	 * @throws SchedulingException
	 * @throws ResourceException 
	 */
	private void schedule(Task task) throws SchedulingException, ResourceException {
		
		if (task.getTaskStatus().equals(JobStatus.SCHEDULED)
				&& !StringUtils.isEmpty(task.getExecutorId())
				&& task.getProgramId() != 0L) {
			return;
		}
		
		ResourceMatcher resourceMatcher = ResourceMatcherFactory.getResourceMatcher();
		JobResourceRequirement taskResourceRequirement = task.getTaskResourceRequirement();
		
//		long programId = resourceMatcher.getMatchedProgram(task, taskResourceRequirement);
//		if (programId != 0L) {
//			task.setProgramId(programId);
//		}
		String nodeId = resourceMatcher.getMatchedNode(task, taskResourceRequirement);
//		if (!StringUtils.isEmpty(nodeId)) {
//			task.setExecutorId(nodeId);
//		}
//		
//		if (programId != 0L && !StringUtils.isEmpty(nodeId)) {
//			task.setTaskStatus(JobStatus.SCHEDULED);
//		} else {
//			LOGGER.error("Failed to schedule task : " + task.getTaskId()
//					+ " , scheduled program id [ " + programId + " ] and node id [ " + nodeId + " ].");
//			throw new SchedulingException("Failed to schedule task : " + task.getTaskId()
//					+ " , scheduled program id [ " + programId + " ] and node id [ " + nodeId + " ].");
//		}
		task.setExecutorId(nodeId);
		task.setProgramId(1100000301L);
		task.setTaskStatus(JobStatus.SCHEDULED);
	}
	
	/**
	 * add to distribute task queue exclusively
	 * @param task
	 */
	private void addToDistributeTaskQueue(Task task) {
		if (!TaskQueue.getInstance().getDistributeTaskQueue().contains(task)) {
			TaskQueue.getInstance().getDistributeTaskQueue().put(task);
			LOGGER.info("Task [ " + task.getTaskId() + " ] has been added into the distribute task queue.");
		}
	}

	/**
	 * shut down distribute task queue thread
	 */
	public synchronized void shutdown() {
		this.stop = true;
		Thread.currentThread().interrupt();
	}
}
