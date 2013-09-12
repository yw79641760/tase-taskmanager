/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Job;
import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.task.util.JobSplitter;

/**
 * 
 * @author yanwei
 * @date 2013-1-16 上午9:23:54
 * 
 */
public class JobObserver implements Observer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JobObserver.class);

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object object) {
		Job job = (Job) object;
		List<Task> taskList = JobSplitter.split(job);
		LOGGER.info("Received new job [ " + job.getJobId() + " ], adding " + taskList.size() + " tasks to global task queue ...");
		TaskStorageService taskStorageService = new TaskStorageService();
		try {
			taskStorageService.saveAll(taskList);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to save task list from job [ " + job.getJobId() + " ] : " + due.getMessage());
		} finally {
			taskStorageService = null;
		}
		
		BlockingQueue<Task> globalTaskQueue = TaskQueue.getInstance().getGlobalTaskQueue();
		GlobalTaskQueueProducer globalMissionQueueProducer = new GlobalTaskQueueProducer(globalTaskQueue, taskList);
		Thread globalMissionQueueRunner = new Thread(globalMissionQueueProducer);
		globalMissionQueueRunner.start();
	}

}
