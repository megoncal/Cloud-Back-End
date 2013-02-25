package com.moovt
 
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;

@MultiTenantAudit
class User  {     
	          
	transient  springSecurityService
	def domainService
	
	//DB properties
	String username
	String password 
	String email
	boolean enabled
	boolean accountExpired
	boolean accountLocked 
	boolean passwordExpired
	Locale locale
	 
	//Transient properties 
	String tenantname
	static transients = [ "tenantname"]  ; 

		static constraints = {
		username nullable:false, blank: false, unique: ['tenantId'] 
		password nullable:false, blank: false
		email nullable:false, blank: false, unique: true
		tenantname bindable: true
	}

	static mapping = {
		password column: '`password`'
	}

	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
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
