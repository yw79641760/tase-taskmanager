/**
 * 
 */
package com.softsec.tase.task.pool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.NodeStorageService;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;

/**
 * NodeMapperProducer.java
 * @author yanwei
 * @date 2013-3-16 上午10:47:41
 * @description
 */
public class NodeMapperProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeMapperProducer.class);

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Start initializing node mapper ...");
		
		initMasterId();
		
		// no need to fetch node info from database
		// because node heart beat will be pushed to task manager by nodes
	}
	
	/**
	 * initialize master id
	 */
	public void initMasterId() {
		
		// get master id from database
		String rpcUrl = Configuration.get(Constants.LISTENER_DOMAIN, "iscentos1")
				+ ":" + Configuration.getInt(Constants.TASK_CLIENT_SERVICE_PORT, 6010);
		NodeStorageService nodeStorageService = new NodeStorageService();
		int masterId = 0;
		try {
			masterId = nodeStorageService.generateMasterIdentifier(rpcUrl);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to get node [ " + rpcUrl + " ] master id : " + masterId + " : " + due.getMessage(), due);
			System.exit(-1);
		}
		
		nodeStorageService = null;
		if (masterId != 0) {
			NodeMapper.getInstance().initMasterId(masterId);
			LOGGER.info("Finished initializing node mapper, master node [ " + rpcUrl + " ] master id [ " + masterId + " ].");
		} else {
			LOGGER.error("Failed to get node [ " + rpcUrl + " ] master id : " + masterId);
			System.exit(-1);
		}
	}

}
