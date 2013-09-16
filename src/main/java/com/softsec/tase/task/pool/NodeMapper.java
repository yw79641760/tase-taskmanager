/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.softsec.tase.common.rpc.domain.node.NodeType;
import com.softsec.tase.store.domain.NodeItem;

/**
 * NodeMapper.java
 * @author yanwei
 * @date 2013-3-15 下午10:05:30
 * @description
 */
public class NodeMapper {
	
	/**
	 * master id 
	 */
	private AtomicInteger masterIdentifier = new AtomicInteger(0);
	
	/**
	 * Map<ProgramType, List<NodeItem>>
	 */
	private Map<Integer, ConcurrentSkipListSet<NodeItem>> dedicatedNodeMap
		= new ConcurrentHashMap<Integer, ConcurrentSkipListSet<NodeItem>>();
	
	/**
	 * Map<NodeType, List<NodeItem>>
	 */
	private Map<NodeType, ConcurrentSkipListSet<NodeItem>> generalNodeMap 
		= new ConcurrentHashMap<NodeType, ConcurrentSkipListSet<NodeItem>>();
	
	private static final NodeMapper nodeMapper = new NodeMapper();
	
	public NodeMapper() {
	}
	
	public static NodeMapper getInstance() {
		return nodeMapper;
	}
	
	public synchronized AtomicInteger getMasterIdentifier() {
		return masterIdentifier;
	}
	
	public synchronized Map<Integer, ConcurrentSkipListSet<NodeItem>> getDedicatedNodeMap() {
		return dedicatedNodeMap;
	}
	
	public synchronized Map<NodeType, ConcurrentSkipListSet<NodeItem>> getGeneralNodeMap() {
		return generalNodeMap;
	}
	
	/**
	 * init task manager id
	 * @param masterIdentifier
	 */
	public synchronized void initMasterId(int masterId) {
		masterIdentifier.set(masterId);
	}
	
	/**
	 * init dedicated node map with program type
	 * @param programType
	 */
	public synchronized ConcurrentSkipListSet<NodeItem> initDedicatedNodeMapWithProgramType(Integer programType) {
		if (dedicatedNodeMap.get(programType) == null) {
			ConcurrentSkipListSet<NodeItem> nodeSet = new ConcurrentSkipListSet<NodeItem>(); 
			dedicatedNodeMap.put(programType, nodeSet);
			return nodeSet;
		} else {
			return null;
		}
	}
	
	/**
	 * init dedicated node map with node item
	 * @param programType
	 * @param nodeItem
	 */
	public synchronized void initDedicatedNodeMapWithNodeItem(Integer programType, NodeItem nodeItem) {
		ConcurrentSkipListSet<NodeItem> nodeSet = dedicatedNodeMap.get(programType);
		if (nodeSet == null) {
			initDedicatedNodeMapWithProgramType(programType);
		} else if(!nodeSet.contains(nodeItem)){
			nodeSet.add(nodeItem);
		}
	}
	
	/**
	 * init general node map with node type
	 * @param nodeType
	 */
	public synchronized ConcurrentSkipListSet<NodeItem> initGeneralNodeMapWithNodeType(NodeType nodeType) {
		if(generalNodeMap.get(nodeType) == null) {
			ConcurrentSkipListSet<NodeItem> nodeSet = new ConcurrentSkipListSet<NodeItem>();
			generalNodeMap.put(nodeType, nodeSet);
			return nodeSet;
		} else {
			return null;
		}
	}
	
	/**
	 * init general node map with node type and node item
	 * @param nodeType
	 * @param nodeItem
	 */
	public synchronized void initGeneralNodeMapWithNodeItem(Set<NodeType> nodeTypeList, NodeItem nodeItem) {
		
		for (NodeType nodeType : nodeTypeList) {
			ConcurrentSkipListSet<NodeItem> nodeSet = generalNodeMap.get(nodeType);
			if(nodeSet == null) {
				initGeneralNodeMapWithNodeType(nodeType);
			} else if (!nodeSet.contains(nodeItem)) {
				nodeSet.add(nodeItem);
			}
		}
	}
	
	/**
	 * get task manager id
	 * @return masterId
	 */
	public synchronized int getMasterId() {
		return masterIdentifier.get();
	}
	
	/**
	 * update dedicated node
	 * @param programType
	 * @param nodeItem
	 */
	public synchronized void updateDedicatedNode(NodeItem nodeItem) {
		for (Integer programType : nodeItem.getPreferredProgramTypeList()) {
			ConcurrentSkipListSet<NodeItem> nodeSet = dedicatedNodeMap.get(programType);
			if (nodeSet == null) {
				nodeSet = initDedicatedNodeMapWithProgramType(programType);
			} else if (nodeSet.contains(nodeItem)) {
				nodeSet.remove(nodeItem);
			}
			
			if (nodeSet != null) {
				nodeSet.add(nodeItem);
			}
		}
	}
	
	/**
	 * get preferred node set by program type in dedicated nodes
	 * @param programType
	 * @return selectedNodeSet
	 */
	public synchronized ConcurrentSkipListSet<NodeItem> getDedicatedNodeSet(Integer programType) {
		ConcurrentSkipListSet<NodeItem> selectedNodeSet = dedicatedNodeMap.get(programType);
		if (selectedNodeSet != null) {
			// establish a new node set
			return new ConcurrentSkipListSet<NodeItem>(selectedNodeSet);
		}
		return selectedNodeSet;
	}
	
	/**
	 * update general node when received heart beat
	 * @param nodeType
	 * @param nodeItem
	 * @return nodeSet
	 */
	public synchronized void updateGeneralNode(NodeItem nodeItem) {
		
		for (NodeType nodeType : nodeItem.getNodeTypeSet()) {
			ConcurrentSkipListSet<NodeItem> nodeSet = generalNodeMap.get(nodeType);
			if (nodeSet == null) {
				nodeSet = initGeneralNodeMapWithNodeType(nodeType);
			} else if (nodeSet.contains(nodeItem)) {
				nodeSet.remove(nodeItem);
			}
			
			if (nodeSet != null) {
				nodeSet.add(nodeItem);
			}
		}
	}
	
	/**
	 * get preferred node set by node type in general nodes
	 * @param nodeType
	 * @return selectedNodeSet
	 */
	public synchronized ConcurrentSkipListSet<NodeItem> getGeneralNodeSet(int nodeType) {
		ConcurrentSkipListSet<NodeItem> selectedNodeSet = new ConcurrentSkipListSet<NodeItem>();
		for (NodeType type : generalNodeMap.keySet()) {
			if ((type.getValue() & nodeType) == type.getValue()) {
				// establish a new node set
				selectedNodeSet.addAll(new ConcurrentSkipListSet<NodeItem>(generalNodeMap.get(type)));
			}
		}
		return selectedNodeSet;
	}
}
