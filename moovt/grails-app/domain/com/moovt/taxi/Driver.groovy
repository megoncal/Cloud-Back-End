package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Location;
import com.moovt.common.User;

//TODO: Make Car Type more intelligent
enum CarType {
	A_SEDAN, B_VAN, C_LIMO
}


enum ActiveStatus  {
	   ENABLED, DISABLED
   }

/**
 * This class represents a <code>Driver</code>. A <code>Driver</code> has the same id as its associated <code>User</code>.
 * 
 * @author egoncalves
 *
 */
@MultiTenantAudit
class Driver {

	def domainService
	
	ActiveStatus activeStatus = ActiveStatus.ENABLED
	CarType carType
	Location servedLocation
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
