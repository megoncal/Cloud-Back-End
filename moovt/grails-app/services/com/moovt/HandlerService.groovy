package com.moovt

import grails.validation.ValidationErrors
import grails.validation.ValidationException

import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.web.util.WebUtils

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError
import org.springframework.web.servlet.support.RequestContextUtils

import com.fasterxml.jackson.databind.ObjectMapper

import grails.converters.*

class HandlerService {

	def grailsApplication
	def messageSource

	public ObjectError getFirstError (ValidationErrors errors) {

		log.info(errors.toString());
		ObjectError fieldError = errors.getFieldError();
		ObjectError globalError = errors.getGlobalError();
		if (globalError) {
			return globalError;
		} else {
			return fieldError;
		}
	}

	//	public void handleSuccess(String msg) {
	//		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + "}";
	//		writeToResponse(responseString);
	//	}

	public void handleSuccess(String code, Object[] args, String additionalInfo = "") {
		String msg = messageSource.getMessage(code ,args, getLocale());
		String responseString;
		if (additionalInfo.equals ("")) {
			responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + "}";
		} else {
			responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", " + additionalInfo + "}";
		}
		writeToResponse(responseString);
	}


	//	public void handleSuccessWithSessionId(String msg, String sessionId) {
	//		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", \"additionalInfo\":" + ([JSESSIONID: sessionId] as JSON) + "}";
	//		log.info("TEST" + responseString);
	//		writeToResponse(responseString);
	//
	//	}

	//	public String successWithSessionId(String msg, String sessionId) {
	//		String test = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", \"additionalInfo\":" + ([JSESSIONID: sessionId] as JSON) + "}";
	//	 	log.info("HERE" + test);
	//		 return test;
	//	}

//	public void handleSuccessWithSessionId(String code, Object[] args, Locale locale, String sessionId) {
//		String msg = messageSource.getMessage(code ,args, locale);
//		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", \"additionalInfo\":" + ([JSESSIONID: sessionId] as JSON) + "}";
//		writeToResponse(responseString);
//	}

	//	public void handleSuccessWithSessionIdAndUserType(String msg, String sessionId, String userType) {
	//		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON()  + ", \"additionalInfo\":" + ([JSESSIONID: sessionId, userType: userType] as JSON) + "}";
	//		writeToResponse(responseString);
	//
	//	}

//	public void handleSuccessWithSessionIdAndUserType(String code, Object[] args, Locale locale, String sessionId, String userType) {
//		String msg = messageSource.getMessage(code ,args, locale);
//		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON()  + ", \"additionalInfo\":" + ([JSESSIONID: sessionId, userType: userType] as JSON) + "}";
//		writeToResponse(responseString);
//	}


	//	public void handleUserError(String msg) {
	//		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.ERROR, msg).encodeAsJSON() + "}";
	//		writeToResponse(responseString);
	//	}
	//
	//	public void handleSystemError(String msg) {
	//		String responseString = "{\"result\":" + new CallResult(CallResult.SYSTEM, CallResult.ERROR, msg).encodeAsJSON() + "}";
	//		writeToResponse(responseString);
	//	}

	public void handleUserError(String code, Object[] args) {
		String msg = messageSource.getMessage(code ,args, getLocale());
		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.ERROR, msg).encodeAsJSON() + "}";
		writeToResponse(responseString);
	}

	public void handleUserError(ObjectError error) {
		String msg = messageSource.getMessage(error, getLocale());
		String responseString = "{\"result\":" + new CallResult(CallResult.USER, CallResult.ERROR, msg).encodeAsJSON() + "}";
		writeToResponse(responseString);
	}

	public void handleSystemError(String msg) {
		String responseString = "{\"result\":" + new CallResult(CallResult.SYSTEM, CallResult.ERROR, msg).encodeAsJSON() + "}";
		writeToResponse(responseString);
	}


	//	public void handleSystemError(String code, Object[] args, Locale locale) {
	//		log.info("FINALLY");
	//		Object[] ddd = ['xxx','yyyyyy']
	//		String msg = messageSource.getMessage(code ,args, locale);
	//		String responseString = "{\"result\":" + new CallResult(CallResult.SYSTEM, CallResult.ERROR, msg).encodeAsJSON() + "}";
	//		writeToResponse(responseString);
	//	}

	private writeToResponse(String responseString) {
		def webUtils = WebUtils.retrieveGrailsWebRequest();
		HttpServletResponse response = webUtils.getCurrentResponse();

		//		RequestContextUtils.get
		//		response.setCharacterEncoding("UTF-8");
		//		response.setLocale(locale);

		//response.setContentType("application/json");

		//Send the string to response
		response << responseString;
		response.flushBuffer()
	}

	private Locale getLocale() {
		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
		return locale;
	}

	public void handleException(Errors errors) {
		CallResult cr = getCallResultFromErrors (errors);
		handleSystemError(cr.message);
	}

	public void handleException(Throwable e) {
		String responseString;
		if (e instanceof OptimisticLockingFailureException) {
			responseString = handleUserError('com.moovt.concurrent.update',null);
		} else if (e instanceof ValidationException) {
			def webUtils = WebUtils.retrieveGrailsWebRequest();
			ObjectError error = getFirstError (e.getErrors());
			handleUserError(error);
		} else {
			log.error ("A generic exception occurred " + e.message);
			handleSystemError(e.message);
			boolean printStackTrace = grailsApplication.config.moovt.display.stacktrace;
			if (printStackTrace) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw, true);
				e.printStackTrace(pw);
				pw.flush();
				sw.flush();
				log.error (sw.toString());
			}
		}

	}


}

