package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;
import com.moovt.common.User

/**
 * This class represents a <code>Passenger</code>. A <code>Passenger</code> has the same id as its associated <code>User</code>.
 *
 * @author egoncalves
 *
 */
@MultiTenantAudit
class Passenger {

	def domainService
	
	static belongsTo = [ user: User ]
	
	static mapping = {
        id column: 'user_id', generator: 'foreign',
            params: [ property: 'user' ]
        user insertable: false, updateable: false
    }
	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
}
