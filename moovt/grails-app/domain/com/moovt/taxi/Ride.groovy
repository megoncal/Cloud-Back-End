package com.moovt.taxi

import java.util.Date;

import com.moovt.audit.DomainHelper;
import com.moovt.audit.MultiTenantAudit;

enum RideStatus {
	UNASSIGNED, ASSIGNED, COMPLETED, CANCELED
 }

/**
 * This class represents a <code>Ride</code>. 
 *
 * @author egoncalves
 *
 */
@MultiTenantAudit
class Ride {


    RideStatus rideStatus  
	Driver driver
	Passenger passenger
	Date pickupDateTime
	Location pickUpLocation
	Location dropOffLocation
	CarType carType
	Double rating
	String comments
	String pickUpLocationComplement
	String messageToTheDriver
	
	static constraints = {

		driver nullable: true
		rating nullable: true
		comments nullable: true
		pickUpLocationComplement nullable: true
		messageToTheDriver nullable: true
	}
	
	
}
