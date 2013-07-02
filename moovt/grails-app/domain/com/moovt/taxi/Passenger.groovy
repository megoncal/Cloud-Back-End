package com.moovt.taxi

import java.util.Date;

import com.moovt.MultiTenantAudit;
import com.moovt.common.User
import com.moovt.DomainHelper

/**
 * This class represents a <code>Passenger</code>. A <code>Passenger</code> has the same id as its associated <code>User</code>.
 *
 * @author egoncalves
 *
 */
//@MultiTenantAudit
class Passenger {
	
	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;

	
	static belongsTo = [ user: User ]
	
	static mapping = {
        id column: 'user_id', generator: 'foreign',
            params: [ property: 'user' ]
        user insertable: false, updateable: false
		//, fetch: 'join'
		
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true
    }
	
	def beforeInsert () {
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setAuditAttributes(this);
	}
}
