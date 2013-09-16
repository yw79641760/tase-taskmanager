/**
 * 
 */
package com.softsec.tase.task.mapper;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.store.domain.NodeItem;

/**
 * NodeMapperTest
 * <p> </p>
 * @author yanwei
 * @since 2013-9-12 下午2:37:33
 * @version
 */
public class NodeMapperTest extends TestCase {

	@Test
	public void testConcurrentSkipListSet() {
		
		ConcurrentSkipListSet<NodeItem> dedicatedNodeSet = new ConcurrentSkipListSet<NodeItem>();
		NodeItem node2 = new NodeItem(80);
		node2.setNodeId("192.168.100.51:7000");
		node2.setUpdatedTime(System.currentTimeMillis());
		NodeItem node1 = new NodeItem(100);
		node1.setNodeId("192.168.100.52:7000");
		dedicatedNodeSet.add(node1);
		dedicatedNodeSet.add(node2);
		NavigableSet<NodeItem> selectedNodeSet = dedicatedNodeSet.headSet(new NodeItem(99), true);
		for (NodeItem node : selectedNodeSet) {
			System.out.println(node);
		}
		NodeItem nodeTest = new NodeItem(100);
		nodeTest.setNodeId("192.168.100.51:7000");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nodeTest.setUpdatedTime(System.currentTimeMillis());
		System.out.println(dedicatedNodeSet.contains(nodeTest));
	}
}
