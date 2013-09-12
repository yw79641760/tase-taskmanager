/**
 * 
 */
package com.softsec.tase.task.matcher;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.node.ClusterType;
import com.softsec.tase.common.rpc.domain.node.NodeType;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.common.util.domain.ProgramUtils;
import com.softsec.tase.store.domain.NodeItem;
import com.softsec.tase.store.domain.ProgramItem;
import com.softsec.tase.task.exception.ResourceException;
import com.softsec.tase.task.pool.NodeMapper;
import com.softsec.tase.task.pool.ProgramMapper;

/**
 * 
 * @author yanwei
 * @date 2013-1-9 涓��1:57:47
 * @description Strategy : load balancing
 */
public class LoadBalancingMatcher extends ResourceMatcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancingMatcher.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.matcher.ResourceMatcher#getMatchedNode(com.softsec.tase.task.domain.Task)
	 */
	@Override
	public String getMatchedNode(Task task, JobResourceRequirement requirement) throws ResourceException {
		
		String executorId = null;
		// parse resource requirement first
		if (requirement != null) {
			executorId = requirement.getExecutorId();
			if (!StringUtils.isEmpty(executorId)) {
				LOGGER.info("Request to schedule task [ " + task.getTaskId() + " ] with node [ "  + executorId + " ].");
				return executorId;
			}
		} 
		task.setProgramId(1100000301L);
		long programId = task.getProgramId();
		if (programId == 0L) {
			LOGGER.error("No program id scheduled for task : " + task.getTaskId());
			throw new ResourceException("No program id scheduled for task : " + task.getTaskId());
		}
		
		ProgramItem programItem = ProgramMapper.getInstance().getProgramItem(programId);
		if (programItem == null) {
			programItem = new ProgramItem();
			programItem.setProgramId(1100000301L);
			programItem.setNodeType(NodeType.BASIC.getValue());
		}
//		if (programItem == null) {
//			LOGGER.error("No such program found by program id : " + programId + " for task : " + task.getTaskId());
//			throw new ResourceException("No such program found by program id : " + programId + " for task : " + task.getTaskId());
//		}
		
		ConcurrentSkipListSet<NodeItem> selectedNodeSet = null;
		ClusterType clusterType = null;
		NodeItem nodeItem = null;
		if (requirement != null) {
			clusterType = requirement.getClusterType();
		}
		
		// if cluster type = DEDICATED, use DedicatedNodeMap<ProgramType, Set<Node>>
		if (clusterType != null && clusterType.equals(ClusterType.DEDICATED)) {
			
			int programType = ProgramUtils.getProgramType(programId);
			programType = 11000003;
			selectedNodeSet = NodeMapper.getInstance().getDedicatedNodeSet(programType);
			
			// sift high performance resource node which grade >= 0
			if (selectedNodeSet != null && selectedNodeSet.size() != 0) {
				NavigableSet<NodeItem> preferredNodeSet = selectedNodeSet.headSet(new NodeItem(0), true);
				if (preferredNodeSet != null && preferredNodeSet.size() != 0) {
					nodeItem = preferredNodeSet.first();
					if (nodeItem != null) {
						NodeMapper.getInstance().updateDedicatedNode(updateNodeItemPayload(nodeItem, 1));
					}
					preferredNodeSet = null;
				}
			}
		}
		
		// if DedicatedNodeMap is high loaded, or cluster type = GENERAL, use GeneralNodeMap<NodeType, Set<Node>>
		if (nodeItem == null || clusterType.equals(ClusterType.GENERAL)) {

			int nodeTypeCode = programItem.getNodeType();
			if (nodeTypeCode == 0) {
				LOGGER.error("Invalid node type of program : " + programItem.getProgramId());
				throw new ResourceException("Invalid node type of program : " + programItem.getProgramId());
			}
			
			selectedNodeSet = NodeMapper.getInstance().getGeneralNodeSet(nodeTypeCode);
			if (selectedNodeSet != null && selectedNodeSet.size() != 0) {
				// TODO
				NavigableSet<NodeItem> preferredNodeSet = selectedNodeSet.headSet(new NodeItem(0), true);
				nodeItem = selectedNodeSet.first();
				NodeMapper.getInstance().updateGeneralNode(updateNodeItemPayload(nodeItem, 1));
			} else {
				LOGGER.error("No appropriate node scheduled for task : " + task.getTaskId());
				throw new ResourceException("No appropriate node scheduled for task : " + task.getTaskId());
			}
		}
		
		if (nodeItem != null) {
			executorId = nodeItem.getNodeId();
		}
		programItem = null;
		clusterType = null;
		selectedNodeSet = null;
		nodeItem = null;
		
		return executorId;
	}
}
