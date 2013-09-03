package com.moovt.common

import java.util.Date;

import com.moovt.MultiTenantAudit;
import com.moovt.DomainHelper;

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
