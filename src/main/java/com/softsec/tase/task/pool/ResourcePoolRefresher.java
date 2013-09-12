/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.node.NodeType;
import com.softsec.tase.store.domain.NodeItem;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;

/**
 * NodeMapperRefresher.java
 * @author yanwei
 * @date 2013-3-22 下午6:11:37
 * @description
 */
public class ResourcePoolRefresher implements Runnable, StatefulJob{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcePoolRefresher.class);
	
	private static final long RESOURCE_REFRESH_INTERVAL = Configuration.getInt(Constants.RESOURCE_REFRESH_INTERVAL, 30000);

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		wipeOutObsoleteNodes();
	}

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		wipeOutObsoleteNodes();
	}

	public void wipeOutObsoleteNodes() {
		
		LOGGER.info("Start wiping out obsolete nodes in resource pool ...");
		
		Map<Integer, ConcurrentSkipListSet<NodeItem>> fixedNodeMap = NodeMapper.getInstance().getDedicatedNodeMap();
		
		for (Entry<Integer, ConcurrentSkipListSet<NodeItem>> fixedNodeEntry : fixedNodeMap.entrySet()) {
			for (NodeItem nodeItem : fixedNodeEntry.getValue()) {
				if (System.currentTimeMillis() - nodeItem.getUpdatedTime() > RESOURCE_REFRESH_INTERVAL) {
					fixedNodeEntry.getValue().remove(nodeItem);
					LOGGER.info("Removing node : " + nodeItem.getNodeId());
				} else {
					LOGGER.info("Valid dedicated node [ " + nodeItem.getNodeId() + " ] updated at [ " + new Date(nodeItem.getUpdatedTime()) + " ] ");
				}
			}
		}
		
		Map<NodeType, ConcurrentSkipListSet<NodeItem>> elasticNodeMap = NodeMapper.getInstance().getGeneralNodeMap();
		
		for (Entry<NodeType, ConcurrentSkipListSet<NodeItem>> elasticNodeEntry : elasticNodeMap.entrySet()) {
			for (NodeItem nodeItem : elasticNodeEntry.getValue()) {
				if (System.currentTimeMillis() - nodeItem.getUpdatedTime() > RESOURCE_REFRESH_INTERVAL) {
					elasticNodeEntry.getValue().remove(nodeItem);
					LOGGER.info("Removing node : " + nodeItem.getNodeId());
				} else {
					LOGGER.info("Valid general node [ " + nodeItem.getNodeId() + " ] updated at [ " + new Date(nodeItem.getUpdatedTime()) + " ] ");
				}
			}
		}
		
		LOGGER.info("Finished wiping out obsolete nodes in resource pool.");
	}
}
