package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;
import com.moovt.common.User

@MultiTenantAudit
class Passenger {

	def domainService
	
	Long id
	
	static mapping = {
		id generator: 'assigned'
	}
	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
}
