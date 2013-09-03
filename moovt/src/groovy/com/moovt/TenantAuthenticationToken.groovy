package com.moovt

import java.util.Locale;

/**
 * 
 * This is an extension to the <code>UsernamePasswordAuthenticationToken</code> to add the tenantName and locale required by the
 * authentication process. 
 * 
 */

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class TenantAuthenticationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1

	final String tenantName


	TenantAuthenticationToken(principal, credentials, String tenantName) {
		super(principal, credentials)
		this.tenantName = tenantName
	} 

	TenantAuthenticationToken(principal, credentials, String tenantName, Collection authorities) {
		super(principal, credentials, authorities)
		this.tenantName = tenantName
	}
}
