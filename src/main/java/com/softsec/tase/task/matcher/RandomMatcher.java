/**
 * 
 */
package com.softsec.tase.task.matcher;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.task.exception.ResourceException;

/**
 * 
 * @author yanwei
 * @date 2013-1-9 下午1:59:33
 * 
 */
public class RandomMatcher extends ResourceMatcher {

	/* (non-Javadoc)
	 * @see com.softsec.tase.task.matcher.ResourceMatcher#getMatchedNode(com.softsec.tase.task.domain.Task)
	 */
	@Override
	public String getMatchedNode(Task task, JobResourceRequirement request) throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

}
