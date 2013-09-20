package com.moovt.audit

import org.springframework.security.core.AuthenticationException

class TenantUserPasswordAuthenticationException extends AuthenticationException {

	public TenantUserPasswordAuthenticationException(String msg, Object extraInformation) {
		super(msg, extraInformation);
	}

	public TenantUserPasswordAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	} 

	public TenantUserPasswordAuthenticationException(String msg) {
		super(msg);
	}

	public TenantUserPasswordAuthenticationException() {
		super('Tenant/Username/Password invalid');
	}
	
}
