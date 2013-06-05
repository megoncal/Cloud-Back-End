package com.moovt.common

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.context.MessageSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.validation.FieldError
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.*

import com.moovt.CallResult;
import com.moovt.CustomGrailsUser
import com.moovt.NotificationService
import com.moovt.QueryUtils
import com.moovt.TenantAuthenticationToken
import com.moovt.UUIDWrapper;
import com.moovt.UtilService
import com.moovt.common.Role;
import com.moovt.common.Tenant;
import com.moovt.common.User;
import com.moovt.common.UserRole;
import com.moovt.taxi.Driver
import com.moovt.taxi.Passenger
import com.moovt.LogUtils

import grails.validation.ValidationException
import java.util.UUID
import grails.plugins.springsecurity.Secured

import org.springframework.security.core.context.SecurityContextHolder;

import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.web.servlet.support.RequestContextUtils;

import org.springframework.security.core.AuthenticationException

class UserController {

	MessageSource messageSource; //inject the messageSource bean
	UtilService utilService; //inject the utilService bean
	AuthenticationManager authenticationManager; //injection of the authenticationManager required to log in the recently created user
	NotificationService notificationService;


	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]


	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}

	/**
	 * This API creates a <code>User</code>, signin the user and returns JSESSIONID.
	 * The User can be either a <code>Passenger</code> or a <code>Driver</code>. It does not require the user to be
	 * signed in.
	 * 
	 * @param  url   <server-name>/user/createUser
	 * @param  input-sample-1 {"tenantname":"WorldTaxi","firstName":"David","lastName":"Ultrafast","username":"dultrafast","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType":"A_SEDAN","servedLocation":{"locationName":"Rua Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},"activeStatus":"ENABLED"}}
	 * @param  input-sample-2 {"tenantname":"WorldTaxi","firstName":"John","lastName":"Airjunkie","username":"jairjunkie","password":"Welcome!1","phone":"773-329-1784","email":"jairjunkie@worldtaxi.com","locale":"en-US","passenger":{}}}
	 * @return output-sample {"type":"USER","code":"SUCCESS","message":"User dultrxafast created","JSESSIONID":"6317F9E37E0F499399A3F89DDA6D5723"}
	 *
	 */
	def createUser() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject userJsonObject = null;
		try {
			userJsonObject = new JSONObject(model);



			//For methods without the need of authentication, verify if the tenant exists
			String tenantname = userJsonObject.optString("tenantname","");

			Tenant tenant = Tenant.findByName(tenantname);
			if (!tenant) {
				String msgTenantDoesNotExist = message(code: 'com.moovt.UserController.badTenant',args: [ tenantname ])
				log.warn (msgTenantDoesNotExist)
				render new CallResult(CallResult.SYSTEM, CallResult.ERROR, msgTenantDoesNotExist).getJSON();
				return;
			}

			//Before any further operation, capture the password in its raw format
			String password = userJsonObject.optString("password","");

			//Upon successful completion of the transaction below, this user will be populated
			User user;

			User.withTransaction { status ->
				log.info("Creating a new user and/or driver and/or passenger");
				user = new User(userJsonObject);
				user.tenantId = tenant.id;
				user.createdBy = 1;
				user.lastUpdatedBy = 1;
				user.save(failOnError:true);

				//Update the createdBy and lastUpdatedBy to self
				user.createdBy = user.id;
				user.lastUpdatedBy = user.id;
				user.save(failOnError:true)

				//Handle Driver type
				JSONObject driverJSON = userJsonObject.opt("driver");
				if (driverJSON) {
					//Handle the driver's served location inside the driver JSON
					JSONObject servedLocationJSON = driverJSON.opt("servedLocation");
					if (!servedLocationJSON) {
						status.setRollbackOnly();
						render new CallResult(CallResult.SYSTEM,CallResult.ERROR, 'A servedLocation JSON element must existing inside a driver JSON').getJSON();
						return;
					}
					Location servedLocation = new Location (servedLocationJSON);
					servedLocation.tenantId = tenant.id;
					servedLocation.createdBy = user.id;
					servedLocation.lastUpdatedBy = user.id;
					servedLocation.save(failOnError:true);

					user.driver = new Driver (driverJSON);
					user.driver.servedLocation = servedLocation;
					user.driver.tenantId = tenant.id;
					user.driver.createdBy = user.id;
					user.driver.lastUpdatedBy = user.id;

					def driverRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_DRIVER')
					UserRole.create (tenant.id, user, driverRole, user.createdBy, user.lastUpdatedBy);
				}

				//Handle Passenger type
				JSONObject passengerJSON = userJsonObject.opt("passenger");
				if (passengerJSON) {
					user.passenger = new Passenger (passengerJSON);
					user.passenger.tenantId = tenant.id;
					user.passenger.createdBy = user.id;
					user.passenger.lastUpdatedBy = user.id;

					// Assign the passenger role
					def passengerRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_PASSENGER')
					UserRole.create (tenant.id, user, passengerRole, user.createdBy, user.lastUpdatedBy);
				}

				user.save(failOnError:true);

				//Authenticate this user, please note the use of password in its non-encoded format
				TenantAuthenticationToken token = new TenantAuthenticationToken(user.username, password,user.tenantname, user.locale);
				Authentication auth = authenticationManager.authenticate(token);
				SecurityContextHolder.getContext().setAuthentication(auth);


			}// End of the Transaction

			String sessionId = session.getId();
			String msg = message(code: 'default.created.message',
			args: [message(code: 'User.label', default: 'User'), user.username ])

			utilService.handleSuccessWithSessionId(msg, sessionId);

		} catch (Throwable e) {
			utilService.handleException(e);
		}
	}

	/**
	 * This API updates the <code>User</code> that is currently logged in and returns the details of the just updated User
	 * @param  url   <server-name>/user/updateLoggedUser
	 * @param  input-sample-1 {"version":"4","firstName":"John","lastName":"VeryGoodarm","username":"jverygooxdarm","password":"Welcome!1","phone":"773-329-1784","email":"jgoodarxm@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":{"locationName":"Rua Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},"activeStatus":"ENABLED"}}
	 * @param  {"version":"7","firstName":"John","lastName":"DecidedToBeADriver","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","activeStatus":"ENABLED"}}
	 * @return output-sample 
	 * 
	 */	@Secured(['ROLE_DRIVER', 'ROLE_PASSENGER', 'IS_AUTHENTICATED_FULLY'])
	def updateLoggedUser() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject userJsonObject = null;
		try {
			userJsonObject = new JSONObject(model);

			//Obtain version from the json object
			Long version = userJsonObject.optLong("version",0);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Tenant tenant = Tenant.get(principal.tenantId);

			User user;

			//Start a transaction to update based on the existence of an id

			User.withTransaction { status ->

				user = User.get(principal.id);
				assert user != null, "Because this method is secured, a principal/user always exist at this point of the code"

				log.info("Retrieved User " + user.dump());

				//Before updating - check for concurrency
				if (user.version > version) {
					render new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')).getJSON();
					return;
				}
				user.properties = userJsonObject;

				//Handle Driver type
				JSONObject driverJSON = userJsonObject.opt("driver");

				if (driverJSON) {
					//Handle the driver's served location inside the driver JSON
					JSONObject servedLocationJSON = driverJSON.opt("servedLocation");
					if (!servedLocationJSON) {
						status.setRollbackOnly();
						render new CallResult(CallResult.SYSTEM,CallResult.ERROR, 'A servedLocation JSON element must existing inside a driver JSON').getJSON();
						return;
					}

					Driver driver = user.driver;
					if (!driver) {
						driver = new Driver(driverJSON);
						Location servedLocation = new Location(servedLocationJSON);
						servedLocation.save(failOnError:true);
						driver.servedLocation = servedLocation;
						driver.user = user
					} else {
						driver.servedLocation.properties = servedLocationJSON;
						driver.properties = driverJSON;
					}

					log.info("Driver being saved " + driver.dump());
					driver.save(failOnError:true);
					
					user.driver = driver;

					//Assign the driver role
					def driverRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_DRIVER')
					if (!user.authorities.contains(driverRole)) {
						UserRole.create ( tenant.id, user, driverRole)
					}

				}

				//Handle Passenger type
				JSONObject passengerJSON = userJsonObject.opt("passenger");
				if (passengerJSON) {
					Passenger passenger = user.passenger;
					if (!passenger) {
						passenger = new Passenger(passengerJSON);
						passenger.user = user
					} else {
						passenger.properties = passengerJSON
					}

					log.info("Passenger being saved " + driver.dump());
					passenger.save(failOnError:true);
					user.passenger = passenger;


					// Assign the passenger role
					def passengerRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_PASSENGER')
					if (!user.authorities.contains(passengerRole)) {
						UserRole.create ( tenanat.id, user, passengerRole)
					}
				}
				log.info("User being saved " + user.dump());

			} //Closing the transaction and saving

			String msg = message(code: 'default.updated.message',
			args: [message(code: 'User.label', default: 'User'), user.username])
			render "{\"result\":" + new CallResult(CallResult.USER, CallResult.SUCCESS, msg).encodeAsJSON() + ", " +"\"user\":" + user.encodeAsJSON() + "}";

		} catch (Throwable e) {
			utilService.handleException(e);
		}
	}

	/**
	 * This API retrieves all Users. This API is only available to users with the Administrator privilege.
	 * 
	 * @param  url   <server-name>/user/retrieveAllUsers
	 * @param  input-sample {}
	 * @return output-sample {"users":[{"id":4,"version":1,"firstName":"Admin","lastName":"Admin","phone":"800-800-8080","email":"admin@worldtaxi.com"},{"id":5,"version":1,"firstName":"John","lastName":"Goodrider","phone":"800-800-8080","email":"jgoodrider@worldtaxi.com","passenger":{"id":5}},{"id":6,"version":1,"firstName":"John","lastName":"Goodarm","phone":"800-800-2020","email":"jgoodarm@worldtaxi.com","driver":{"id":6,"servedMetro":"Chicago-Naperville-Joliet, IL","carType":"VAN"}}]}
	 */
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveAllUsers() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();
			Tenant tenant = Tenant.get(principal.tenantId);

			def c = User.createCriteria();

			def users = c.list {
				and {
					eq("tenantId",tenant.id)
				}
				order("lastUpdated", "desc")
			}

			if(!users) {
				utilService.handleUserError(message (code: 'com.moovt.common.User.notFound'));
			} else {
				render "{\"users\":" + users.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			utilService.handleException(e);
		}
	}


	/**
	 * This API is retrieves a User by id. This API is only available to users with the Administrator privilege.
	 *  
	 * @param  url   <server-name>/user/retrieveUserDetailById
	 * @param  input-sample {"id":"6"}
	 * @return output-sample {"user":{"id":6,"version":1,"firstName":"John","lastName":"Goodarm","phone":"800-800-2020","email":"jgoodarm@worldtaxi.com","driver":{"id":6,"servedMetro":"Chicago-Naperville-Joliet, IL","carType":"VAN"}}}
	 */

	//TODO: Create test conditions for this

	//	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveUserDetailById() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);

			Long id = jsonObject.optLong("id",0);

			def c = User.createCriteria();

			def user = c.get {
				and {
					//eq("tenantId",tenant.id)
					eq("id",id)
				}
				order("lastUpdated", "desc")
			}


			if(!user) {
				def error = ['error':'No User Found']
				utilService.handleUserError(message (code: 'com.moovt.common.User.notFound'));
			} else {
				render "{\"user\":" + user.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			utilService.handleException(e);
		}


	}

	/**
	 * This API retrieves the details of the user currently logged in. It is useful to obtain the id of the user for use in subsequent calls.
	 * 
	 * @param  url   <server-name>/user/retrieveLoggedUserDetails
	 * @param  input-sample {}
	 * @return output-sample {"user":{"id":5,"version":1,"firstName":"John","lastName":"Goodrider","phone":"800-800-8080","email":"jgoodrider@worldtaxi.com","passenger":{"id":5}}}
	 *
	 */

	@Secured(['ROLE_DRIVER','ROLE_PASSENGER','IS_AUTHENTICATED_FULLY'	])
	def retrieveLoggedUserDetails() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);


			//The User
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomGrailsUser principal = auth.getPrincipal();

			User user = User.get(principal.id);
			assert user != null, "Because this method is secured, a principal always exist at this point of the code"

			render "{\"user\":" + user.encodeAsJSON() + "}"
		} catch (Throwable e) {
			utilService.handleException(e);
		}


	}

	/**
	 * This API updates the password and sends an email to the user.
	 *
	 * @param  url   <server-name>/user/resetPassword
	 * @param  input-sample-1 {"tenantname":"WorldTaxi","email":"dultrafast@worldtaxi.com"}
	 * @return output-sample {"type":"SYSTEM","code":"SUCCESS","message":"User dultrxafast created","JSESSIONID":"6317F9E37E0F499399A3F89DDA6D5723"}
	 *
	 */
	def resetPassword() {

		//Obtain tenantname and email from input message
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		String tenantname = "";
		String email = "";
		try {
			JSONObject jsonObject = new JSONObject(model);
			tenantname = jsonObject.getString("tenantname");
			email = jsonObject.getString("email");

			//For methods without the need of authentication, verify if the tenant exists
			Tenant tenant = Tenant.findByName(tenantname);
			if (!tenant) {
				String msgTenantDoesNotExist = message(code: 'com.moovt.UserController.badTenant',args: [ tenantname ])
				log.warn (msgTenantDoesNotExist)
				render new CallResult(CallResult.SYSTEM, CallResult.ERROR, msgTenantDoesNotExist).getJSON();
				return;
			}

			//Find the user
			def c = User.createCriteria();

			User user = c.get {
				and {
					eq("tenantId",tenant.id)
					eq("email",email)
				}
			}

			if (!user) {
				String msgEmailDoesNotExist = message(code: 'com.moovt.UserController.emailNotFound',args: [ email ])
				utilService.handleUserError(msgEmailDoesNotExist);
				return;
			}

			//Update the user and notify
			String resetPassword = grailsApplication.config.moovt.resetPassword;
			user.password = resetPassword;

			User.withTransaction { status ->
				user.save()

				notificationService.notifyPasswordChanged (user, resetPassword);
			}
			String msg = message(code: 'com.moovt.UserController.newPasswordSent',
			args: [ email ])
			utilService.handleSuccess(msg);
		} catch (Throwable e) {
			utilService.handleException(e);
		}
	}

}


