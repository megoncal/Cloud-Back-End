package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.User

enum CarType {
	SEDAN, VAN, LIMO
}

enum MetroArea {
	
}
class Driver extends User {


	CarType carType

	String servedMetro

	static constraints = {
	}
	
}
