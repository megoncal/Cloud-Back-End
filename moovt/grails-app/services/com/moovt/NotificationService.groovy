package com.moovt

import com.moovt.common.Location;
import com.moovt.common.LocationType;
import com.moovt.common.User
import com.moovt.taxi.Driver;
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import com.moovt.common.NotificationTask
import com.moovt.common.TaskType
import com.moovt.common.TaskStatus
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

import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsNotification
import com.notnoop.apns.ApnsService
import com.notnoop.apns.PayloadBuilder


class NotificationService {

	SessionFactory sessionFactory
	MailService mailService
	ApnsService apnsService
	MessageSource messageSource;

	static transactional = false

	public void notifyDriversOfRideAvailable (Integer driverId, Double distance, Ride ride) {

		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());

		//TODO: Rationalize these SQL statements
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

		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);
		//to driverUser.email
		//		mailService.sendMail {
		//			to "egoncalves@moovt.com"
		//			from "dispatch@moovt.com"
		//			subject emailSubject
		//			body emailBody
		//		}

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

		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);
	}

	public void notifyDriverOfRideDeleted (Ride ride) {

		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());

		User driverUser = User.get(ride.driver.id);
		User passengerUser = User.get(ride.passenger.id);

		String emailSubject = messageSource.getMessage ('com.moovt.driver.ride.deleted.subject',
				[ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
					ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName] as Object[],
				locale);

		String emailBody = messageSource.getMessage ('com.moovt.driver.ride.deleted.body',
				[driverUser.firstName + " " + driverUser.lastName,
					ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
					ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
					ride.pickupDateTime,
					passengerUser.firstName + " " + passengerUser.lastName,
					passengerUser.phone,
					passengerUser.email] as Object[],
				locale);

		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);

		if (driverUser.apnsToken) {
			def pushNotification = new NotificationTask(
					taskType: TaskType.PUSHNOTIF,
					taskStatus: TaskStatus.INQUEUE,
					notificationFrom: "N/A",
					notificationTo: driverUser.apnsToken,
					subject: emailSubject,
					message: emailBody).save(failOnError: true);
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

		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);

	}


	public void notifyDriverOfRideClosed (Ride ride) {

		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());

		User driverUser = User.get(ride.driver.id);
		User passengerUser = User.get(ride.passenger.id);

		String emailSubject = messageSource.getMessage ('com.moovt.driver.ride.closed.subject',
				[ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
					ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
					ride.pickupDateTime] as Object[],
				locale);

		String emailBody = messageSource.getMessage ('com.moovt.driver.ride.closed.body',
				[driverUser.firstName + " " + driverUser.lastName,
					ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
					ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
					ride.pickupDateTime,
					passengerUser.firstName + " " + passengerUser.lastName,
					passengerUser.phone,
					passengerUser.email] as Object[],
				locale);

		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);

	}

	public void notifyPassengerOfRideClosed (Ride ride) {

		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());

		User driverUser = User.get(ride.driver.id);
		User passengerUser = User.get(ride.passenger.id);


		String emailSubject = messageSource.getMessage ('com.moovt.passenger.ride.closed.subject',
				[ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
					ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
					ride.pickupDateTime] as Object[],
				locale);

		String emailBody = messageSource.getMessage ('com.moovt.passenger.ride.closed.body',
				[passengerUser.firstName + " " + passengerUser.lastName,
					ride.pickUpLocation.locationName + " - " + ride.pickUpLocation.politicalName,
					ride.dropOffLocation.locationName + " - " + ride.dropOffLocation.politicalName,
					ride.pickupDateTime,
					driverUser.firstName + " " + passengerUser.lastName,
					driverUser.phone,
					driverUser.email] as Object[],
				locale);

		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);

	}

	public void notifyPasswordChanged (User user, String newPassword) {

		def webUtils = WebUtils.retrieveGrailsWebRequest();
		Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());


		String emailSubject = messageSource.getMessage ('com.moovt.user.passwordReset.subject', null,locale);

		String emailBody = messageSource.getMessage ('com.moovt.user.passwordReset.subject',
				[user.firstName + " " + user.lastName,
					newPassword] as Object[],
				locale);


		//Insert into the notification task table
		def emailNotification = new NotificationTask(
				taskType: TaskType.EMAIL,
				taskStatus: TaskStatus.INQUEUE,
				notificationFrom: "dispatch@moovt.com",
				notificationTo: "egoncalves@moovt.com",
				subject: emailSubject,
				message: emailBody).save(failOnError: true);

	}

	//Process Notification is called in a separate Thread
	int processNotification() {
		log.info("Processing notifications");

		def c = NotificationTask.createCriteria();

		def notificationTasks = c.list {
			and {
				eq("taskStatus", TaskStatus.INQUEUE)
			}
			order("lastUpdated", "desc")
		}
		int numberOfTasks = notificationTasks.size();
		log.info("Found " + numberOfTasks + " notifications to process");
		for (notificationTask in notificationTasks) {

			log.info("Processing message " + notificationTask.subject);
			log.info("Mechanism is " + notificationTask.taskType);
			try {
				if (notificationTask.taskType.equals(TaskType.EMAIL)) {
					mailService.sendMail {
						to notificationTask.notificationTo
						from notificationTask.notificationFrom
						subject notificationTask.subject
						body notificationTask.message
					}
				}

				if (notificationTask.taskType.equals(TaskType.PUSHNOTIF)) {
					try {
						PayloadBuilder payloadBuilder = APNS.newPayload();
						payloadBuilder.alertBody(notificationTask.subject);
						String payload = payloadBuilder.build();

						String token = notificationTask.notificationTo;
						log.info("Now pushing");

						apnsService.push(token, payload);

						log.info("Pushed");
					} catch (Exception e) {
						e.printStackTrace();
						log.error("Could not connect to APNs to send the notification - " + e.message)
					}

				}

				NotificationTask.withTransaction {
					notificationTask.taskStatus = TaskStatus.PROCESSED;
					notificationTask.processDate = new Date();
					notificationTask.save (failOnError:true);
				}
			} catch (Throwable e) {
				log.info("An exception occurred " + e.message);
				e.printStackTrace();
			}
		}

		return numberOfTasks;

	}

}

