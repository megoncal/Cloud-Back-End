package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;

enum RideStatus {
	UNASSIGNED, ASSIGNED, CANCELED, COMPLETED, COMMENTED
 }

@MultiTenantAudit
class Ride {

    RideStatus rideStatus  
	Driver driver
	Passenger passenger
	Date pickupDateTime
	Address pickUpAddress
	//Address dropOffAddress
	Double rating
	String comments
	
	static constraints = {
		driver nullable: true
		rating nullable: true
		comments nullable: true
	}
	
	static mapping = {
		pickUpAddress cascade: 'all'

	}
	
}
