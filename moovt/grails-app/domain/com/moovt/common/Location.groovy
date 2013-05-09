package com.moovt.common

import com.moovt.MultiTenantAudit;

enum LocationType {
	ROOFTOP, RANGE_INTERPOLATED, GEOMETRIC_CENTER, APPROXIMATE
}

@MultiTenantAudit
class Location {
	
	def domainService

	String locationName
	String politicalName
	Double latitude
	Double longitude
	LocationType locationType

	def beforeValidate () {
		domainService.setAuditAttributes(this);
	}

}
