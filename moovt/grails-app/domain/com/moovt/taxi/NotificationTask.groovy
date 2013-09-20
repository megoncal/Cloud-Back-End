package com.moovt.taxi; 
           
import java.util.Date; 
 
import com.moovt.audit.DomainHelper;
import com.moovt.audit.MultiTenantAudit;

     
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
@MultiTenantAudit
class NotificationTask {


    TaskType taskType;  
	TaskStatus taskStatus;
	String notificationFrom;
	String notificationTo;
	String subject;
	String message;
	Date processDate;
	String failedReason;
	
	static constraints = {
		processDate nullable: true
		failedReason nullable: true
	}
	
	static mapping = {
		message type: "text"
	}
	
	
}
