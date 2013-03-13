package com.moovt.taxi

import com.moovt.CallResult;
import com.moovt.CustomGrailsUser
import com.moovt.common.Tenant;
import com.moovt.common.Role;
import com.moovt.common.User
import com.moovt.common.UserRole;
import grails.converters.JSON;
import java.util.Locale;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;
import grails.validation.ValidationException;
import org.springframework.security.core.Authentication
import org.springframework.web.servlet.support.RequestContextUtils;
import grails.plugins.springsecurity.Secured;
import org.springframework.security.core.context.SecurityContextHolder;


class DriverController {

	def index() { }

	

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveAllDrivers() {

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
			def drivers = Driver.list();

			if(!drivers) {
				def error = ['error':'No Drivers Found']
				render "${error as JSON}"
			} else {
				render "{\"drivers\":" + drivers.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}
	}

	@Secured(['ROLE_ADMIN','IS_AUTHENTICATED_FULLY'	])
	def retrieveDriverDetailById() {

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

			Driver driver = Driver.get(id);
			if(!driver) {
				def error = ['error':'No Driver Found']
				render "${error as JSON}"
			} else {
				render "{\"driver\":" + driver.encodeAsJSON() + "}"
			}
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}


	}

	@Secured(['ROLE_DRIVER','IS_AUTHENTICATED_FULLY'	])
	def retrieveLoggedDriverDetails() {

		String model = request.reader.getText();
		log.info(this.actionName + " params are: " + params + " and model is : " + model);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR,  e.message) as JSON);
			throw e;
		}


		//The Driver
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = auth.getPrincipal();

		try {
			
			Driver driver = Driver.get(principal.id);
			assert driver != null, "Because this method is secured, a principal always exist at this point of the code"

			render "{\"driver\":" + driver.encodeAsJSON() + "}"
		} catch (Throwable e) {
			status.setRollbackOnly();
			render(new CallResult(CallResult.SYSTEM,CallResult.ERROR,e.message) as JSON);
			throw e;
		}


	}

}

