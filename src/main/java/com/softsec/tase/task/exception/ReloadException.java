/**
 * 
 */
package com.softsec.tase.task.exception;

/**
 * ReloadException.java
 * @author yanwei
 * @date 2013-3-27 下午4:20:48
 * @description
 */
public class ReloadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9102632545970286364L;

	public ReloadException() {
		super();
	}
	
	public ReloadException(String msg) {
		super(msg);
	}
	
	public ReloadException(Throwable cause) {
		super(cause);
	}
	
	public ReloadException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
