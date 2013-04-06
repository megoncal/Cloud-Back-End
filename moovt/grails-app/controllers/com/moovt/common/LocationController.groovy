package com.moovt.common

import com.moovt.CallResult
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class LocationController {

	def locationService
	
    def index() { }
	
	/**
	 * This API retrieves one or more <code>GeoLocation</code> based on a location string provided
	 * @param  url  <server-name>/geoLocation/search
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
			render([code: "ERROR", msg: e.message ] as JSON);
			return;
		}
		
		String locationStr = jsonObject.get("location");
		
		if (!locationStr) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,"Input JSON must contain a location element") as JSON);
		}
		
		
		
		//Call Service
		
		List<Location> locations = locationService.searchLocation (locationStr);
		
		render locations as JSON;
		
		
		
	}
	
}
