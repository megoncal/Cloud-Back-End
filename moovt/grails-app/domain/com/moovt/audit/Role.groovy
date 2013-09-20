package com.moovt.audit
 
import java.util.Date;

import com.moovt.audit.DomainHelper;
import com.moovt.audit.MultiTenantAudit;

/**
 * This class represents a <code>Role</code> that is used to assign authority to a given <code>User</code>.
 *
 * @author egoncalves
 *
 */
 
@MultiTenantAudit
class Role {
	  
	String authority 
	
	static mapping = {
		cache true
	}
  
	
	static constraints = {

		
		authority blank: false, unique: ['tenantId']

	}
	
	
}
