package com.moovt.common
 
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.moovt.MultiTenantAudit;
import com.moovt.taxi.Passenger;
import com.moovt.taxi.Driver;

@MultiTenantAudit
class User  {     
	          
	transient  springSecurityService
	//TODO: Should this be transient
	def domainService
	
	//DB properties
	String username
	String password 
	String firstName
	String lastName
	String phone
	String email
	boolean enabled = true
	boolean accountExpired = false
	boolean accountLocked = false
	boolean passwordExpired = false
	String locale = "en_US"
	
	//0 or 1 relationship with he possible user types
	//TODO: Make this more scalable
	static hasOne = [ passenger: Passenger,
						driver: Driver ]
				     
	 
	//Transient properties 
	String tenantname
	static transients = [ "tenantname"]  ; 

		static constraints = {
		username nullable:false, blank: false, unique: ['tenantId'] 
		password nullable:false, blank: false
		email nullable:false, blank: false, unique: true
		tenantname bindable: true
		passenger nullable:true,blank: true
		driver nullable:true,blank: true
		//TODO: Other user types
	}

	static mapping = {
		password column: '`password`'
		
	}

	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
	
	Set<Role> getAuthorities() {
		log.info("Getting Authorities (tenantId: " + this.tenantId + ", and User: " + this.id + ") ");
		log.info(UserRole.findAllByTenantIdAndUser(tenantId,this));
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
	
}
