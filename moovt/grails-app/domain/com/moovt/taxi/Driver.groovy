package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.User

enum CarType {
	SEDAN, VAN, LIMO
}

enum ActiveStatus {
	ENABLED, DISABLED
}
enum MetroArea {
}

@MultiTenantAudit
class Driver {

	def domainService
	
	Long id
	ActiveStatus activeStatus = ActiveStatus.ENABLED
	CarType carType
	String servedMetro

	static constraints = {
	}
	static mapping = {
		id generator: 'assigned'
	}	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
}
