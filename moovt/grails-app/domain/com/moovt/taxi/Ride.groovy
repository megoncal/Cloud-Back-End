package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;
import com.moovt.common.Location

enum RideStatus {
	UNASSIGNED, ASSIGNED, COMPLETED
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
	Double rating
	String comments
	
	static constraints = {
		driver nullable: true
		rating nullable: true
		comments nullable: true
	}
	
	//TODO: Check Custom Hibernate Types
	
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
