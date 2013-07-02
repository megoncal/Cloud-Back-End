package com.moovt.taxi

import java.util.Date;

import com.moovt.MultiTenantAudit;
import com.moovt.common.Location;
import com.moovt.DomainHelper;

enum RideStatus {
	UNASSIGNED, ASSIGNED, COMPLETED, DELETED
 }

/**
 * This class represents a <code>Ride</code>. 
 *
 * @author egoncalves
 *
 */
//@MultiTenantAudit
class Ride {

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;

    RideStatus rideStatus  
	Driver driver
	Passenger passenger
	Date pickupDateTime
	Location pickUpLocation
	Location dropOffLocation
	CarType carType
	Double rating
	String comments
	
	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true

		driver nullable: true
		rating nullable: true
		comments nullable: true
	}
	
	def beforeInsert () {
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setAuditAttributes(this);
	}
	
}
