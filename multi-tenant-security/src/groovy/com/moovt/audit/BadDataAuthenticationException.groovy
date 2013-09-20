package com.moovt.audit

import org.springframework.security.core.AuthenticationException

class BadDataAuthenticationException extends AuthenticationException {

	public BadDataAuthenticationException(String msg, Object extraInformation) {
		super(msg, extraInformation);
	}

	public BadDataAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	} 

	public BadDataAuthenticationException(String msg) {
		super(msg);
	}

	
}