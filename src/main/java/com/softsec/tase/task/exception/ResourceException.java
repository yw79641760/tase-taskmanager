/**
 * 
 */
package com.softsec.tase.task.exception;

/**
 * 
 * @author yanwei
 * @date 2013-1-14 下午2:51:17
 * 
 */
public class ResourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2208095564324262036L;

	public ResourceException() {
		super();
	}
	
	public ResourceException(String msg) {
		super(msg);
	}
	
	public ResourceException(Throwable cause) {
		super(cause);
	}
	
	public ResourceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
