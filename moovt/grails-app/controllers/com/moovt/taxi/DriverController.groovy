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
		def carTypes = dynEnumService.getDynamicEnum(CarType, RequestContextUtils.getLocale(request));
		render (carTypes as JSON);
		
	}
	
	def getActiveStatusEnum() {
		def carTypes = dynEnumService.getDynamicEnum(ActiveStatus, RequestContextUtils.getLocale(request));
		render (carTypes as JSON);
		
	}
	
	def getRadiusServedEnum() {
		def carTypes = dynEnumService.getDynamicEnum(RadiusServed, RequestContextUtils.getLocale(request));
		render (carTypes as JSON);
		
	}
}

