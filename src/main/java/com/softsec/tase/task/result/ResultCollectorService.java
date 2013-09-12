/**
 * 
 */
package com.softsec.tase.task.result;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.task.exception.ResultException;

/**
 * 
 * @author yanwei
 * @date 2013-1-18 下午2:05:15
 * 
 */
public abstract class ResultCollectorService {
	
	public abstract int parse(Result result) throws ResultException;
	
	public abstract int save() throws ResultException;
}
