package com.moovt.taxi

import java.util.Date;

import com.moovt.MultiTenantAudit;
import com.moovt.common.Location;
import com.moovt.common.User;
import com.moovt.DomainHelper;

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
//@MultiTenantAudit
class Driver {

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;
	
	ActiveStatus activeStatus = ActiveStatus.ENABLED
	CarType carType
	Location servedLocation
	static belongsTo = [ user: User ]
	
	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true
	}
	
	static mapping = {
		id column: 'user_id', generator: 'foreign',
			params: [ property: 'user' ]
		user insertable: false, updateable: false
		//, fetch: 'join'
	}
	def beforeInsert () {
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setAuditAttributes(this);
	}
}
