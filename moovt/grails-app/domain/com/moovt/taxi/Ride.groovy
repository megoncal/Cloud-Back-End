package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Location

enum RideStatus {
	UNASSIGNED, ASSIGNED, COMPLETED, DELETED
 }

/**
 * This class represents a <code>Ride</code>. 
 *
 * @author egoncalves
 *
 */
@MultiTenantAudit
class Ride {

	def domainService
	
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
		driver nullable: true
		rating nullable: true
		comments nullable: true
	}
	
	
	static mapping = {
		pickUpAddress fetch: 'join'
		dropOffAddress fetch: 'join'
		driver fetch: 'join'
		passenger fetch: 'join'
	}
	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
	
}
