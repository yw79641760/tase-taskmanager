/**
 * 
 */
package com.softsec.tase.task.exception;

/**
 * 
 * @author yanwei
 * @date 2013-1-14 下午2:47:33
 * 
 */
public class SchedulingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2974319324679492040L;

	public SchedulingException() {
		super();
	}
	
	public SchedulingException(String msg) {
		super(msg);
	}
	
	public SchedulingException(Throwable cause) {
		super(cause);
	}
	
	public SchedulingException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
