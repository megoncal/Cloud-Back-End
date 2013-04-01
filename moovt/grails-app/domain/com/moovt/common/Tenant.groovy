package com.moovt.common

/**
 * This class represents a <code>Tenant</code> in this multi-tenant application.
 * 
 * @author egoncalves
 *
 */
class Tenant{

	def domainService;
	
	String name
	
	Long createdBy;
	Long lastUpdatedBy;
	Date dateCreated;
	Date lastUpdated;
	
    
    static constraints = {
        name blank: false, unique: true
    }
	
	def beforeValidate () {
		domainService.setTenantAuditAttributes(this);
	}
    
    Integer tenantId() {
        return this.id
    }

}