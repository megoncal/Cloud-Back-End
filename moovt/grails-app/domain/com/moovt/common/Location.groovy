package com.moovt.common

//import com.moovt.MultiTenantAudit;
import java.util.Date;

import com.moovt.DomainHelper;

enum LocationType {
	ROOFTOP, RANGE_INTERPOLATED, GEOMETRIC_CENTER, APPROXIMATE
}

//@MultiTenantAudit
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
	
	def beforeInsert() {
		println("Before Insert");
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate() {
		println("Before Update");
		DomainHelper.setAuditAttributes(this);
	}

}
