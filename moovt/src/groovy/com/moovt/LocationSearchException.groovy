/**
 * 
 */
package com.moovt

/**
 * @author egoncalves
 *
 */
class LocationSearchException extends Exception {

	/**
	 * 
	 */
	public LocationSearchException() {
	}

	/**
	 * @param message
	 */
	public LocationSearchException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LocationSearchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LocationSearchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public LocationSearchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
