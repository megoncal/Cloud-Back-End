package com.moovt.common

import java.util.Date;

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.moovt.MultiTenantAudit;
import com.moovt.taxi.Passenger;
import com.moovt.taxi.Driver;

import com.moovt.DomainHelper;

enum UserType  {
	DRIVER, PASSENGER, DRIVER_PASSENGER, NO_TYPE
}


/**
 * This class represents a <code>User</code> in the application. A <code>User</code> is extended through additional classes like <code>Passenger</code> and <code>Driver</code>.
 * 
 * @author egoncalves
 *
 */
//@MultiTenantAudit
class User  {

	transient  springSecurityService

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;

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
	//Passenger passenger;
	//Driver driver;

	//0 or 1 relationship with the possible user types
	static hasOne = [ passenger: Passenger,
		driver: Driver ]


	//Transient properties
	String tenantname
	static transients = [ "tenantname"]  ;

	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true

		firstName nullable:false, blank: false
		lastName nullable:false, blank: false
		phone nullable:false, blank: false
		username nullable:false, blank: false, unique: ['tenantId']
		password nullable:false, blank: false
		email nullable:false, blank: false, unique: true
		tenantname bindable: true
		passenger nullable:true
		driver nullable:true
	}

	static mapping = {
		//table "usr"
		password column: '`password`'

	}


	Set<Role> getAuthorities() {
		log.info("Getting Authorities (tenantId: " + this.tenantId + ", and User: " + this.id + ") ");
		log.info(UserRole.findAllByTenantIdAndUser(tenantId,this));
		UserRole.findAllByTenantIdAndUser(tenantId, this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
		DomainHelper.setAuditAttributes(this);
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
		DomainHelper.setAuditAttributes(this);
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password, username)
	}

}
