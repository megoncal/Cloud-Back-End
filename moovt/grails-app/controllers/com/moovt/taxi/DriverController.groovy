package com.moovt.taxi

import com.moovt.CallResult;
import com.moovt.common.Tenant;
import com.moovt.common.Role;
import com.moovt.common.UserRole;
import grails.converters.JSON;
import java.util.Locale;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;
import grails.validation.ValidationException;
import org.springframework.web.servlet.support.RequestContextUtils;



class DriverController {

	def index() { }

	/*
	 * createOrUpdateDriver
	 * 
	 * 
	 *  localhost:8080/moovt/driver/createOrUpdateDriverUser
	 * 
	 * 	{"tenantname":"WorldTaxi","firstName":"John","lastName":"Goodarm","username":"jgoodarm","password":"Welcome!1","phone":"773-329-1784","email":"jgoodarm@worldtaxi.com","carType":"Sedan","servedMetro":"Chicago-Naperville-Joliet, IL","locale":"en-US"}
	 * 
	 *  {"tenantname":"WorldTaxi","id":11,"version":"3","firstName":"John","lastName":"Goodarm","username":"jgoodarm","password":"Welcome!1","phone":"773-329-1784","email":"jgoodarm@worldtaxi.com","carType":"Sedan","servedMetro":"Chicago-Naperville-Joliet, IL","locale":"en-US"}
	 */


	def createOrUpdateDriverUser() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject driverJsonObject = null;
		try {
			driverJsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		//Obtain the id, version and tenantname from the json object
		Long id = driverJsonObject.optLong("id",0);
		Long version = driverJsonObject.optLong("version",0);
		String tenantname = driverJsonObject.optString("tenantname","");
		

		//For methods without the need of authentication, verify if the tenant exists
		Tenant aTenant = Tenant.findByName(tenantname);
		if (!aTenant) {
			String msgTenantDoesNotExist = message(code: 'com.moovt.UserController.badTenant',args: [ tenantname ])
			log.warn (msgTenantDoesNotExist)
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR, msgTenantDoesNotExist) as JSON);
			return;
		}

		//Start a transaction to insert or update based on the existence of an id
		Driver.withTransaction { status ->
			Driver driver = null;
			try {
				if (id == 0) {
					log.info("Creating a new driver");
					driver = new Driver(driverJsonObject);
					driver.enabled = true;
					driver.tenantId = aTenant.id;
					driver.createdBy = 1;
					driver.lastUpdatedBy = 1;
					driver.save(flush:true, failOnError:true);

					//Update the createdBy and lastUpdatedBy to self
					driver.createdBy = driver.id;
					driver.lastUpdatedBy = driver.id;
					driver.save(flush:true, failOnError:true)

					// Assign the driver role
					def driverRole = Role.findByTenantIdAndAuthority(aTenant.id, 'ROLE_DRIVER')
					UserRole.create (driver.tenantId, driver, driverRole, driver.createdBy, driver.lastUpdatedBy);

					def rideMgrRole = Role.findByTenantIdAndAuthority(aTenant.id, 'ROLE_RIDE_MGR')
					UserRole.create (driver.tenantId, driver, rideMgrRole, driver.createdBy, driver.lastUpdatedBy);

					String msg = message(code: 'default.created.message',
					args: [message(code: 'Driver.label', default: 'Driver'), driver.username])
					render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);
					return;
				} else { //update
					log.info("Updating an existing driver");
					driver = Driver.get(id);
					if (!driver) {
						render (new CallResult(CallResult.SYSTEM, CallResult.ERROR, message (code: 'com.moovt.driver.not.found',args:[id])) as JSON);
						return;
					}
					//Before updating - check for concurrency
					if (driver.version > version) {
						render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')) as JSON);
						return;
					}
					driver.properties = driverJsonObject;
					driver.lastUpdatedBy = driver.id; //In the taxi app, the driver updates itself
					driver.save(flush:true, failOnError:true);
					String msg = message(code: 'default.updated.message',
					args: [message(code: 'Driver.label', default: 'Driver'), driver.username])
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

