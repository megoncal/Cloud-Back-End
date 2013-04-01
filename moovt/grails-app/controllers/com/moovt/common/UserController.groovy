package com.moovt.common

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.Authentication
import org.springframework.validation.FieldError
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.*

import com.moovt.CallResult;
import com.moovt.CustomGrailsUser
import com.moovt.QueryUtils
import com.moovt.TenantAuthenticationToken
import com.moovt.UUIDWrapper;
import com.moovt.common.Role;
import com.moovt.common.Tenant;
import com.moovt.common.User;
import com.moovt.common.UserRole;
import com.moovt.taxi.Driver
import com.moovt.taxi.Passenger

import grails.validation.ValidationException
import java.util.UUID
import grails.plugins.springsecurity.Secured

import org.springframework.security.core.context.SecurityContextHolder;

import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.web.servlet.support.RequestContextUtils;

import org.springframework.security.core.AuthenticationException

class UserController {

	def messageSource; //inject the messageSource bean
	def authenticationManager; //injection of the authenticationManager required to log in the recently created user

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}

	/**
	 * This API is not currently supported.
	 *
	 * Example: <server-name>/user/upsert
	 *
	 */
	//This method updates or insert an array of JSON objects
	@Secured([
		'ROLE_ADMIN',
		'IS_AUTHENTICATED_FULLY'
	])
	def upsert() {

		String models = request.reader.getText();

		//TODO: Error Handling if unable to retrieve models

		log.info("Upserting with params:" + params + "and models: " + models);

		JSONArray jsonArray = new JSONArray(models);


		ArrayList returnUsers = new ArrayList();

		jsonArray.each () {
			JSONObject jsonObject = jsonArray[0];

			//Obtain the id and version from the json object
			def id = jsonObject.get("id");
			def version = jsonObject.get("version");

			log.info("JSON Object to update is " + jsonObject + " with id " + id + " and version " + version + ".");


			//Create a new user or retrieve the user from the database
			User userInstance = null;

			if ((id.equals(null)) || (id.equals(''))) {
				log.info("Creating a new user");
				userInstance = new User();
			} else {
				log.info("Updating and existing user");
				userInstance = User.get(id);
			}

			//Check for optimistic locking
			//TODO: Add a user to user and indicate which user updated the version
			if (!version.equals(null)) {
				if (userInstance.version > version) {
					userInstance.CRUDMessage="Another user udpate this record"''
				}
			}

			userInstance.properties = jsonObject;


			//Validate and if valid save
			//This method returns the saved user to the client
			if (userInstance.validate()) {
				userInstance.save(flush: true);
				userInstance.CRUDMessage = 'OK';
				returnUsers.add(userInstance);
			} else {
				//TODO: Create descriptive message
				userInstance.CRUDMessage = 'NOK';
			}

		}

		log.info("Rendering saved user as JSON Array " + "${params.callback}(${returnUsers as JSON})");
		render "${params.callback}(${returnUsers as JSON})";


	}

	/**
	 * This API is not currently supported.
	 *
	 * Example: <server-name>/user/delete
	 *
	 */

	@Secured(['ROLE_ADMIN',	'IS_AUTHENTICATED_FULLY'])
	def delete() {
		log.info("Delete with params:" + params + "and request: " + request.reader.getText());
		//Assume that the parameters contain a Kendo UI array of models
		String modelParam = params.get("models");
		Boolean arrayMode = true;
		if (!modelParam) {
			arrayMode = true;
		}

		//Obtain one json object directly or through the array
		JSONObject jsonObject;

		if (arrayMode) {
			JSONArray jsonArray = new JSONArray(modelParam);
			jsonObject = jsonArray[0];
		} else {
			jsonObject = request.JSON;
		}

		//Obtain the id and version from the json object
		def id = jsonObject.get("id");
		def version = jsonObject.get("version");

		log.info("JSON Object to delete is " + jsonObject + " with id " + id + " and version " + version + ".");


		//Create a new user or retrieve the user from the database
		User userInstance = User.get(id);

		if (!userInstance) {
			def error = ['error':"User invalid " + userInstance.toString()]
			render "${params.callback}(${error as JSON})"
		}

		//Check for optimistic locking
		//TODO: Add a user to user and indicate which user updated the version
		if (!version.equals(null)) {
			if (userInstance.version > version) {
				def error = ['error':"Another user udpate "]
				render "${params.callback}(${error as JSON})"
			}
		}


		//If we got to this point, delete user
		try {
			userInstance.delete(flush: true)
			render "${params.callback}(${userInstance as JSON})"
		}
		catch (DataIntegrityViolationException e) {
			def error = ['error':"User invalid " + userInstance.toString()]
			render "${params.callback}(${error as JSON})"
		}


	}

	/**
	 * This API is not currently supported.
	 *
	 * Example: <server-name>/user/saveImage
	 *
	 */
	@Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
	def saveImage={

		log.info(request.contentType);
		CommonsMultipartFile file = request.getFile('image');
		String path = grailsApplication.config.imageStoreDir;
		UUID uuid = UUID.randomUUID();
		UUIDWrapper uuidWrapper = new UUIDWrapper(uuid);
		String fileName = path+uuid+".jpg";

		log.info("Storing File  " + fileName);
		if (file && !file.empty) {
			file.transferTo(new File(fileName));
			//TODO: Determine the difference between these two statements
			//render "${params.callback}(${uuidWrapper as JSON})"
			render "${uuidWrapper as JSON}"
		}
	}

	/**
	 * This API creates a <code>User</code>, signin the user and returns JSESSIONID.
	 * The User can be either a <code>Passenger</code> or a <code>Driver</code>. It does not require the user to be
	 * signed in.
	 * 
	 * @param  url   <server-name>/user/createUser
	 * @param  input-sample-1 {"tenantname":"WorldTaxi","firstName":"David","lastName":"Ultrafast","username":"dultrafast","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","radiusServed":"RADIUS_50","activeStatus":"ENABLED"}}
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
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}



		//For methods without the need of authentication, verify if the tenant exists
		String tenantname = userJsonObject.optString("tenantname","");
		
		Tenant tenant = Tenant.findByName(tenantname);
		if (!tenant) {
			String msgTenantDoesNotExist = message(code: 'com.moovt.UserController.badTenant',args: [ tenantname ])
			log.warn (msgTenantDoesNotExist)
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR, msgTenantDoesNotExist) as JSON);
			return;
		}
		
		//Before any further operation, capture the password in its raw format
		String password = userJsonObject.optString("password","");

		//Start a transaction to insert or update based on the existence of an id
		User.withTransaction { status ->
			try {
				log.info("Creating a new user and/or driver and/or passenger");
				User user = new User(userJsonObject);
				user.tenantId = tenant.id;
				user.createdBy = 1;
				user.lastUpdatedBy = 1;
				user.save(flush:true, failOnError:true);

				//Update the createdBy and lastUpdatedBy to self
				user.createdBy = user.id;
				user.lastUpdatedBy = user.id;
				user.save(flush:true, failOnError:true)

				//Handle Driver type
				JSONObject driverJSON = userJsonObject.opt("driver");
				if (driverJSON) {
					user.driver = new Driver (driverJSON);
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

				user.save(flush:true, failOnError:true);

				//Authenticate this user, please note the use of password in its non-encoded format
				TenantAuthenticationToken token = new TenantAuthenticationToken(user.username, password,user.tenantname, user.locale);
				Authentication auth = authenticationManager.authenticate(token);
				SecurityContextHolder.getContext().setAuthentication(auth);
				String sessionId = session.getId();

				String msg = message(code: 'default.created.message',
				args: [message(code: 'User.label', default: 'User'), user.username ])

				render([type: CallResult.USER, code: CallResult.SUCCESS, message: msg, JSESSIONID: sessionId] as JSON);
				return;
				
			} catch (OptimisticLockingFailureException e) {
				status.setRollbackOnly();
				//TODO: Refine locking messages
				render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
				throw e;
				return;
			} catch (ValidationException  e)  {
				status.setRollbackOnly();
				render(CallResult.getCallResultFromErrors (e.getErrors(), RequestContextUtils.getLocale(request)) as JSON);
				throw e;
				return;
			}  catch (AuthenticationException e) {
				status.setRollbackOnly();
				render(new CallResult(CallResult.USER, CallResult.ERROR, e.message) as JSON);
				throw e;
				return;
			}
			catch (Throwable e) {
				status.setRollbackOnly();
				render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
				throw e;
			}

		}
	}

	/**
	 * This API updates the <code>User</code> that is currently logged in and returns the details of the just updated User
	 * @param  url   <server-name>/user/updateLoggedUser
	 * @param  input-sample-1 {"version":"4","firstName":"John","lastName":"VeryGoodarm","username":"jverygooxdarm","password":"Welcome!1","phone":"773-329-1784","email":"jgoodarxm@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","radiusServed":"RADIUS_50","activeStatus":"ENABLED"}}
	 * @param  {"version":"7","firstName":"John","lastName":"DecidedToBeADriver","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","radiusServed":"RADIUS_50","activeStatus":"ENABLED"}}
	 * @return output-sample 
	 * 
	 */	@Secured(['ROLE_DRIVER', 'ROLE_PASSENGER', 'IS_AUTHENTICATED_FULLY'])
	def updateLoggedUser() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject userJsonObject = null;
		try {
			userJsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		//Obtain version from the json object
		Long version = userJsonObject.optLong("version",0);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		Tenant tenant = Tenant.get(principal.tenantId);


		//Start a transaction to update based on the existence of an id
		User.withTransaction { status ->
			try {
				User user = User.get(principal.id);
				assert user != null, "Because this method is secured, a principal/user always exist at this point of the code"

				log.info("Retrieved User " + user.dump());

				//Before updating - check for concurrency
				if (user.version > version) {
					render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')) as JSON);
					return;
				}
				user.properties = userJsonObject;

				//Handle Driver type
				JSONObject driverJSON = userJsonObject.opt("driver");

				if (driverJSON) {
					Driver driver = user.driver;
					if (!driver) {
						driver = new Driver(driverJSON);
						driver.user = user
					} else {
						driver.properties = driverJSON
					}

					driver.save(flush:true, failOnError:true);

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

					passenger.save(flush:true, failOnError:true);

					user.passenger = passenger;


					// Assign the passenger role
					def passengerRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_PASSENGER')
					if (!user.authorities.contains(passengerRole)) {
						UserRole.create ( tenanat.id, user, passengerRole)
					}
				}

				log.info("Prior to Saving")
				user.save(flush:true, failOnError:true)

				String msg = message(code: 'default.updated.message',
				args: [message(code: 'User.label', default: 'User'), user.username])
				render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);


			} catch (OptimisticLockingFailureException e) {
				status.setRollbackOnly();
				//TODO: Refine locking messages
				render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
				return;
			} catch (ValidationException  e)  {
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
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		Tenant tenant = Tenant.get(principal.tenantId);

		try {
			def c = User.createCriteria();

			def users = c.list {
				and {
					eq("tenantId",tenant.id)
				}
				order("lastUpdated", "desc")
			}

			if(!users) {
				def error = ['error':'No Users Found']
				render "${error as JSON}"
			} else {
				render "{\"users\":" + users.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}

	/**
	 * This API retrieves all Users. This API is only available to users with the Administrator privilege.
	 *
	 * The return users using the JSONP convention. Please use retrieveAllUsers instead.
	 *
	 */
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveAllUsersAsJSONP() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);


		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		Tenant tenant = Tenant.get(principal.tenantId);

		try {
			def c = User.createCriteria();

			def users = c.list {
				and {
					eq("tenantId",tenant.id)
				}
				order("lastUpdated", "desc")
			}

			if(!users) {
				def error = ['error':'No Users Found']
				render "${error as JSON}"
			} else {
				render "${params.callback}(${users as JSON})"
			}
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}

	/**
	 * This API is retrieves a User by id. This API is only available to users with the Administrator privilege.
	 *  
	 * @param  url   <server-name>/user/retrieveUserDetailById
	 * @param  input-sample {"id":"6"}
	 * @return output-sample {"user":{"id":6,"version":1,"firstName":"John","lastName":"Goodarm","phone":"800-800-2020","email":"jgoodarm@worldtaxi.com","driver":{"id":6,"servedMetro":"Chicago-Naperville-Joliet, IL","carType":"VAN"}}}
	 */

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveUserDetailById() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		Tenant tenant = Tenant.get(principal.tenantId);

		try {

			Long id = jsonObject.optLong("id",0);

			def c = User.createCriteria();

			def user = c.get {
				and {
					eq("tenantId",tenant.id)
					eq("id",id)
				}
				order("lastUpdated", "desc")
			}


			if(!user) {
				def error = ['error':'No User Found']
				render "${error as JSON}"
			} else {
				render "{\"user\":" + user.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
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
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}


		//The User
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();

		try {

			User user = User.get(principal.id);
			assert user != null, "Because this method is secured, a principal always exist at this point of the code"

			render "{\"user\":" + user.encodeAsJSON() + "}"
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}


	}



}

