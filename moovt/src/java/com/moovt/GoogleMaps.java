package com.moovt;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moovt.taxi.Location;
import com.moovt.taxi.LocationType;

public class GoogleMaps {

	public static List<Location> searchLocation(String locationStr) throws LocationSearchException{
		List<Location> l = new ArrayList<Location>();

		try {

			//Some know issues with the Google API
			locationStr = locationStr.toUpperCase();

			if (locationStr.matches(".*BRASILIA.*")) {
				//log.info("Adjusting location for Brasilia")
				locationStr = locationStr.toUpperCase().replaceAll("BRASILIA","BRASILIA, DF, BR");
			}

			if (locationStr.toUpperCase().matches(".*CASA.*[0-9]*")) {
				//log.info('Adjusting location for Brasilia')
				locationStr = locationStr.toUpperCase().replaceAll("CASA","");
			}
			URI uri = new URI(
				    "http", 
				    "maps.googleapis.com", 
				    "/maps/api/geocode/json",
				    "address="+locationStr+"&sensor=false",
				    null);
			URL url = uri.toURL();
			
			ObjectMapper mapper = new ObjectMapper(); // just need one

			GoogleGeoCodeResponse result = mapper.readValue(url,GoogleGeoCodeResponse.class);

			if (result.status.equals("ZERO_RESULTS")) {
				return l;
			}
			
			if (!(result.status.equals("OK"))) {
				throw new LocationSearchException ("A not OK was return ");
			}

			for (results aResult : result.results) {
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

				for (address_component aAddressComponent : aResult.address_components) {
					String shortName = aAddressComponent.short_name;
					String longName = aAddressComponent.long_name;

					for(int i =0; i < aAddressComponent.types.length; i++) {
						String type = aAddressComponent.types [i];
						if (type.equals("establishment")) {
							establishment = longName;
						}
						if (type.equals("street_number")) {
							streetNumber = shortName;
						}
						if (type.equals("route")) {
							route = longName;
						}
						if (type.equals("sublocality")) {
							neighborhood = longName;
						}
						if (type.equals("locality")) {
							city = longName;
						}
						if (type.equals("administrative_area_level_1")) {
							stateShort = shortName;
							stateLong = longName;
						}
						if (type.equals("country")) {
							countryShort = shortName;
							countryLong = longName;
						}
					}
				}

				if (!establishment.equals("")) {
					locationName = establishment;
					politicalName = city +", " + stateShort +", "+countryShort;
				} else if (!route.equals("")) {
					if (countryShort.equals("US")) {
						if (!streetNumber.equals("")) {
							locationName = streetNumber + " " + route;
						} else {
							locationName = route;
						}
					}
					if (countryShort.equals("BR")) {
						if (!streetNumber.equals("")) {
							locationName = route + ", " + streetNumber;
						} else {
							locationName = route;
						}
					}
					politicalName = city +", " + stateShort +", "+countryShort;
				} else if (!city.equals("")) { //No street
					locationName = city;
					politicalName = stateLong +", "+countryLong;
				}

				location.setLocationName(locationName);
				location.setPoliticalName(politicalName);
				location.setLatitude(new Double(aResult.geometry.location.lat));
				location.setLongitude(new Double(aResult.geometry.location.lng));
				String locationType = aResult.geometry.location_type;
				if (locationType.equals("ROOFTOP")) {
					location.setLocationType(LocationType.ROOFTOP);
				}
				if (locationType.equals("RANGE_INTERPOLATED")) {
					location.setLocationType(LocationType.RANGE_INTERPOLATED);
				}
				if (locationType.equals("GEOMETRIC_CENTER")) {
					location.setLocationType(LocationType.GEOMETRIC_CENTER);
				}
				if (locationType.equals("APPROXIMATE")) {
					location.setLocationType(LocationType.APPROXIMATE);
				}
				if (city!="") {
					l.add(location);
				}
			}
		} catch (Throwable e) {
			throw new LocationSearchException ("Unable to call googlemaps " + e.getLocalizedMessage());
		}
		return l;
	}
}
