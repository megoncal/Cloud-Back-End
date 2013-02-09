package com.moovt

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.validation.FieldError
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.*
import com.moovt.QueryUtils

import java.util.UUID
import grails.plugins.springsecurity.Secured

class UserController {

	def messageSource; //inject the messageSource bean

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}
	
	/*
	* createUserInExistingTenant
	* Input
	*		tenantname (naSavassi)
	*		username
	* 		password
	*      locale
	*
	*
	* Output
	*		code (SUCCESS or ERROR)
	*		msg
	*/
   def createUserInExistingTenant() {
	   String model = request.reader.getText();
	   log.info(this.actionName + " params are: " + params + " and model is : " + model);
	   JSONObject jsonObject = null;
	   try {
		   jsonObject = new JSONObject(model);
	   } catch (Exception e) {
		   render([code: "ERROR", msg: e.message ] as JSON);
		   return;
	   }
	   
	   User aUser = new User(jsonObject);
	   
	   
	   //Verify if the tenant exists
	   log.info("Verify if the tenant exists");
	   Tenant aTenant = Tenant.findByName(aUser.tenantname);
	   if (!aTenant) {
		   String msgTenantDoesNotExist = message(code: 'com.moovt.UserController.badTenant',args: [ aUser.tenantname ], locale: aUser.locale)
		   log.warn (msgTenantDoesNotExist)
		   render([code: "ERROR", msg: msgTenantDoesNotExist ] as JSON);
		   return;
	   }

	   //Assign the tenantId and Create the user
	   aUser.tenantId = aTenant.id;
	   aUser.enabled = true;
	   User guestUser = aUser.save();
	   if (!guestUser) {
		   String message ="";
		   def locale = Locale.getDefault()
		   for (fieldErrors in aUser.errors) {
			   for (error in fieldErrors.allErrors) {
				   //TODO: Investigate Field Error Handling
				   message = messageSource.getMessage(error, aUser.locale)
			   }
		   }
		   render([code: "ERROR", msg: message ] as JSON);
		   return
	   }

	   //Assign guest role
	   //TODO: Embend tenant id in every query
	   //TODO: Handle error
	   def guestRole = Role.findByTenantIdAndAuthority(aTenant.id, 'ROLE_GUEST')

	   UserRole.create guestUser, guestRole

	   log.info("TEST");
	   
	   String msg = message(code: 'default.created.message',
		   args: [message(code: 'User.label', default: 'User', locale:guestUser.locale), guestUser.username],
	   locale: guestUser.locale)

	   render([code: "SUCCESS", msg: msg ] as JSON);

   }

	
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def search() {

		log.info("Searching with params:" + params + "and request: " + request.reader.getText());

		params.max = Math.min(params.max ? params.int('max') : 5, 100)

		def c = User.createCriteria();

		def users = c.list (QueryUtils.c_c(params));

		if(!users) {
			def error = ['error':"No Users Found"]
			render "${params.callback}(${error as JSON})"
		} else {
			log.warn(params)
			render "${params.callback}(${users as JSON})"
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
	/*
	 * If the company name already exists in the database. The auto sign up will be blocked. 
	 */
	def createCompanyAndUser() {

		String model = request.reader.getText();
		//TODO: Error Handling if unable to retrieve models
		log.info("Saving user with params:" + params + "and model: " + model);
		JSONObject jsonObject = new JSONObject(model);
		//TODO: Transient attribute does not marshall
		User userInstance = new User(jsonObject);
		userInstance.tenantname = jsonObject.get("tenantname");

		log.info("User being saved is: " + userInstance);

		//Verify if there is another user for the same company
		//The first user to sign up becomes the administrator

		log.info("Verifying if another user from the same company has already signed up");
		Tenant aCompany = Tenant.findByName(userInstance.tenantname);
		if (aCompany) {
			//Another user has already signed up for this company
			//TODO: Leverage the message
			log.info("Another user has already signed up for this company");
			String msgAlreadySigned = "Another user has already signed up for " + userInstance.tenantname;
			render([code: "ERROR", msg: msgAlreadySigned ] as JSON);
			return;
		}

		//Create the customer/tenant and start operating in the context of this tenant
		def theTenant = new Tenant(name: userInstance.tenantname).save(failOnError: true)

		theTenant.withThisTenant {
			//Create the user
			userInstance.userTenantId = theTenant.id;
			if (!userInstance.save()) {
				String message ="";
				def locale = Locale.getDefault()
				for (fieldErrors in userInstance.errors) {
					for (error in fieldErrors.allErrors) {
						message = messageSource.getMessage(error, locale)
					}
				}
				render([code: "ERROR", msg: message ] as JSON);
				return
			}

			//Assign Admin role
			def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

			if (!adminUser.authorities.contains(adminRole)) {
				UserRole.create adminUser, adminRole
			}
			
			//
			
			
		}

		String msg = messageSource.getMessage(code: 'default.created.message', args: [
			messageSource.getMessage(code: 'User.label', default: 'User', locale: aUser.locale),
			userInstance.username
		], locale: aUser.locale)
		render([code: "SUCCESS", msg: msg ] as JSON);

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

}

