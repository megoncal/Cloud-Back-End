package com.moovt.common

import com.moovt.MultiTenantAudit
//import com.moovt.DomainHelper;

@MultiTenantAudit
class MyTest {  
	String test
	String test2
 
	def beforeInsert() {
		System.out.println("ssxxcxx111123xcss");
		//encodePassword();
		//DomainHelper.setAuditAttributes(this);
	} 
//
//	def beforeUpdate() {
//		if (isDirty('password')) {
//			encodePassword()
//		}
//	}
	
	
	}
