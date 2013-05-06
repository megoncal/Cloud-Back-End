package com.moovt

import com.moovt.common.Location;
import com.moovt.common.LocationType;
import com.moovt.common.User
import com.moovt.taxi.Driver;
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import grails.plugin.mail.MailService
import groovyx.net.http.*
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.context.MessageSource
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.web.servlet.support.RequestContextUtils

class NotificationService {

	SessionFactory sessionFactory
	MailService mailService
	MessageSource messageSource;

	static transactional = false

	public void notifyDriversOfRideAvailable (Integer driverId, Double distance, Ride ride) {
		
		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
		
		Driver driver = Driver.get(driverId);
		User driverUser = User.get(driverId);
		Passenger passenger = ride.passenger;
		User passengerUser = User.get(ride.passenger.id);
		
		distance = Math.round(distance);
		
		String emailSubject = messageSource.getMessage ('com.moovt.driver.ride.notification.subject',
			[ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
			 ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
			 ride.pickupDateTime] as Object[],
			 locale);
		
		String emailBody = messageSource.getMessage ('com.moovt.driver.ride.notification.body',
													  [driverUser.firstName + " " + driverUser.lastName, 
			                                           ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
													   ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
													   ride.pickupDateTime,
													   distance,
													   driver.servedLocation.locationName + " - " + driver.servedLocation.politicalName] as Object[],
												       locale);
		
		mailService.sendMail {
			to driverUser.email
			from "dispatch@moovt.com"
			subject emailSubject
			body emailBody
		}

	}

	public void notifyDriverOfRideAssignment (Ride ride) {
		
		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
		
		User driverUser = User.get(ride.driver.id);
		User passengerUser = User.get(ride.passenger.id);
		
		String emailSubject = messageSource.getMessage ('com.moovt.driver.ride.assignment.subject',
			[ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
			 ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
			 ride.pickupDateTime] as Object[],
			 locale);
		
		String emailBody = messageSource.getMessage ('com.moovt.driver.ride.assignment.body',
													  [driverUser.firstName + " " + driverUser.lastName,
													   ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
													   ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
													   ride.pickupDateTime,
													   passengerUser.firstName + " " + passengerUser.lastName,
													   passengerUser.phone,
													   passengerUser.email] as Object[],
													   locale);
		
		mailService.sendMail {
			to driverUser.email
			from "dispatch@moovt.com"
			subject emailSubject
			body emailBody
		}

	}
	
	
	public void notifyPassengerOfRideAssignment (Ride ride) {
		
		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
		
		User driverUser = User.get(ride.driver.id);
		User passengerUser = User.get(ride.passenger.id);
	
		
		String emailSubject = messageSource.getMessage ('com.moovt.passenger.ride.assignment.subject',
			[ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
			 ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
			 ride.pickupDateTime] as Object[],
			 locale);
		
		String emailBody = messageSource.getMessage ('com.moovt.passenger.ride.assignment.body',
													  [passengerUser.firstName + " " + passengerUser.lastName,
													   ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
													   ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
													   ride.pickupDateTime,
													   driverUser.firstName + " " + passengerUser.lastName,
													   driverUser.phone,
													   driverUser.email] as Object[],
													   locale);
		
		mailService.sendMail {
			to passengerUser.email
			from "dispatch@moovt.com"
			subject emailSubject
			body emailBody
		}

	}
	
	
	
	

	}

