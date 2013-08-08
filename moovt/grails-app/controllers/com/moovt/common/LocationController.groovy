package com.moovt.common

import com.moovt.CallResult
import com.moovt.CustomGrailsUser
import com.moovt.taxi.Passenger
import com.moovt.LogUtils
import com.moovt.GoogleMaps
import com.moovt.GoogleMapsDelegate

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.dao.OptimisticLockingFailureException;
import grails.validation.ValidationException;
import org.springframework.security.core.Authentication
import org.springframework.web.servlet.support.RequestContextUtils;
import grails.plugins.springsecurity.Secured;
import org.springframework.security.core.context.SecurityContextHolder;


class LocationController {

	def locationService
	
    def index() { }
	
	/**
	 * This API retrieves one or more <code>Location</code> based on a location string provided
	 * @param  url  <server-name>/location/search
	 * @param  input-sample {"location":"Rua Major Lopes 55, Belo Horizonte, MG"}
	 * @return output-sample {"locations":[{"locationName":...,"politicalName":...,"latitude":..., "longitude":....,"locationType":....}]}
	 */
	
	def search() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
			return;
		}
		
		String locationStr = jsonObject.opt("location");
		
		if (!locationStr) {
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,"Input JSON must contain a location element").getJSON(); 
		}
		
		
		
		//Call Service
		try {
			List<Location> locations = locationService.searchLocation (locationStr);
			//Location locations = new Location(locationName:'Wheaton',  politicalName:'Illinois, United States', latitude: 41.8661403, longitude: -88.1070127, locationType: LocationType.APPROXIMATE);	
			log.info("About to render locations (1/2)");
			log.info("Rendering locations " + locations.encodeAsJSON());
			log.info("About to render locations (2/2)");
			
			render "{\"locations\":" + locations.encodeAsJSON() + "}";
			log.info("Locations rendered");
			
		} catch (Throwable e) {
			LogUtils.printStackTrace(e);
		    render new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
		}
		
		
		
		
		
	}
	
	/**
	 * This API retrieves one or more <code>Location</code> based on a location string provided
	 * @param  url  <server-name>/location/search
	 * @param  input-sample {"location":"Rua Major Lopes 55, Belo Horizonte, MG"}
	 * @return output-sample {"locations":[{"locationName":...,"politicalName":...,"latitude":..., "longitude":....,"locationType":....}]}
	 */
	
	def search01() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
			return;
		}
		
		String locationStr = jsonObject.opt("location");
		
		if (!locationStr) {
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,"Input JSON must contain a location element").getJSON();
		}
		
		
		
		//Call Service
		try {
			List<Location> locations = GoogleMapsDelegate.searchLocation (locationStr);
			//Location locations = new Location(locationName:'Wheaton',  politicalName:'Illinois, United States', latitude: 41.8661403, longitude: -88.1070127, locationType: LocationType.APPROXIMATE);
			log.info("About to render locations via Delegate (1/2)");
			log.info("Rendering locations " + locations.encodeAsJSON());
			log.info("About to render locations via Delegate (2/2)");
			
			render "{\"locations\":" + locations.encodeAsJSON() + "}";
			log.info("Locations rendered");
			
		} catch (Throwable e) {
			LogUtils.printStackTrace(e);
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
		}
		
		
	}

	def search02() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
			return;
		}
		
		String locationStr = jsonObject.opt("location");
		
		if (!locationStr) {
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,"Input JSON must contain a location element").getJSON();
		}
		
		
		
		//Call Service
		try {
			List<Location> locations = GoogleMaps.searchLocation (locationStr);
			//Location locations = new Location(locationName:'Wheaton',  politicalName:'Illinois, United States', latitude: 41.8661403, longitude: -88.1070127, locationType: LocationType.APPROXIMATE);
			log.info("About to render locations via GoogleMaps (1/2)");
			log.info("Rendering locations " + locations.encodeAsJSON());
			log.info("About to render locations via GoogleMaps (2/2)");
			
			render "{\"locations\":" + locations.encodeAsJSON() + "}";
			log.info("Locations rendered");
			
		} catch (Throwable e) {
			LogUtils.printStackTrace(e);
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message).getJSON();
		}
		
		
	}

	/**
	 * This API retrieves a list of Location frequently used by this passenger
	 * @param  url  <server-name>/location/getMostFrequentLocations
	 * @param  input-sample {}
	 * @return output-sample {"locations":[{"locationName":...,"politicalName":...,"latitude":..., "longitude":....,"locationType":....}]}
	 */
	
	@Secured(['ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'	])
	def getMostFrequentLocations() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);


		//Passenger
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		assert principal.id != null, "Because this method is secured, a principal always exist at this point of the code"
		Passenger passenger = Passenger.get(principal.id);
		
		//Call Service
			List<Location> locations = locationService.getMostFrequentLocations(passenger);
			render "{\"locations\":" + locations.encodeAsJSON() + "}"
		} catch (Throwable e) {
			LogUtils.printStackTrace(e);
			render new CallResult(CallResult.SYSTEM,CallResult.ERROR, e.message).getJSON();
		}
		
		
		
		
		
	}
	
	
}
