package com.moovt.taxi

import com.moovt.CallResult;
import com.moovt.CustomGrailsUser
import com.moovt.common.Address
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


class RideController {

	def springSecurityService;
	
	def index() { }

	/*
	 * createRide
	 * 
	 *  localhost:8080/moovt/passenger/createOrUpdatePassengerUser
	 * 
	 * 	{"tenantname":"WorldTaxi","firstName":"John","lastName":"Goodrider","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"pt_BR","Address":{"street":"123 Main St","city":"Wheaton","state":"IL","zip":"60107","type":"Home"}}
	 * 
	 * 
	 *  http://localhost:8080/moovt/login/authenticateUser
	 * 
	 * {"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1",locale:"pt_BR"}
	 * 
	 *  localhost:8080/moovt/ride/createRide
	 *  
	 *  
	 * 
	 *  {"id":"6","version":"1","pickupTime":"12:30 PM","passenger":"3",
	 *  "pickUpAddress":{"street":"111 Main St","city":"Wheaton","state":"IL","zip":"60107","type":"Home"},
	 *  "dropOffAddress":{"street":"999 Main St","city":"Naperville","state":"IL","zip":"60107","type":"Home"}
	 *  }
	 */

	@Secured(['ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'	])
	def createRide() {
		
			   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	   	   CustomGrailsUser principal = auth.getPrincipal();
	   	   log.info("AUTHxxxxxx " + auth.getAuthorities());
	   	   log.info("AUTHENTICATED " + auth.isAuthenticated());
	   	   log.info("PRINCIPAL TENANT " + principal);
			  
			  
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject rideJsonObject = null;
		try {
			rideJsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		//Obtain the id, version from the json object
		Long id = rideJsonObject.optLong("id",0);
		Long version = rideJsonObject.optLong("version",0);
		
		if (id != 0) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,message (code: 'com.moovt.taxi.ride.id.not.allowed')) as JSON);
			return;
		}
		
		//Start a transaction to insert or update based on the existence of an id
		Ride.withTransaction { status ->
			Ride ride = null;
			try {
				if (id == 0) {
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
					log.info("HERE" + principal.id + "---" + Passenger.get(principal.id));
					ride.passenger = Passenger.get(principal.id);

					
					//Addresses
					
					JSONObject pickUpAddressJsonObject = rideJsonObject.opt("pickUpAddress");
					log.info(pickUpAddressJsonObject);
					Address pickUpAddress = new Address(pickUpAddressJsonObject);
					ride.pickUpAddress = pickUpAddress;
					
//					JSONObject dropOffAddressJsonObject = rideJsonObject.opt("dropOffAddress");
//					log.info(dropOffAddressJsonObject);
//					Address dropOffAddress = new Address(dropOffAddressJsonObject);
//					ride.dropOffAddress = dropOffAddress;
					
					log.info("READY " + ride.dump());
					
					ride.save(flush:true, failOnError:true);

					String msg = message(code: 'default.created.message',
					args: [message(code: 'Ride.label', default: 'Ride'), ride.username])
					render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);
					return;
				} else { //update
					log.info("Updating an existing ride");
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
					
					//If address provided, update the address
					JSONObject addressJsonObject = rideJsonObject.opt("Address");
					if (addressJsonObject) {
						ride.address.properties = addressJsonObject;
					}
					ride.properties = rideJsonObject;
					ride.lastUpdatedBy = ride.id; //In the taxi app, the ride updates itself
					ride.save(flush:true, failOnError:true);
					String msg = message(code: 'default.updated.message',
					args: [message(code: 'Ride.label', default: 'Ride'), ride.username])
					render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);
				}

			} catch (OptimisticLockingFailureException e) {
				status.setRollbackOnly();
				//TODO: Refine locking messages
				render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
				return;
			} catch (ValidationException  e) {
				status.setRollbackOnly();
				render(CallResult.getCallResultFromErrors (e.getErrors(), RequestContextUtils.getLocale(request)) as JSON);
				return;
			} catch (Throwable e) {
				status.setRollbackOnly();
				render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
				throw e;
			}

		}
	}


}

