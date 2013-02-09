package com.moovt

class Item {
	Long tenantId
	String title
	String shortDescription
	String longDescription
	String type
	Date creationDate
	Date lastUpdateDate
	Long createdBy
	Long lastUpdatedBy
	Double latitude
	Double longitude
	
	String toString() {
		return "id: " + id + ", version: " + version + ", shortDescription: " + shortDescription + ", longDescription: " + longDescription;
	}
}
