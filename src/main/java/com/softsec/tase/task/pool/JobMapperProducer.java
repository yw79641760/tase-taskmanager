/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.store.domain.JobTypeItem;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.JobStorageService;

/**
 * JobMapperProducer.java
 * @author yanwei
 * @date 2013-3-13 上午9:35:09
 * @description
 */
public class JobMapperProducer implements Runnable{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JobMapperProducer.class);

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Start initializing job mapper ...");
		
		List<JobTypeItem> jobTypeItemList = null;
		JobStorageService jobStorageService = new JobStorageService();
		try {
			jobTypeItemList = jobStorageService.getJobTypeList(NodeMapper.getInstance().getMasterId());
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to init job mapper : " + due.getMessage(), due);
			System.exit(-1);
		}
		
		// build new job mapper
		if (jobTypeItemList != null && jobTypeItemList.size() != 0) {
			for (JobTypeItem jobTypeItem : jobTypeItemList) {
				JobMapper.getInstance().initJobCountMap(jobTypeItem.getJobType(), jobTypeItem.getJobCount());
			}
		}
		
		// finish initialize job mapper
		jobStorageService = null;
		LOGGER.info("Finished initializing job mapper : " + JobMapper.getInstance().getJobCountMap().toString());
	}

}
