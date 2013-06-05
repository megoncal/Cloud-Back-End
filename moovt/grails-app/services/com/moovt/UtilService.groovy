package com.moovt

import grails.validation.ValidationErrors
import grails.validation.ValidationException

import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.servlet.support.RequestContextUtils

import grails.converters.*

class UtilService {

	def messageSource;
	
	public CallResult getCallResultFromErrors (ValidationErrors errors, Locale locale = Locale.US) {
						
				Boolean userError = false;
				String message ="";
				for (error in errors.getAllErrors()) {
						
						//Uniqueness can only be determined in the server and is a USER error
						if (error.code.contains('unique')){
							userError = true;
						}
						message = messageSource.getMessage(error, locale);
				}
				if (userError) {
					return new CallResult(CallResult.USER,CallResult.ERROR,message);
				} else {
					return new CallResult(CallResult.SYSTEM,CallResult.ERROR,message);
				}
			}
	
	public void handleSuccessWithSessionId(String msg, String sessionId) {
		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", \"additionalInfo\":" + ([JSESSIONID: sessionId] as JSON) + "}";
		writeToResponse(responseString);
	 
	}

	public void handleSuccessWithSessionIdAndUserType(String msg, String sessionId, String userType) {
		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", \"additionalInfo\":" + ([JSESSIONID: sessionId, userType: userType] as JSON) + "}";
		writeToResponse(responseString);
	 
	}

	public void handleSuccess(String msg) {
		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + "}";
		writeToResponse(responseString);
	 
	}
	
	public void handleUserError(String msg) {
		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.ERROR, msg).encodeAsJSON() + "}";
		writeToResponse(responseString);
	}
	
	public void handleSystemError(String msg) {
		String responseString = "{\"result\":" + new CallResult(CallResult.SYSTEM, CallResult.ERROR, msg).encodeAsJSON() + "}";
		writeToResponse(responseString);
	}

	private writeToResponse(String responseString) {
		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
		HttpServletResponse response = webUtils.getCurrentResponse();

		//Send the string to response
		response << responseString;
		response.flushBuffer()
	}
	
	public void handleException(Throwable e) {
			String responseString;
			if (e instanceof OptimisticLockingFailureException) {
			     responseString = new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')).getJSON();
			} else if (e instanceof ValidationException) {
				def webUtils = WebUtils.retrieveGrailsWebRequest();
				Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
				responseString = getCallResultFromErrors (e.getErrors(), locale).getJSON();
			} else if (e instanceof BadDataAuthenticationException) {
				responseString = new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
			} else if (e instanceof TenantUserPasswordAuthenticationException) {
				responseString = new CallResult(CallResult.USER,CallResult.ERROR,e.message).getJSON();
			} else {
				responseString = new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
			}

			//Print the stacktrace 
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			log.error (sw.toString());
			
			writeToResponse(responseString);
		 
	}
}
 
