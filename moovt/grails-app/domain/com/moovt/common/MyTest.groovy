package com.moovt.common

import com.moovt.MultiTenantAudit;
import com.moovt.DomainHelper;

@MultiTenantAudit
class MyTest {
	
	String a;
	String b;

	def beforeValidate () {
		DomainHelper.setAuditAttributes(this);
	}
	
	}
