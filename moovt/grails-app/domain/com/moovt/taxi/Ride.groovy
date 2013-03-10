package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;

enum RideStatus {
	UNASSIGNED, ASSIGNED, CANCELED, COMPLETED, COMMENTED
 }

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
		//pickUpAddress cascade: 'all'
		//dropOffAddress cascade: 'all'
		pickUpAddress fetch: 'join', cascade: 'all'
		dropOffAddress fetch: 'join', cascade: 'all'
		driver fetch: 'join'
		passenger fetch: 'join'
	}
	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
	
}
