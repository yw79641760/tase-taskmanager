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
public class ResultException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2208095564324262036L;

	public ResultException() {
		super();
	}
	
	public ResultException(String msg) {
		super(msg);
	}
	
	public ResultException(Throwable cause) {
		super(cause);
	}
	
	public ResultException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
