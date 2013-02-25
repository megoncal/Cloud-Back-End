package com.moovt

//TODO: Handle the authentication failure other than 302

/**
 * Represents an Asset model
 *
 * @param  url  an absolute URL giving the base location of the image
 * @param  name the location of the image, relative to the url argument
 * @return      the image at the specified URL
 * @see         Image
 */
class Asset {

	static constraints = {
		imageUUID nullable: true
	}
	
	/**
	 * The
	 */
    Integer tenantId
	String shortDescription
	String longDescription
	String imageUUID
	
	
	static transients = [ "CRUDMessage" ];
	/*
	 * 
	 */
	String CRUDMessage;
	
	String toString() {
		return "id: " + id + ", version: " + version + ", shortDescription: " + shortDescription + ", longDescription: " + longDescription;
	}

}
