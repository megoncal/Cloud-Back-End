package com.moovt

import java.util.Date;

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
