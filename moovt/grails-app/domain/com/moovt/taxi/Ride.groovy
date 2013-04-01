package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;

enum RideStatus {
	UNASSIGNED, ASSIGNED, CANCELED, COMPLETED, COMMENTED
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
	Address pickUpAddress
	Address dropOffAddress
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
