package com.moovt

import com.moovt.common.Location
import com.moovt.common.User
import com.moovt.common.Role
import com.moovt.common.UserRole
import com.moovt.taxi.Driver
import com.moovt.taxi.Passenger
import grails.validation.ValidationException
import org.codehaus.groovy.grails.web.json.JSONObject

class UserService {

	public updateUser (User user, JSONObject userJSON)  {
		log.info("Retrieved User: " + user.dump());

		//Obtain version from the json object
		Long version = userJSON.optLong("version",0);
				
		//Before updating - check for concurrency
		log.info("Comparing retrieved User Version " + user.version + " with JSON Version " + version);
		
		if (user.version > version) {
			throw new ConcurrencyException ();
		}
		user.properties = userJSON;

		//Handle Driver type
		JSONObject driverJSON = userJSON.opt("driver");



		if (driverJSON) {
			//Handle the driver's served location inside the driver JSON
			JSONObject servedLocationJSON = driverJSON.get("servedLocation");

			//Handle car Type
			JSONObject carTypeJSON = driverJSON.get("carType");


			//Handle Active Status
			JSONObject activeStatusJsonObject = driverJSON.get("activeStatus");
			
			if (!user.driver) {
				log.info("Creating driver");
				Driver driver = new Driver(driverJSON);
				Location servedLocation = new Location(servedLocationJSON);
				servedLocation.save(failOnError:true);
				driver.servedLocation = servedLocation;
				driver.carType = carTypeJSON.get("code");
				driver.activeStatus = activeStatusJsonObject.get("code");
				driver.user = user
				user.driver = driver;
			} else {
				log.info("Updating servedLocation, carType, and activeStatus");
				user.driver.servedLocation.properties = servedLocationJSON;
				user.driver.carType = carTypeJSON.get("code");
				user.driver.activeStatus = activeStatusJsonObject.get("code");
				user.lastUpdated = new Date(); //This forces the update of user
			}

			log.info("Driver being saved " + user.driver.dump());
			//user.driver.save(failOnError:true);


			//Assign the driver role
			def driverRole = Role.findByTenantIdAndAuthority(user.tenantId, 'ROLE_DRIVER')
			if (!user.authorities.contains(driverRole)) {
				UserRole.create ( user.tenantId, user, driverRole)
			}

		}

		//Handle Passenger type
		JSONObject passengerJSON = userJSON.opt("passenger");
		if (passengerJSON) {
			Passenger passenger = user.passenger;
			if (!passenger) {
				passenger = new Passenger(passengerJSON);
				passenger.user = user
				user.passenger = passenger;

			} else {
				passenger.properties = passengerJSON
			}

			log.info("Passenger being saved " + driver.dump());
			//passenger.save(failOnError:true);


			// Assign the passenger role
			def passengerRole = Role.findByTenantIdAndAuthority(user.tenantId, 'ROLE_PASSENGER')
			if (!user.authorities.contains(passengerRole)) {
				UserRole.create ( user.tenantId, user, passengerRole)
			}
		}
		log.info("User being saved " + user.dump());
	}
}
