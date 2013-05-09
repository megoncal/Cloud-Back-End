package com.moovt.taxi

import com.moovt.CallResult;
import com.moovt.CustomGrailsUser
import com.moovt.DriverDistance
import com.moovt.LocationService
import com.moovt.NotificationService;
import com.moovt.RideDistance
import com.moovt.UtilService
import com.moovt.common.Address
import com.moovt.common.Location
import com.moovt.common.LoginController;
import com.moovt.common.Tenant;
import com.moovt.common.Role;
import com.moovt.common.User;
import grails.converters.JSON;
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;
import grails.validation.ValidationException;
import org.springframework.security.core.Authentication
import org.springframework.web.servlet.support.RequestContextUtils;
import grails.plugins.springsecurity.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The RideController is responsible for managing the lifecycle of a <code>Ride</code>
 */
class RideController {

	def springSecurityService;
	LocationService locationService;
	NotificationService notificationService;
	UtilService utilService;
	



	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}

	/**
	 * This API creates a <code>Ride</code> for the <code>Passenger</code> currently logged in. Please note that a <code>User</code> 
	 * registered as a <code>Passenger</code> must be logged in. Otherwise a Not Authorized exception will be returned.
	 * 
	 *  Example: <server-name>/moovt/ride/createRide
	 * 
	 * {"pickupDateTime":"2013-03-15 06:30",
	 * "pickUpLocation":{"locationName":"Rua PickUp Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},
	 * "dropOffLocation":{"locationName":"Rua DropOff Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"}
	 * }
	 * 
	 */

	@Secured(['ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'	])
	def createRide() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject rideJsonObject = null;
		try {
			rideJsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		//Start a transaction to insert or update based on the existence of an id
		Ride.withTransaction { status ->
			Ride ride = null;
			try {
				log.info("Creating a new ride");

				ride = new Ride();

				try {
					String dateTimeStr = rideJsonObject.optString("pickupDateTime");
					if (!dateTimeStr) {
						render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,"Please make sure you have a field called pickupDateTime with format yyyy-MM-dd HH:mm in your JSON") as JSON);
						return;
					}
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					ride.pickupDateTime = simpleDateFormat.parse(dateTimeStr);
				} catch (ParseException e){
					render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,"DateTime must always use the format: yyyy-MM-dd HH:mm") as JSON);
					throw e;
				} catch (Throwable e) {
					render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
					throw e;
				}


				ride.rideStatus = RideStatus.UNASSIGNED;

				//Passenger
				assert principal.id != null, "Because this method is secured, a principal always exist at this point of the code"
				ride.passenger = Passenger.get(principal.id);


				//Addresses

				JSONObject pickUpLocationJsonObject = rideJsonObject.opt("pickUpLocation");
				log.info(pickUpLocationJsonObject);
				Location pickUpLocation = new Location(pickUpLocationJsonObject).save(flush:true, failOnError:true);
				ride.pickUpLocation = pickUpLocation;

				JSONObject dropOffLocationJsonObject = rideJsonObject.opt("dropOffLocation");
				log.info(dropOffLocationJsonObject);
				Location dropOffLocation = new Location(dropOffLocationJsonObject).save(flush:true, failOnError:true);
				ride.dropOffLocation = dropOffLocation;

				ride.save(flush:true, failOnError:true);

				List<DriverDistance> nearbyDrivers = locationService.findNearbyDrivers (pickUpLocation);
				for (nearbyDriver in nearbyDrivers) {
					notificationService.notifyDriversOfRideAvailable(nearbyDriver.driverId, nearbyDriver.distance, ride)
				}

				String msg = message(code: 'ride.created.message',
				args: [ride.id, nearbyDrivers.size() ])
				render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);
				return;
			} catch (OptimisticLockingFailureException e) {
				status.setRollbackOnly();
				//TODO: Refine locking messages
				render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
				return;
			} catch (ValidationException  e) {
				status.setRollbackOnly();
				render(utilService.getCallResultFromErrors (e.getErrors(), RequestContextUtils.getLocale(request)) as JSON);
				return;
			} catch (Throwable e) {
				status.setRollbackOnly();
				render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
				throw e;
			}

		}
	}

	/**
	 * This API retrieves all Rides created by the <code>Passenger</code> currently logged in.  Please note that a <code>User</code> 
	 * registered as a <code>Passenger</code> must be logged in. Otherwise a Not Authorized exception will be returned.
	 * @param  url  <server-name>/ride/retrievePassengerRides
	 * @param  input-sample {}
	 * @return output-sample {"rides":[{"id":1,"version":1,"rideStatus":"UNASSIGNED","driver":null,"passenger":{"id":5},"pickupDateTime":"2013-03-13 20:10","pickUpAddress":{"street":"123 Main St","city":"Wheanton","state":"IL","zip":"00001","addressType":"HOME"},"dropOffAddress":{"street":"123 Main St","city":"Wheanton","state":"IL","zip":"00001","addressType":"HOME"},"rating":null,"comments":null},{"id":2,"version":1,"rideStatus":"UNASSIGNED","driver":null,"passenger":{"id":5},"pickupDateTime":"2013-03-13 20:10","pickUpAddress":{"street":"123 Main St","city":"Wheanton","state":"IL","zip":"00001","addressType":"HOME"},"dropOffAddress":{"street":"123 Main St","city":"Wheanton","state":"IL","zip":"00001","addressType":"HOME"},"rating":null,"comments":null}]}
	 */
	@Secured(['ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'	])
	def retrievePassengerRides() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);


			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Passenger passenger = Passenger.get(principal.id);
			assert passenger != null, "Because this method is secured, a principal/passenger always exist at this point of the code"

			def c = Ride.createCriteria();

			def rides = c.list {
				and {
					eq("passenger", passenger)
				}
				maxResults(10)
				order("lastUpdated", "asc")
			}



			if(!rides) {
				def error = ['error':'No Rides Found']
				render "${error as JSON}"
			} else {
				render "{\"rides\":" + rides.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}


	/**
	 * This API retrieves all unassigned in the Metro Area of the <code>Driver</code> currently logged in.  Please note that a <code>User</code>
	 * registered as a <code>Driver</code> must be logged in. Otherwise a Not Authorized exception will be returned.
	 *
	 * Example: <server-name>/ride/retrieveUnassignedRideInDriverMetro
	 *
	 * {}
	 */
	@Secured(['ROLE_DRIVER','IS_AUTHENTICATED_FULLY'])
	def retrieveUnassignedRideInServedArea() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Driver driver = Driver.get(principal.id);
			assert driver != null, "Because this method is secured, a principal/driver always exist at this point of the code"
			
			log.info("About to compile list of nearby rides for " + driver.dump())
			log.info("The served location is " + driver.servedLocation.dump())
			log.info("The served location is " + Location.get(driver.servedLocation.id).dump())
			
			List<Ride> rides = new ArrayList<Ride>();
			List<RideDistance> nearbyRides = locationService.findNearbyRides (driver.servedLocation);
			for (nearbyRide in nearbyRides) {
				Ride ride = Ride.get(nearbyRide.rideId)
				rides.add(ride);
			}


			if(!rides) {
				render (new CallResult(CallResult.SYSTEM,CallResult.ERROR, 'No Rides Found') as JSON);
			} else {
				render "{\"rides\":" + rides.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}

	/**
	 * This API retrieves assigns a <code>Ride</code> to the <code>Driver</code> currently logged in.  Please note that a <code>User</code>
	 * registered as a <code>Driver</code> must be logged in. Otherwise a Not Authorized exception will be returned.
	 *
	 * Example: <server-name>/ride/retrieveUnassignedRideInDriverMetro
	 *
	 * {id=1, version=2}
	 */
	@Secured(['ROLE_DRIVER','IS_AUTHENTICATED_FULLY'])
	def assignRideToDriver() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject rideJsonObject = null;
		try {
			rideJsonObject = new JSONObject(model);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Driver driver = Driver.get(principal.id);
			assert driver != null, "Because this method is secured, a principal/driver always exist at this point of the code"

			Long id = rideJsonObject.getLong("id");
			Long version = rideJsonObject.getLong("version");

			Ride.withTransaction { status ->
				Ride ride = null;
				try {
					ride = Ride.get(id);

					//Before proceeding - check that this is a legitimate id
					if (!ride) {
						render (new CallResult(CallResult.SYSTEM, CallResult.ERROR, message (code: 'com.moovt.ride.not.found',args:[id])) as JSON);
						return;
					}
					//Before updating - check for concurrency
					if (ride.version > version) {
						render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')) as JSON);
						return;
					}

					if ((ride.rideStatus == RideStatus.ASSIGNED) || (ride.rideStatus == RideStatus.COMPLETED)) {
						render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.ride.already.assigned')) as JSON);
						return;
					}

					log.info("HERE");
					ride.driver = driver;
					ride.rideStatus = RideStatus.ASSIGNED;

					ride.save(flush:true, failOnError:true);
					notificationService.notifyDriverOfRideAssignment(ride);
					notificationService.notifyPassengerOfRideAssignment(ride);


					String msg = message(code: 'default.updated.message',
					args: [message(code: 'Ride.label', default: 'Ride'), ride.id])
					render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);

				} catch (OptimisticLockingFailureException e) {
					status.setRollbackOnly();
					//TODO: Refine locking messages
					render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
					return;
				} catch (ValidationException  e) {
					status.setRollbackOnly();

					render(utilService.getCallResultFromErrors (e.getErrors(), RequestContextUtils.getLocale(request)) as JSON);
					return;
				} catch (Throwable e) {
					status.setRollbackOnly();
					render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
					throw e;
				}

			}

		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}

	}

	/**
	 * This API retrieves assigns a <code>Ride</code> to the <code>Driver</code> currently logged in.  Please note that a <code>User</code>
	 * registered as a <code>Driver</code> must be logged in. Otherwise a Not Authorized exception will be returned.
	 *
	 * Example: <server-name>/ride/retrieveUnassignedRideInDriverMetro
	 *
	 * {id=1, version=2, rating=5, comment="Great driver!"}
	 */
	@Secured(['ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'])
	def closeRide() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject rideJsonObject = null;
		try {
			rideJsonObject = new JSONObject(model);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Passenger passenger = Passenger.get(principal.id);
			assert passenger != null, "Because this method is secured, a principal/passenger always exist at this point of the code"


			Long id = rideJsonObject.getLong("id");
			Long version = rideJsonObject.getLong("version");
			Integer rating = rideJsonObject.getInt("rating");
			if (!((0 <= rating) && (rating <=5))) {
				throw new Exception ("Rating must be between 0 and 5")
			}
			String comments = rideJsonObject.optString("comments","");

			Ride.withTransaction { status ->
				Ride ride = null;
				try {
					ride = Ride.get(id);

					//Before proceeding - check that this is a legitimate id
					if (!ride) {
						render (new CallResult(CallResult.SYSTEM, CallResult.ERROR, message (code: 'com.moovt.ride.not.found',args:[id])) as JSON);
						return;
					}
					//Before updating - check for concurrency
					if (ride.version > version) {
						log.info("Ride version is " + ride.version + "vs." + version);
						render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')) as JSON);
						return;
					}

					if (ride.rideStatus == RideStatus.COMPLETED) {
						render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.ride.already.completed')) as JSON);
						return;
					}

					log.info("HERE");
					ride.rating = rating;
					ride.comments = comments;
					ride.rideStatus = RideStatus.COMPLETED;

					ride.save(flush:true, failOnError:true);
					//notificationService.notifyDriverOfRideClosed(ride);
					//notificationService.notifyPassengerOfRideClosed(ride);


					String msg = message(code: 'default.updated.message',
					args: [message(code: 'Ride.label', default: 'Ride'), ride.id])
					render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);

				} catch (OptimisticLockingFailureException e) {
					status.setRollbackOnly();
					//TODO: Refine locking messages
					render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
					return;
				} catch (ValidationException  e) {
					status.setRollbackOnly();
					render(utilService.getCallResultFromErrors (e.getErrors(), RequestContextUtils.getLocale(request)) as JSON);
					return;
				} catch (Throwable e) {
					status.setRollbackOnly();
					render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
					throw e;
				}

			} //Transaction Close

		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}


	/**
	 * This API retrieves assigns a <code>Ride</code> to the <code>Driver</code> currently logged in.  Please note that a <code>User</code>
	 * registered as a <code>Driver</code> must be logged in. Otherwise a Not Authorized exception will be returned.
	 *
	 * Example: <server-name>/ride/cloneRide
	 *
	 * {id=1}
	 */
	@Secured(['ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'])
	def cloneRide() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject rideJsonObject = null;
		try {
			rideJsonObject = new JSONObject(model);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Passenger passenger = Passenger.get(principal.id);
			assert passenger != null, "Because this method is secured, a principal/driver always exist at this point of the code"

			Long id = rideJsonObject.getLong("id");

			Ride ride = Ride.get(id);

			//Before proceeding - check that this is a legitimate id
			if (!ride) {
				render (new CallResult(CallResult.SYSTEM, CallResult.ERROR, message (code: 'com.moovt.ride.not.found',args:[id])) as JSON);
				return;
			}

			Ride clonedRide = new Ride();
			clonedRide.properties = ride.properties;
			clonedRide.rideStatus = RideStatus.UNASSIGNED;

			render clonedRide.encodeAsJSON()


		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}

	}



	/**
	 * This API retrieves all Rides. Only administrators can perform this call.
	 * 
	 * Example: <server-name>/ride/retrieveAllRides
	 * 
	 * {}
	 */

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveAllRides() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.SYSTEM, e.message) as JSON);
			return;
		}
		try {
			def c = Ride.createCriteria();

			def rides = Ride.list();

			if(!rides) {
				def error = ['error':'No Rides Found']
				render "${error as JSON}"
			} else {
				render "{\"rides\":" + rides.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}

	}

	/**
	 * This API retrieves all Rides using the JSONP format. Only administrators can perform this call.
	 *
	 * Example: <server-name>/ride/retrieveAllRidesAsJSONP
	 *
	 * {}
	 */

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveAllRidesAsJSONP() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);

		try {
			def c = Ride.createCriteria();

			def rides = Ride.list();

			if(!rides) {
				def error = ['error':'No Rides Found']
				render "${error as JSON}"
			} else {
				render "${params.callback}(${rides as JSON})"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}

	}
}