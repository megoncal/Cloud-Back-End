package com.moovt.common

import com.moovt.MultiTenantAudit;

enum AddressType {
	HOME, OFFICE
}

/**
 * This class represents an <code>Address</code> that is used in Rides and Users.
 *
 * @author egoncalves
 *
 */
@MultiTenantAudit
class Address {

	
	def domainService
	
    static constraints = {
    }
	
	public Address(String street, String city, String state, String zip,
			AddressType addressType) {
		super();
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.addressType = addressType;
	}

	
	String address
	String formattedAddress
	Long latitude
	Long longitude
	String locationType
	AddressType addressType

	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}
		
	String toString(){
		dump();
	}
	
}
