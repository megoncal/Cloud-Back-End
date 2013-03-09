package com.moovt.common

import com.moovt.MultiTenantAudit;

enum AddressType {
	HOME, OFFICE
}

@MultiTenantAudit
class Address {

    static constraints = {
    }
	
	public Address(String street, String city, String state, String zip,
			String type) {
		super();
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.type = type;
	}

	String street
	String city
	String state
	String zip
	AddressType addressType
	
	String toString(){
		dump();
	}
	
}
