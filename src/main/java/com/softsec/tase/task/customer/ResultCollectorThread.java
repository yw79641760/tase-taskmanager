/**
 * 
 */
package com.softsec.tase.task.customer;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.task.exception.ReloadException;
import com.softsec.tase.task.exception.ResultException;

/**
 * Result queue handler for single result
 * @author yanwei
 * @date 2013-1-22 下午1:44:49
 * 
 */
public class ResultCollectorThread implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollectorThread.class);
	
	private BlockingQueue<Result> queue;
	
	private ResultCollector resultCollector;
	
	private TaskReloader taskReloader;
	
	public ResultCollectorThread(BlockingQueue<Result> queue, ResultCollector resultCollector, TaskReloader taskReloader) {
		this.queue = queue;
		this.resultCollector = resultCollector;
		this.taskReloader = taskReloader;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		LOGGER.info("Result collector thread [ " + Thread.currentThread().getName() + " ] started ...");
		Result result = null;
		while(!Thread.currentThread().interrupted()) {
			try {
				result = queue.take();
				if (result != null) {
					// result handler for single result
					resultCollector.collect(result);
					taskReloader.reload(result);
				}
			} catch (InterruptedException ie) {
				// put result back into result queue
				if (!result.equals(queue.peek())) {
					queue.add(result);
				}
				Thread.currentThread().interrupt();
			} catch (ResultException rse) {
				LOGGER.error("Failed to collect task result [ " + result.getTaskId() + " ] : " + rse.getMessage(), rse);
				updateTaskStatusToFailure(result.getTaskId(), result.getResultType(), JobStatus.FAILURE);
			} catch (ReloadException rle) {
				LOGGER.error("Failed to reload task [ " + result.getTaskId() + " ] into global task queue.", rle);
			}
		}
	}

	/**
	 * update task status to be execution failure
	 * @param taskId
	 * @param jobPhase
	 */
	public void updateTaskStatusToFailure(Long taskId, JobPhase jobPhase, JobStatus jobStatus) {
		TaskStorageService taskStorageService = new TaskStorageService();
		try {
			taskStorageService.updateTaskStatus(taskId, jobPhase, jobStatus);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to update task [ " + taskId + " ] status to be " + 
					JobStatus.FAILURE + " : " + due.getMessage(), due);
		} finally {
			taskStorageService = null;
		}
	}
}
