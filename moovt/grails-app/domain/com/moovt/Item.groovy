package com.moovt

@MultiTenantAudit
class Item {
	
	def domainService;
	
	String title
	String shortDescription
	String longDescription
	String type
	Double latitude
	Double longitude
	
	 
	
	def beforeValidate () {
		domainService.setAuditAttributes(this);
	} 
	
	String toString() {
		return "id: " + id + ", version: " + version + ", shortDescription: " + shortDescription + ", longDescription: " + longDescription;
	}
}
