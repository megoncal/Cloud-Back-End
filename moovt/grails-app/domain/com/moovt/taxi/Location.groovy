package com.moovt.taxi;
 
import java.util.Date;

import com.moovt.audit.MultiTenantAudit;

enum LocationType {
	ROOFTOP, RANGE_INTERPOLATED, GEOMETRIC_CENTER, APPROXIMATE
}
  
@MultiTenantAudit
class Location {

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;
	 
	 String locationName
	 String politicalName
	 Double latitude
	 Double longitude
	 LocationType locationType

	
	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true
	}
}
