package com.softsec.tase.task.customer;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.task.exception.DistributionException;
import com.softsec.tase.task.pool.JobMapper;


/**
 * task distributor customer thread 
 * @author yanwei
 */
public class TaskDistributorThread implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDistributorThread.class);
	
	private final BlockingQueue<Task> queue;

	private TaskDistributor taskDistributor;

	public TaskDistributorThread(BlockingQueue<Task> queue, TaskDistributor taskDistributor) {
		this.queue = queue;
		this.taskDistributor = taskDistributor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Task distributor thread [ " + Thread.currentThread().getName() + " ] started ...");
		Task task = null;
		TaskStorageService taskStorageService = new TaskStorageService();
		while (!Thread.currentThread().isInterrupted()) {
			try {
				task = queue.take();
				if (task != null) {
					
					// distribute to node
					taskDistributor.distributeToNode(task);
					
					// update task status to JobStatus.ISSUED
					try {
						taskStorageService.updateTaskStatus(task.getTaskId(), task.getJobPhase(), JobStatus.ISSUED);
						taskStorageService.updateTaskTimestamp(task.getTaskId(), task.getJobPhase(), 0, System.currentTimeMillis(), 0, 0);
						LOGGER.info("Succeed to update task [ " + task.getTaskId()
								+ " ] at [ " + task.getJobPhase() + " ] to [ " + JobStatus.ISSUED + " ]. ");
					} catch (DbUtilsException due) {
						LOGGER.error("Failed to update task [ " + task.getTaskId()
								+ " ] at [ " + task.getJobPhase() + " ] to [ " + JobStatus.ISSUED + " ]. ");
					}
					
					// add task to task monitor map
					// JobMapper.getInstance().addTaskMonitoring(task.getTaskId(), task.getTaskStatus());
					
				} else {
					LOGGER.info("Task Distribute Queue is EMPTY.");
				}
				
			} catch (InterruptedException ie) {
				LOGGER.error("Distribute task queue interrupted : " + ie.getMessage(), ie);
			} catch (DistributionException de) {
				LOGGER.error("Failed to distribute task [ " + task.getTaskId() 
						+ " ] to Node [ " + task.getExecutorId() + " ], " + de.getMessage(), de);
				// update task status to JobStatus.ISSUED_FAILED
				try {
					taskStorageService.updateTaskStatus(task.getTaskId(), task.getJobPhase(), JobStatus.ISSUE_FAILED);
				} catch (DbUtilsException due) {
					LOGGER.error("Failed to update task [ " + task.getTaskId()
							+ " ] at [ " + task.getJobPhase() + " ] to [ " + JobStatus.ISSUE_FAILED + " ]. ");
				}
			}
		}
		taskStorageService = null;
	}
}