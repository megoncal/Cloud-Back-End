package com.moovt


class Asset {

	static transients = [ "CRUDMessage" ];
	
	static constraints = {
		imageUUID nullable: true
	}
	
    //static expose = 'asset'
    Integer tenantId
	String shortDescription
	String longDescription
	String imageUUID
	String CRUDMessage;
	String toString() {
		return "id: " + id + ", version: " + version + ", shortDescription: " + shortDescription + ", longDescription: " + longDescription;
	}

}
