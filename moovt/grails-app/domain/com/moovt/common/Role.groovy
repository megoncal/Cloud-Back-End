package com.moovt.common

import java.util.Date;

import com.moovt.MultiTenantAudit;

@MultiTenantAudit
class Role {

	def domainService;
	
	String authority
	
	static mapping = {
		cache true
	}

	
	static constraints = {
		authority blank: false, unique: ['tenantId']
		
	}
	
	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	} 
}
