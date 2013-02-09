package com.moovt

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class TenantAuthenticationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1

	final String tenantName

	TenantAuthenticationToken(principal, credentials, String tName) {
		super(principal, credentials)
		tenantName = tName
	}

	TenantAuthenticationToken(principal, credentials, String tName, Collection authorities) {
		super(principal, credentials, authorities)
		tenantName = tName
	}
}
