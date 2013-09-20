package com.moovt.taxi
 
import java.util.Date;

import com.moovt.audit.DomainHelper;
import com.moovt.audit.MultiTenantAudit;
import com.moovt.audit.User;

/**
 * This class represents a <code>Passenger</code>. A <code>Passenger</code> has the same id as its associated <code>User</code>.
 *
 * @author egoncalves
 *
 */
@MultiTenantAudit
class Passenger {
	
   
	
	static belongsTo = [ user: User ]
	
	static mapping = {
        id column: 'user_id', generator: 'foreign',
            params: [ property: 'user' ]
        user insertable: false, updateable: false
		//, fetch: 'join'
		
    }
	
}
