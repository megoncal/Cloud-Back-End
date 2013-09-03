package com.moovt


/**
 * Encapsulates the results of a JSON call. It is used in the return of several JSON call to indicate the code, message and type of message (i.e. USER or SYSTEM).
 * Messages of type USER can be presented to the user and will be returned according to the Accept-Language HTTP header. Messages of type 
 * SYSTEM represents errors that should be handled by the front-end.
 * 
 *
 * @author egoncalves
 *
 */
class CallResult {

	String type
	String code
	String message
	static String ERROR = "ERROR"
	static String SUCCESS = "SUCCESS"
	static String USER = "USER"
	static String SYSTEM = "SYSTEM"

	public CallResult(String type, String code, String message) {
		super();
		this.type = type;
		this.code = code;
		this.message = message;
	}

	public CallResult() {

	}
	
//	public String getJSON() {
//		//return "{\"type\":\"" + this.type + "\",\"code\":\"" + this.code + "\",\"message\":\"" + this.message + "\"}";
//		return ([type: this.type, code: this.code, message: this.message] as JSON);
//	}	
}
