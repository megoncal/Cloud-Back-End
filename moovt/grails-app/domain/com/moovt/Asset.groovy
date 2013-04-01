package com.moovt

//TODO: Handle the authentication failure other than 302

enum CustomerType  {
	DRIVER, PASSENGER
}

/**
 * This class represents an <code>Asset</code>. This is currently used by the AssetManager App.
 *
 * @author egoncalves
 *
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
