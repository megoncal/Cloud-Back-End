package com.moovt

import org.springframework.security.core.AuthenticationException

class TenantUserPasswordAuthenticationException extends AuthenticationException {

	public TenantUserPasswordAuthenticationException(String msg, Object extraInformation) {
		super(msg, extraInformation);
		// TODO Auto-generated constructor stub
	}

	public TenantUserPasswordAuthenticationException(String msg, Throwable t) {
		super(msg, t);
		// TODO Auto-generated constructor stub
	} 

	public TenantUserPasswordAuthenticationException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	
}
