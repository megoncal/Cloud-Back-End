package com.moovt.taxi

import grails.converters.JSON

import org.springframework.web.servlet.support.RequestContextUtils

/**
 * The DriverController class contains methods associated with the Driver. Currently, this class is particularly useful
 * to retrieve the Enumerations required for creating or updating a Driver. 
 */
class DriverController {

	def messageSource;
	def dynEnumService;
	
	def getCarTypeEnum() {
		def carTypes = dynEnumService.getDynamicEnums(CarType);
		render (carTypes as JSON);
		
	}
	
	def getActiveStatusEnum() {
		def activeStatus = dynEnumService.getDynamicEnums(ActiveStatus);
		render (activeStatus as JSON);
		
	}
	
}

