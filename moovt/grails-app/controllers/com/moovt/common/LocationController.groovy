package com.moovt.common

import com.moovt.CallResult
import com.moovt.CustomGrailsUser
import com.moovt.taxi.Passenger
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
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			return;
		}
		
		String locationStr = jsonObject.opt("location");
		
		if (!locationStr) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,"Input JSON must contain a location element") as JSON);
		}
		
		
		
		//Call Service
		try {
			List<Location> locations = locationService.searchLocation (locationStr);
			render "{\"locations\":" + locations.encodeAsJSON() + "}"
		} catch (Throwable e) {
		    render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
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
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR, e.message) as JSON);
			return;
		}

		//Passenger
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		assert principal.id != null, "Because this method is secured, a principal always exist at this point of the code"
		Passenger passenger = Passenger.get(principal.id);
		
		//Call Service
		try {
			List<Location> locations = locationService.getMostFrequentLocations(passenger);
			render "{\"locations\":" + locations.encodeAsJSON() + "}"
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
		
		
		
		
		
	}
	
	
}
