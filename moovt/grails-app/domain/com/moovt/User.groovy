package com.moovt

import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;

class User {
	
	transient springSecurityService

	//DB properties
	//TODO: [java.lang.Integer] and doesn't support constraint [blank]
	Integer tenantId
	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	Locale locale
	
	//Transient properties
	String tenantname

	static transients = [ "tenantname" ];
	

	static constraints = {
		tenantId nullable: false
		username nullable:false, blank: false, unique: ['tenantId']
		password nullable:false, blank: false
		tenantname bindable: true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		log.info("Getting Authorities " + UserRole.findAllByTenantIdAndUser(tenantId,this));
		UserRole.findAllByTenantIdAndUser(tenantId, this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
			password = springSecurityService.encodePassword(password, username)
	}
	
	String toString() {
		return "id: " + id + ", version: " + version + ", tenantId: " + tenantId + ", tenantname [transient]: "+ tenantname +", username: " + username + ", password: " + password + " locale: " + locale;
	}
}
