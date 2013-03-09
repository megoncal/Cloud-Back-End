package com.moovt

import grails.validation.ValidationErrors
import org.apache.commons.logging.Log
import org.codehaus.groovy.grails.commons.ApplicationHolder

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable

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
		// TODO Auto-generated constructor stub


	}
	
	public static getCallResultFromErrors (ValidationErrors errors, Locale locale = Locale.US) {
		
				MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource')
				
				String message ="";
				for (error in errors.getAllErrors()) {
						System.out.println("Errors are " + error);
						//message = message + ", " + messageSource.getMessage(error, locale)
						message = messageSource.getMessage(error, locale);
				}
		
				return new CallResult(CallResult.USER,CallResult.ERROR,message);
			}
}
