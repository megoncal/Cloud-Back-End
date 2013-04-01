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
	final String locale


	TenantAuthenticationToken(principal, credentials, String tenantName , String locale = "en_US") {
		super(principal, credentials)
		this.tenantName = tenantName
		this.locale = locale  
	} 

	TenantAuthenticationToken(principal, credentials, String tenantName, Collection authorities, String locale = "en_US") {
		super(principal, credentials, authorities)
		this.tenantName = tenantName
		this.locale = locale
	}
}
