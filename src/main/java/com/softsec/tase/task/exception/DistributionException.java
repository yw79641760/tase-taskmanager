/**
 * 
 */
package com.softsec.tase.task.exception;

/**
 * 
 * @author yanwei
 * @date 2013-1-14 下午2:46:05
 * 
 */
public class DistributionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1916287783477709436L;

	public DistributionException() {
		super();
	}

	public DistributionException(String msg) {
		super(msg);
	}
	
	public DistributionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public DistributionException(Throwable cause) {
		super(cause);
	}
}
