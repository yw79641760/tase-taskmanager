/**
 * 
 */
package com.softsec.tase.task.exception;

/**
 * MapperException.java
 * @author yanwei
 * @date 2013-3-18 下午2:13:19
 * @description
 */
public class MapperException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 58254203652604714L;

	public MapperException() {
		super();
	}
	
	public MapperException(String msg) {
		super(msg);
	}
	
	public MapperException(Throwable cause) {
		super(cause);
	}
	
	public MapperException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
