/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.store.domain.ProgramItem;
import com.softsec.tase.store.domain.ProgramTypeItem;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.ProgramStorageService;

/**
 * ProgramMapperProducer.java
 * @author yanwei
 * @date 2013-3-13 上午9:35:49
 * @description only to fetch program mapper and is not responsible for program registration
 */
public class ProgramMapperProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramMapperProducer.class);
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		LOGGER.info("Start initializing program mapper ...");
		
		List<ProgramTypeItem> programTypeItemList = null;
		List<ProgramItem> programItemList = null;
		ProgramStorageService programStorageService = new ProgramStorageService();
		try {
			programTypeItemList = programStorageService.getProgramTypeList();
			programItemList = programStorageService.getProgramItemList();
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to get program list : " + due.getMessage(), due);
			System.exit(-1);
		}
		
		if (programTypeItemList != null && programTypeItemList.size() != 0) {
			for (ProgramTypeItem programTypeItem : programTypeItemList) {
				ProgramMapper.getInstance().initProgramCountMap(programTypeItem.getProgramType(), programTypeItem.getProgramCount());
			}
		}
		if (programItemList != null && programItemList.size() != 0) {
			for (ProgramItem programItem : programItemList) {
				ProgramMapper.getInstance().initProgramIdMap(programItem.getProgramId(), programItem);
			}
		}
		
		LOGGER.info("Finished initializing program mapper : " + ProgramMapper.getInstance().getProgramCountMap().toString());
		programStorageService = null;
	}

}
