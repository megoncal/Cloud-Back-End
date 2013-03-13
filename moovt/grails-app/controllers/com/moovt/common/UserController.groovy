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

class UserController {

	def messageSource; //inject the messageSource bean

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}


	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def search() {

		log.info("Searching with params:" + params + "and request: " + request.reader.getText());

		params.max = Math.min(params.max ? params.int('max') : 5, 100)


		try {
			def c = User.createCriteria();

			def users = c.list (QueryUtils.c_c(params));

			if(!users) {
				render(new CallResult(CallResult.USER,CallResult.ERROR,"No users found") as JSON);		
			} else {
				render "{\"users\":" + users.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}


	}

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

	/*
	 * createUser
	 * 
	 * Creates a user and the related type of user object such as a driver and or a passenger
	 * 
	 * To create a Drive use this JSON as and example
	 * 
	 * {"tenantname":"WorldTaxi","firstName":"David","lastName":"Ultrafast","username":"dultrafast","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType":"Sedan","servedMetro":"Chicago-Naperville-Joliet, IL","activeStatus":"ENABLED"}}
	 * 
	 * To create a Passenger, use this JSON as an example
	 * 
	 * {"tenantname":"WorldTaxi","firstName":"John","lastName":"Airjunkie","username":"jairjunkie","password":"Welcome!1","phone":"773-329-1784","email":"jairjunkie@worldtaxi.com","locale":"en-US","passenger":{}}}
	 *
	 * To update include the id and version in the JSON e.g. "id":"456","version":"7"
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
					log.info("000");
					Driver driver = new Driver (driverJSON);
					log.info("222 " + driver.dump());
					driver.id = user.id;
					driver.tenantId = tenant.id;
					driver.createdBy = user.id;
					driver.lastUpdatedBy = user.id;
					driver.save(flush:true, failOnError:true)
					log.info("45454");
					// Assign the driver role
					def driverRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_DRIVER')
					UserRole.create (tenant.id, user, driverRole, driver.createdBy, driver.lastUpdatedBy);
					log.info("333");
				}

				//Handle Passenger type
				JSONObject passengerJSON = userJsonObject.opt("passenger");
				if (passengerJSON) {
					Passenger passenger = new Passenger (passengerJSON);
					passenger.id = user.id;
					passenger.tenantId = tenant.id;
					passenger.createdBy = user.id;
					passenger.lastUpdatedBy = user.id;
					passenger.save(flush:true, failOnError:true)

					// Assign the passenger role
					def passengerRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_PASSENGER')
					UserRole.create (tenant.id, user, passengerRole, driver.createdBy, driver.lastUpdatedBy);
				}
				String msg = message(code: 'default.created.message',
				args: [message(code: 'User.label', default: 'User'), user.username ])
				render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);
				return;
			} catch (OptimisticLockingFailureException e) {
				status.setRollbackOnly();
				//TODO: Refine locking messages
				render(new CallResult(CallResult.USER,CallResult.ERROR,message (code: 'com.moovt.concurrent.update')) as JSON);
				return;
			} catch (ValidationException  e)  {
				status.setRollbackOnly();
				log.info("4444");
				render(CallResult.getCallResultFromErrors (e.getErrors(), RequestContextUtils.getLocale(request)) as JSON);
				return;
			} catch (Throwable e) {log.info("3333");
				status.setRollbackOnly();
				render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
				throw e;
			}

		}
	}

	/*
	 * user/updateLoggedUser
	 *
	 * Creates or updates a user and the related type of user object such as a driver and or a passenger
	 *
	 * To update a Drive use this JSON as and example
	 *
	 * {"version":"2","firstName":"John","lastName":"VeryGoodarm","username":"jverygoodarm","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType":"VAN","servedMetro":"Chicago-Naperville-Joliet, IL","activeStatus":"ENABLED"}}
	 *
	 * To update a Passenger, use this JSON as an example
	 *
	 * {"version":"2","firstName":"UpdatedJohn","lastName":"Goodrider","username":"jgodrider","password":"Welcome!1","locale":"en-US","passenger":{}}}
	 *
	 *
	 */
	@Secured(['ROLE_DRIVER', 'ROLE_PASSENGER', 'IS_AUTHENTICATED_FULLY'])
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

		//The Driver
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();
		Tenant tenant = Tenant.get(principal.tenantId);

		log.info(">>>>" + principal.dump());


		//Start a transaction to update based on the existence of an id
		User.withTransaction { status ->
			try {
				User user = User.get(principal.id);
				assert user != null, "Because this method is secured, a principal/user always exist at this point of the code"

				//Before updating - check for concurrency
				if (user.version > version) {
					render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')) as JSON);
					return;
				}
				user.properties = userJsonObject;
				user.lastUpdatedBy = user.id; //In the taxi app, the user updates itself
				user.save(flush:true, failOnError:true);


				//Handle Driver type
				JSONObject driverJSON = userJsonObject.opt("driver");
				if (driverJSON) {
					Driver driver = Driver.get(principal.id);

					log.info("llllllllllllllllll" + driver.activeStatus);

					if (!driver) {
						driver = new Driver();
					}
					driver.properties = driverJSON
					driver.id = user.id;
					log.info("llllllllllllllllll" + driver.dump());
					driver.save(flush:true, failOnError:true)

					// Assign the driver role
					def driverRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_DRIVER')
					if (!user.authorities.contains(driverRole)) {
						UserRole.create ( tenant.id, user, driverRole)
					}

				}

				//Handle Passenger type
				JSONObject passengerJSON = userJsonObject.opt("passenger");
				if (passengerJSON) {
					Passenger passenger = Passenger.get(principal.id);
					if (!passenger) {
						passenger = new Passenger();
					}
					passenger.properties = passengerJSON
					passenger.id = user.id;
					passenger.save(flush:true, failOnError:true)

					// Assign the passenger role
					def passengerRole = Role.findByTenantIdAndAuthority(tenant.id, 'ROLE_PASSENGER')
					if (!user.authorities.contains(passengerRole)) {
						UserRole.create ( tenanat.id, user, passengerRole)
					}
				}

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
	
	/*
	 * user/retrieveAllUsers
	 *
	 * Retrieves all users and associated information about the user type (e.g. driver, passenger, etc
	 * 
	 * An example JSON input is {}
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

		try {
			def users = User.list();

			if(!users) {
				def error = ['error':'No Users Found']
				render "${error as JSON}"
			} else {
				render "{\"users\":" + users.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}

	/*
	 * user/retrieveUserDetailById
	 *
	 * Retrieves all users and associated information about the user type (e.g. driver, passenger, etc
	 * 
	 * An example JSON input is {"id","1"}
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

		try {
			Long id = jsonObject.optLong("id",0);

			User user = User.get(id);
			if(!user) {
				def error = ['error':'No User Found']
				render "${error as JSON}"
			} else {
				render "{\"user\":" + user.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}


	}

	/*
	 * user/retrieveLoggedUserDetails
	 *
	 * Retrieves all the details about the user currently logged in
	 * 
	 * An example JSON input is {}
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
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}


	}



}

