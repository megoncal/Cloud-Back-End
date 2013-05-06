package com.moovt

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.Authentication
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.*

import com.moovt.CustomGrailsUser;
import com.moovt.Item;
import com.moovt.QueryUtils
import com.moovt.UUIDWrapper;

import java.util.UUID
import grails.plugins.springsecurity.Secured
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * This class contains the APIs to maintain an <code>Item</code> in the naSavassi app.
 *
 * @author egoncalves
 *
 */
class ItemController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	
	//List is a passthrough to the main view list.gsp
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}

	/*
	* This method retrieves up to 1000 items based on type and ordered by last update date
	*
	* Input
	*	type
	*
	* Output
	*	id
	*	title
	*	shortDescription
	*	longDescription
	*	type (Bar, Event, etc)
	*	creationDate
	*	lastUpdateDate : Sorted by this descending
	*	createdBy
	*	lastUpdatedBy
	*	latitude (Double)
	*	longitude (Double)
	*
	*	Sample call: http://localhost:8080/moovt/item/retrieveItemsByTypeAndRecency
	*
	*   Sample input: {"type":"com.moovt.Item.type.Bar"}
	*
	*/
   @Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
   def retrieveItemsByTypeAndRecency() {

	   String model = request.reader.getText();
	   log.info(this.actionName + " params are: " + params + " and model is : " + model);
	   JSONObject jsonObject = null;
	   try {
		   jsonObject = new JSONObject(model);
	   } catch (Exception e) {
		   render(new CallResult(CallResult.SYSTEM, CallResult.SYSTEM, e.message) as JSON);
		   return;
	   }
	   
	   //type is expected in the jsonObject
	   String type = jsonObject.type;
	   if (!type) {
		   render(new CallResult(CallResult.SYSTEM, CallResult.SYSTEM, message(code:'com.moovt.Item.retrieveItemsByTypeAndRecency.parameter.invalid')) as JSON);
	   }

	   def c = Item.createCriteria();

	   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	   CustomGrailsUser principal = auth.getPrincipal();
	   log.info("AUTH " + auth.getAuthorities());
	   log.info("AUTHENTICATED " + auth.isAuthenticated());
	   log.info("PRINCIPAL TENANT " + principal.tenantId);
	   
	   	   
	   def items = c.list {
		   and {
			   eq("tenantId", principal.tenantId)
			   eq("type", type)
		   }
		   maxResults(10)
		   order("lastUpdated", "desc")
	   }
	
	   	   
	   	
		if(!items) {
			def error = ['error':'No Items Found']
			render "${error as JSON}"
		} else {
			render "{\"items\":" + items.encodeAsJSON() + "}"
		}
   }
	
   
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def search() {

		log.info("Searching with params:" + params + "and request: " + request.reader.getText());
		
		params.max = Math.min(params.max ? params.int('max') : 5, 100)

		def c = Item.createCriteria();

		def items = c.list (QueryUtils.c_c(params));

		if(!items) {
			def error = ['error':"No Items Found"]
			render "${params.callback}(${error as JSON})"
		} else {
			log.warn(params)
			render "${params.callback}(${items as JSON})"
		}
	}

	//This method updates or insert an array of JSON objects
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def upsert() {
		
		String models = request.reader.getText();
		
		//TODO: Error Handling if unable to retrieve models
		
		log.info("Upserting with params:" + params + "and models: " + models);

		JSONArray jsonArray = new JSONArray(models);
		 

		ArrayList returnItems = new ArrayList();
		
		jsonArray.each () {
			JSONObject jsonObject = jsonArray[0];

			//Obtain the id and version from the json object
			def id = jsonObject.get("id");
			def version = jsonObject.get("version");

			log.info("JSON Object to update is " + jsonObject + " with id " + id + " and version " + version + ".");


			//Create a new item or retrieve the item from the database
			Item itemInstance = null;

			if ((id.equals(null)) || (id.equals(''))) {
				log.info("Creating a new item");
				itemInstance = new Item();
			} else {
				log.info("Updating and existing item");
				itemInstance = Item.get(id);
			}

			//Check for optimistic locking
			//TODO: Add a user to item and indicate which user updated the version
			if (!version.equals(null)) {
				if (itemInstance.version > version) {
					itemInstance.CRUDMessage="Another user udpate this record"''
				}
			}

			itemInstance.properties = jsonObject;


			//Validate and if valid save
			//This method returns the saved item to the client
			if (itemInstance.validate()) {
				itemInstance.save(flush: true);
				itemInstance.CRUDMessage = 'OK';
				returnItems.add(itemInstance);
			} else {
				//TODO: Create descriptive message
				itemInstance.CRUDMessage = 'NOK';
			}
			
		}			
		
		log.info("Rendering saved item as JSON Array " + "${params.callback}(${returnItems as JSON})");
		render "${params.callback}(${returnItems as JSON})";


	}

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
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


		//Create a new item or retrieve the item from the database
		Item itemInstance = Item.get(id);

		if (!itemInstance) {
			def error = ['error':"Item invalid " + itemInstance.toString()]
			render "${params.callback}(${error as JSON})"
		}

		//Check for optimistic locking
		//TODO: Add a user to item and indicate which user updated the version
		if (!version.equals(null)) {
			if (itemInstance.version > version) {
				def error = ['error':"Another user udpate "]
				render "${params.callback}(${error as JSON})"
			}
		}


		//If we got to this point, delete item
		try {
			itemInstance.delete(flush: true)
			render "${params.callback}(${itemInstance as JSON})"
		}
		catch (DataIntegrityViolationException e) {
			def error = ['error':"Item invalid " + itemInstance.toString()]
			render "${params.callback}(${error as JSON})"
		}


	}
	
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
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
		
//		def getImage={
//			
//					log.info("Retrieving Image");
//					File imageFile = new File('C:\\study\\imagestore\\ed486e33-3337-4ccd-8efa-860b56cc0e4f.jpg')
//					log.info(imageFile.text);
//					response.outputStream << imageFile.text;
//		}
		
}

