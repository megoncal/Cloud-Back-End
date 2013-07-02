package com.moovt

import com.moovt.common.Location;
import com.moovt.common.LocationType;
import com.moovt.common.User
import com.moovt.taxi.CarType
import com.moovt.taxi.Driver;
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import grails.plugin.mail.MailService
import groovyx.net.http.*
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.SessionFactory
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import org.codehaus.groovy.grails.commons.GrailsApplication


class LocationService {

	SessionFactory sessionFactory
	MailService mailService
	GrailsApplication grailsApplication

	static transactional = false

	public List<Location> searchLocation(String locationStr) throws LocationSearchException{
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
			http.request( GET, JSON ) {
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
								//println it;
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
			throw new LocationSearchException ("Unable to call googlemaps");
		}
		return l;
	}

	public List<DriverDistance> findNearbyDrivers(Location pickUpLocation, CarType carType) {

		List<DriverDistance> l = new ArrayList<DriverDistance>();

		//Determine the rectangle of 66 radius
		//Drivers within 66 miles or 100 Km of the pickup location will be called

		Double dist = grailsApplication.config.moovt.driver.search.radius;
		Double lon1 = pickUpLocation.longitude-dist/Math.abs(Math.cos(Math.toRadians(pickUpLocation.latitude))*69);
		Double lon2 = pickUpLocation.longitude+dist/Math.abs(Math.cos(Math.toRadians(pickUpLocation.latitude))*69);
		Double lat1 = pickUpLocation.latitude-(dist/69);
		Double lat2 = pickUpLocation.latitude+(dist/69);


		String sql = "SELECT d.user_id as driverId, 3956 * 2 * ASIN(SQRT( POWER(SIN((:lat - latitude) * pi()/180 / 2), 2) +COS(:lat * pi()/180) * COS(latitude * pi()/180) *POWER(SIN((:lon - longitude) * pi()/180 / 2), 2) )) as distance FROM  driver d, location l WHERE d.served_location_id = l.id AND l.longitude between :lon1 and :lon2 and l.latitude between :lat1 and :lat2 and d.car_type >= :carType having distance < :dist ORDER BY distance limit 10"
		Session dbSession =  sessionFactory.getCurrentSession();
		Query query = dbSession.createSQLQuery(sql);
		query.setDouble('lat', pickUpLocation.latitude);
		query.setDouble('lon', pickUpLocation.longitude);
		query.setDouble('lon1', lon1);
		query.setDouble('lon2', lon2);
		query.setDouble('lat1', lat1);
		query.setDouble('lat2', lat2);
		query.setDouble('dist', dist);
		query.setString('carType', carType.toString());
		
		List rows = query.list();

		Integer numberOfDrivers = 0;
		for (row in rows) {

			ArrayList driverAndDistance = row;
			Integer driverId = driverAndDistance.get(0);
			Double distance =  driverAndDistance.get(1);
			DriverDistance driverDistance = new DriverDistance(driverId, distance);
			l.add(driverDistance);
		}

		return l;
	}

	public List<RideDistance> findNearbyRides(Location servedLocation) {
		
				List<RideDistance> l = new ArrayList<RideDistance>();
				
				log.info("Finding Nearby Rides for " + servedLocation.dump())
		
				//Determine the rectangle of 66 radius
				//Rides within 66 miles or 100 Km of the driver location will be displayed
		
				Double dist = grailsApplication.config.moovt.driver.search.radius;
				Double lon1 = servedLocation.longitude-dist/Math.abs(Math.cos(Math.toRadians(servedLocation.latitude))*69);
				Double lon2 = servedLocation.longitude+dist/Math.abs(Math.cos(Math.toRadians(servedLocation.latitude))*69);
				Double lat1 = servedLocation.latitude-(dist/69);
				Double lat2 = servedLocation.latitude+(dist/69);
		
		
				String sql = "SELECT r.id as rideId, 3956 * 2 * ASIN(SQRT( POWER(SIN((:lat - latitude) * pi()/180 / 2), 2) +COS(:lat * pi()/180) * COS(latitude * pi()/180) *POWER(SIN((:lon - longitude) * pi()/180 / 2), 2) )) as distance FROM  ride r, location l WHERE r.pick_up_location_id = l.id AND r.ride_status = 'UNASSIGNED' AND l.longitude between :lon1 and :lon2 and l.latitude between :lat1 and :lat2 having distance < :dist ORDER BY distance limit 10"
				Session dbSession =  sessionFactory.getCurrentSession();
				Query query = dbSession.createSQLQuery(sql);
				query.setDouble('lat', servedLocation.latitude);
				query.setDouble('lon', servedLocation.longitude);
				query.setDouble('lon1', lon1);
				query.setDouble('lon2', lon2);
				query.setDouble('lat1', lat1);
				query.setDouble('lat2', lat2);
				query.setDouble('dist', dist);
				
				log.info(query.dump());
				
				List rows = query.list();
		
				Integer numberOfDrivers = 0;
				for (row in rows) {
		
					ArrayList rideAndDistance = row;
					Integer rideId = rideAndDistance.get(0);
					Double distance =  rideAndDistance.get(1);
					RideDistance rideDistance = new RideDistance(rideId, distance);
					l.add(rideDistance);
				}
		
				return l;
			}
		

	
	public List<Location> getMostFrequentLocations (Passenger passenger) {
		List<Location> l = new ArrayList<DriverDistance>();
		
		//Select the 10 most frequently used locations in the rides of this passenger
		String sql = "select l.id, l.version, l.latitude, l.location_name, l.location_type, l.longitude, l.political_name, count(*) from ride r, location l where (l.id = r.pick_up_location_id or l.id = r.drop_off_location_id) and passenger_id = :passengerId group by l.id order by count(*) desc limit 10"
		Session dbSession =  sessionFactory.getCurrentSession();
		Query query = dbSession.createSQLQuery(sql);
		query.setLong('passengerId', passenger.id);
		List rows = query.list();
		rows.each {
			Location location = new Location();
			location.id = it[0];
			location.version = it[1];
			location.latitude = it[2];
			location.locationName = it[3];
			location.locationType = it[4];
			location.longitude = it[5];
			location.politicalName = it[6];
			
			l.add(location);
		}
		
		return l;
		
		
	}
}

