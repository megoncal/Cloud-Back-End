package com.moovt.taxi

import com.moovt.MultiTenantAudit;
import com.moovt.common.Address;
import com.moovt.common.User

class Passenger extends User {


	Address address
	static mapping = {
		address cascade: 'all'
	}
	}
