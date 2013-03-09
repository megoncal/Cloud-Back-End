package com.moovt.common

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.*

import com.moovt.Asset;
import com.moovt.QueryUtils
import com.moovt.UUIDWrapper;

import java.util.UUID
import grails.plugins.springsecurity.Secured

class AssetController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	//List is a passthrough to the main view list.gsp
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def main() {
	}

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def search() {

		log.info("Searching with params:" + params + "and request: " + request.reader.getText());
		
		params.max = Math.min(params.max ? params.int('max') : 5, 100)

		def c = Asset.createCriteria();

		def assets = c.list (QueryUtils.c_c(params));

		if(!assets) {
			def error = ['error':"No Assets Found"]
			render "${params.callback}(${error as JSON})"
		} else {
			log.warn(params)
			render "${params.callback}(${assets as JSON})"
		}
	}

	//This method updates or insert an array of JSON objects
	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def upsert() {
		
		String models = request.reader.getText();
		
		//TODO: Error Handling if unable to retrieve models
		
		log.info("Upserting with params:" + params + "and models: " + models);

		JSONArray jsonArray = new JSONArray(models);
		 

		ArrayList returnAssets = new ArrayList();
		
		jsonArray.each () {
			JSONObject jsonObject = jsonArray[0];

			//Obtain the id and version from the json object
			def id = jsonObject.get("id");
			def version = jsonObject.get("version");

			log.info("JSON Object to update is " + jsonObject + " with id " + id + " and version " + version + ".");


			//Create a new asset or retrieve the asset from the database
			Asset assetInstance = null;

			if ((id.equals(null)) || (id.equals(''))) {
				log.info("Creating a new asset");
				assetInstance = new Asset();
			} else {
				log.info("Updating and existing asset");
				assetInstance = Asset.get(id);
			}

			//Check for optimistic locking
			//TODO: Add a user to asset and indicate which user updated the version
			if (!version.equals(null)) {
				if (assetInstance.version > version) {
					assetInstance.CRUDMessage="Another user udpate this record"''
				}
			}

			assetInstance.properties = jsonObject;


			//Validate and if valid save
			//This method returns the saved asset to the client
			if (assetInstance.validate()) {
				assetInstance.save(flush: true);
				assetInstance.CRUDMessage = 'OK';
				returnAssets.add(assetInstance);
			} else {
				//TODO: Create descriptive message
				assetInstance.CRUDMessage = 'NOK';
			}
			
		}			
		
		log.info("Rendering saved asset as JSON Array " + "${params.callback}(${returnAssets as JSON})");
		render "${params.callback}(${returnAssets as JSON})";


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


		//Create a new asset or retrieve the asset from the database
		Asset assetInstance = Asset.get(id);

		if (!assetInstance) {
			def error = ['error':"Asset invalid " + assetInstance.toString()]
			render "${params.callback}(${error as JSON})"
		}

		//Check for optimistic locking
		//TODO: Add a user to asset and indicate which user updated the version
		if (!version.equals(null)) {
			if (assetInstance.version > version) {
				def error = ['error':"Another user udpate "]
				render "${params.callback}(${error as JSON})"
			}
		}


		//If we got to this point, delete asset
		try {
			assetInstance.delete(flush: true)
			render "${params.callback}(${assetInstance as JSON})"
		}
		catch (DataIntegrityViolationException e) {
			def error = ['error':"Asset invalid " + assetInstance.toString()]
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

