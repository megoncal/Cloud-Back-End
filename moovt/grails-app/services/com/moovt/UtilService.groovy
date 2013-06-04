package com.moovt

import grails.validation.ValidationErrors;

import java.util.Locale;

class UtilService {

	def messageSource;
	
	public CallResult getCallResultFromErrors (ValidationErrors errors, Locale locale = Locale.US) {
						
				String message ="";
				for (error in errors.getAllErrors()) {
						message = messageSource.getMessage(error, locale);
				}
				//The front end should perform validations
				return new CallResult(CallResult.SYSTEM,CallResult.ERROR,message);
			}
}
 
