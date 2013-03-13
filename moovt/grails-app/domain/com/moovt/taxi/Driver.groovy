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
	
	ActiveStatus activeStatus = ActiveStatus.ENABLED
	CarType carType
	String servedMetro
	static belongsTo = [ user: User ]
	
	static constraints = {
	}
	
	static mapping = {
		id column: 'user_id', generator: 'foreign',
			params: [ property: 'user' ]
		user insertable: false, updateable: false
	}
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
}
