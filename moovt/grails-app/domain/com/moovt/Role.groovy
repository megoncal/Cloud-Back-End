package com.moovt

class Role {

	Integer tenantId
	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		tenantId nullable: false
		authority blank: false, unique: ['tenantId']
		
	}
}
