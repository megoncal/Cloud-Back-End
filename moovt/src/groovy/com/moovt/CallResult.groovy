package com.moovt

import grails.validation.ValidationErrors
import org.apache.commons.logging.Log

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable

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

	protected static final Logger log = LoggerFactory.getLogger(getClass());

	public CallResult(String type, String code, String message) {
		super();
		this.type = type;
		this.code = code;
		this.message = message;
	}

	public CallResult() {

	}
	
	public String getJSON() {
		return "{\"result\":" + this.encodeAsJSON() + "}";
	}

	
}
