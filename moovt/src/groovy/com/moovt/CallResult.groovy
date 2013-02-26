package com.moovt

import grails.validation.ValidationErrors
import org.codehaus.groovy.grails.commons.ApplicationHolder

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


	public CallResult(String type, String code, String message) {
		super();
		this.type = type;
		this.code = code;
		this.message = message;
	}

	public CallResult() {
		// TODO Auto-generated constructor stub


	}

	public static getCallResultFromErrors (ValidationErrors errors, Locale locale = Locale.ENGLISH) {

		MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource')
		String message ="";
		for (error in errors.getAllErrors()) {
				message = messageSource.getMessage(error, locale)
		}

		return new CallResult(CallResult.USER,CallResult.ERROR,message);
	}

}
