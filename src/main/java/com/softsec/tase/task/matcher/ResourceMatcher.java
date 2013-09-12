/**
 * 
 */
package com.softsec.tase.task.matcher;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.util.domain.ProgramUtils;
import com.softsec.tase.store.Configuration;
import com.softsec.tase.store.domain.NodeItem;
import com.softsec.tase.task.Constants;
import com.softsec.tase.task.exception.ResourceException;
import com.softsec.tase.task.pool.ProgramMapper;

/**
 * 
 * @author yanwei
 * @date 2013-1-9 上午9:23:59
 * @description 
 * 
 */
public abstract class ResourceMatcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMatcher.class);
	
	public abstract String getMatchedNode(Task task, JobResourceRequirement requirement) throws ResourceException;
	
	/**
	 * get appropriate program
	 * @param task
	 * @return
	 * @throws ResourceException
	 */
	public long getMatchedProgram(Task task, JobResourceRequirement requirement) throws ResourceException {
		
		// parse resource request first
		if (requirement != null) {
			long programId = requirement.getProgramId();
			if (programId != 0L) {
				LOGGER.info("Request to schedule task [ " + task.getTaskId() + " ] with program id [ " + programId + " ].");
				return programId;
			}
		}
		
		int programType = ProgramUtils.getProgramType(task.getTaskId(), task.getJobPhase());
		// prefer to the latest program
		int programCount = ProgramMapper.getInstance().getProgramCount(programType);
		if (programCount == 0) {
			throw new ResourceException("No such program type matched : " + programType);
		}
		
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append(programType);
		sbuilder.append(new DecimalFormat("00").format(programCount));
		return Long.parseLong(sbuilder.toString());
	}
	
	public NodeItem updateNodeItemPayload(NodeItem nodeItem, int taskNum) throws ResourceException {
		nodeItem.setQueueNum(nodeItem.getQueueNum() + taskNum);
		nodeItem.updateGrade();
		return nodeItem;
	}
}
