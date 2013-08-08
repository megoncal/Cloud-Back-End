package com.moovt.common;

import java.util.Date;

import com.moovt.MultiTenantAudit;
import com.moovt.common.Location;
import com.moovt.DomainHelper;

enum TaskStatus {
	INQUEUE, PROCESSED, FAILED
 }

enum TaskType {
	EMAIL, PUSHNOTIF
 }
/**
 * This class represents a <code>Ride</code>. 
 *
 * @author egoncalves
 *
 */
//@MultiTenantAudit
class NotificationTask {

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;

    TaskType taskType;  
	TaskStatus taskStatus;
	String notificationFrom;
	String notificationTo;
	String subject;
	String message;
	Date processDate;
	String failedReason;
	
	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true

		processDate nullable: true
		failedReason nullable: true
	}
	
	static mapping = {
		message type: "text"
	}
	
	def beforeInsert () {
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setAuditAttributes(this);
	}
	
}
