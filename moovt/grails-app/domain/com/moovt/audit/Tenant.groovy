package com.moovt.audit

import com.moovt.audit.DomainHelper;

/**
 * This class represents a <code>Tenant</code> in this multi-tenant application.
 * 
 * @author egoncalves
 *
 */
class Tenant{

	
	String name
	
	Long createdBy;
	Long lastUpdatedBy;
	Date dateCreated;
	Date lastUpdated;
	
    
    static constraints = {
        name blank: false, unique: true
    }

	def beforeInsert () {
		DomainHelper.setTenantAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setTenantAuditAttributes(this);
	}
    
    Integer tenantId() {
        return this.id
    }

}