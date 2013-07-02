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
//@MultiTenantAudit
class Role {

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;
	
	String authority
	
	static mapping = {
		cache true
	}

	
	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true
		
		authority blank: false, unique: ['tenantId']

	}
	
	
	def beforeInsert () {
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setAuditAttributes(this);
	}}
