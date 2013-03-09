package com.moovt.taxi

import com.moovt.CallResult;
import com.moovt.common.Address
import com.moovt.common.Tenant;
import com.moovt.common.Role;
import com.moovt.common.UserRole;
import grails.converters.JSON;
import java.util.Locale;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;
import grails.validation.ValidationException;
import org.springframework.web.servlet.support.RequestContextUtils;



class PassengerController {

	def index() { }

	/*
	 * createOrUpdatePassenger
	 * 
	 * 
	 *  localhost:8080/moovt/passenger/createOrUpdatePassengerUser
	 * 
	 * 	{"tenantname":"WorldTaxi","firstName":"John","lastName":"Goodrider","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"pt_BR","Address":{"street":"123 Main St","city":"Wheaton","state":"IL","zip":"60107","type":"Home"}}
	 *  
	 *  {"tenantname":"WorldTaxi","id":"6","version":"1","firstName":"John","lastName":"Goodrider","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"pt_BR","Address":{"street":"123 Main St","city":"Wheaton","state":"IL","zip":"60107","type":"Home"}}
	 */

	def createOrUpdatePassengerUser() {
		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject passengerJsonObject = null;
		try {
			passengerJsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}

		//Obtain the id, version and tenantname from the json object
		Long id = passengerJsonObject.optLong("id",0);
		Long version = passengerJsonObject.optLong("version",0);
		String tenantname = passengerJsonObject.optString("tenantname","");
		
		//For methods without the need of authentication, verify if the tenant exists
		Tenant aTenant = Tenant.findByName(tenantname);
		if (!aTenant) {
			String msgTenantDoesNotExist = message(code: 'com.moovt.UserController.badTenant',args: [ tenantname ])
			log.warn (msgTenantDoesNotExist)
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR, msgTenantDoesNotExist) as JSON);
			return;
		}

		
		
		//Start a transaction to insert or update based on the existence of an id
		Passenger.withTransaction { status ->
			Passenger passenger = null;
			try {
				if (id == 0) {
					log.info("Creating a new passenger");
					passenger = new Passenger(passengerJsonObject);
					//TODO: Further investigate binding of nested objects
					JSONObject addressJsonObject = passengerJsonObject.opt("Address");
					log.info(addressJsonObject);
					Address address = new Address(addressJsonObject);
					address.tenantId = aTenant.id;
					passenger.address = address;
					passenger.tenantId = aTenant.id;
					passenger.createdBy = 1;
					passenger.lastUpdatedBy = 1;
					passenger.save(flush:true, failOnError:true);

					//Update the createdBy and lastUpdatedBy to self
					passenger.createdBy = passenger.id;
					passenger.lastUpdatedBy = passenger.id;
					address.createdBy = passenger.id;
					address.lastUpdatedBy = passenger.id;
					passenger.save(flush:true, failOnError:true)

					// Assign the passenger role
					def passengerRole = Role.findByTenantIdAndAuthority(aTenant.id, 'ROLE_PASSENGER')
					UserRole.create (passenger.tenantId, passenger, passengerRole, passenger.createdBy, passenger.lastUpdatedBy);

					def rideMgrRole = Role.findByTenantIdAndAuthority(aTenant.id, 'ROLE_RIDE_MGR')
					UserRole.create (passenger.tenantId, passenger, rideMgrRole, passenger.createdBy, passenger.lastUpdatedBy);

					String msg = message(code: 'default.created.message',
					args: [message(code: 'Passenger.label', default: 'Passenger'), passenger.username])
					render(new CallResult(CallResult.USER, CallResult.SUCCESS, msg) as JSON);
					return;
				} else { //update
					log.info("Updating an existing passenger");
					passenger = Passenger.get(id);
					//Before proceeding - check that this is a legitimate id
					if (!passenger) {
						render (new CallResult(CallResult.SYSTEM, CallResult.ERROR, message (code: 'com.moovt.passenger.not.found',args:[id])) as JSON);
						return;
					}
					//Before updating - check for concurrency
					if (passenger.version > version) {
						render (new CallResult(CallResult.USER, CallResult.ERROR, message (code: 'com.moovt.concurrent.update')) as JSON);
						return;
					}
					
					//If address provided, update the address
					JSONObject addressJsonObject = passengerJsonObject.opt("Address");
					if (addressJsonObject) {
						passenger.address.properties = addressJsonObject;
						passenger.address.lastUpdatedBy = passenger.id;
					}
					passenger.properties = passengerJsonObject;
					passenger.lastUpdatedBy = passenger.id; //In the taxi app, the passenger updates itself
					passenger.save(flush:true, failOnError:true);
					String msg = message(code: 'default.updated.message',
					args: [message(code: 'Passenger.label', default: 'Passenger'), passenger.username])
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

