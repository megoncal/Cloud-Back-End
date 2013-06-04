/**
 * 
 */
package com.moovt

/**
 * @author egoncalves
 *
 */
class AuditAttributtesException extends Exception {

	/**
	 * 
	 */
	public AuditAttributtesException() {
		
	}

	/**
	 * @param message
	 */
	public AuditAttributtesException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AuditAttributtesException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AuditAttributtesException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AuditAttributtesException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
