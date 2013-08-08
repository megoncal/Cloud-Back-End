package com.moovt

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.moovt.common.LocationType;


import com.moovt.common.Location;


class GoogleMapsDelegate {

	protected static final Logger log = LoggerFactory.getLogger(getClass());
	
	public static List<Location> searchLocation(String locationStr) throws LocationSearchException{
		List<Location> l = new ArrayList<Location>()

		log.info("Calling Google")

		//Some know issues with the Google API
		locationStr = locationStr.toUpperCase();

		if (locationStr.matches('.*BRASILIA.*')) {
			log.info('Adjusting location for Brasilia')
			locationStr = locationStr.toUpperCase().replaceAll("BRASILIA","BRASILIA, DF, BR");
		}

		if (locationStr.toUpperCase().matches('.*CASA.*[0-9]*')) {
			log.info('Adjusting location for Brasilia')
			locationStr = locationStr.toUpperCase().replaceAll("CASA","");
		}

		def http = new HTTPBuilder( 'http://maps.googleapis.com/' )
		try {
			// perform a GET request, expecting JSON response data
			http.request( Method.GET, ContentType.JSON ) {
				uri.path = '/maps/api/geocode/json'
				uri.query = [ address: locationStr, sensor: false ]

				headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

				// response handler for a success response code:
				response.success = { resp, json ->
					//println resp.statusLine
					// parse the JSON response object:
					//println json.results;
					json.results.each {
						Location location = new Location();
						String locationName = "";
						String politicalName = "";
						String streetNumber = "";
						String route ="";
						String neighborhood ="";
						String city ="";
						String stateShort ="";
						String stateLong ="";
						String countryShort = "";
						String countryLong = "";
						String establishment = "";
						Double latitude;
						Double longitude;

						it.address_components.each {
							String shortName = it.short_name;
							String longName = it.long_name;

							it.types.each {
								if (it == "establishment") {
									establishment = longName;
								}
								if (it == "street_number") {
									streetNumber = shortName;
								}
								if (it == "route") {
									route = longName;
								}
								if (it == "sublocality") {
									neighborhood = longName;
								}
								if (it == "locality") {
									city = longName;
								}
								if (it == "administrative_area_level_1") {
									stateShort = shortName;
									stateLong = longName;
								}
								if (it == "country") {
									countryShort = shortName;
									countryLong = longName;
								}
							}
						}
						log.info("AGAIN3");
						if (establishment) {
							locationName = establishment;
							politicalName = city +", " + stateShort +", "+countryShort
						} else if (route) {
							if (countryShort == "US") {
								if (streetNumber) {
									locationName = streetNumber + " " + route;
								} else {
									locationName = route;
								}
							}
							if (countryShort == "BR") {
								if (streetNumber) {
									locationName = route + ", " + streetNumber;
								} else {
									locationName = route;
								}
							}
							politicalName = city +", " + stateShort +", "+countryShort
						} else if (city) { //No street
							locationName = city
							politicalName = stateLong +", "+countryLong
						}

						log.info("AGAIN4");
						location.locationName = locationName;
						location.politicalName = politicalName;
						location.latitude =  it.geometry.location.lat
						location.longitude = it.geometry.location.lng
						String locationType = it.geometry.location_type
						switch ( locationType ) {
							case "ROOFTOP":
								location.locationType = LocationType.ROOFTOP;
								break;
							case "RANGE_INTERPOLATED":
								location.locationType = LocationType.RANGE_INTERPOLATED;
								break;
							case "GEOMETRIC_CENTER":
								location.locationType = LocationType.GEOMETRIC_CENTER;
								break;
							case "APPROXIMATE":
								location.locationType = LocationType.APPROXIMATE;
								break;
						}

						if (city) {
							l.add(location);
						}
					}
				}

				// handler for any failure status code:
				response.failure = { resp ->
					throw new LocationSearchException ("Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}");
				}
			}
		} catch (Throwable e) {
			throw new LocationSearchException ("Unable to call googlemaps " + e.message);
		}
		return l;
	}
}
