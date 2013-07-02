package com.moovt.common

import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder

import com.moovt.MultiTenantAudit;
import com.moovt.DomainHelper;

/**
 * This class represents the many-to-many association between <code>User</code> and its authorities (i.e. Roles).
 * 
 * @author egoncalves
 *
 */
//@MultiTenantAudit
class UserRole implements Serializable {

	Long tenantId;
	Long createdBy;
	Long lastUpdatedBy;
	Date lastUpdated;
	Date dateCreated;
	
	User user
	Role role

	static constraints = {
		tenantId nullable: true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		lastUpdated nullable: true
		dateCreated nullable: true
		
		user nullable: false
		role nullable: false

	}

	def beforeInsert () {
		DomainHelper.setAuditAttributes(this);
	}
	
	def beforeUpdate () {
		DomainHelper.setAuditAttributes(this);
	}		
	boolean equals(other) {
		if (!(other instanceof UserRole)) {
			return false
		}

		other.user?.id == user?.id &&
			other.role?.id == role?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static UserRole get(long userId, long roleId) {
		find 'from UserRole where user.id=:userId and role.id=:roleId',
			[userId: userId, roleId: roleId]
	}

	static UserRole create(Long tenantId, User user, Role role, Long createdBy, Long lastUpdatedBy, boolean flush = false) {
		new UserRole(tenantId: tenantId, user: user, role: role, createdBy: createdBy, lastUpdatedBy: lastUpdatedBy).save(flush: flush, insert: true)
	}
	
	static UserRole create(Long tenantId, User user, Role role, boolean flush = false) {
		new UserRole(tenantId: tenantId, user: user, role: role).save(flush: flush, insert: true)
	}

	static boolean remove(User user, Role role, boolean flush = false) {
		UserRole instance = UserRole.findByUserAndRole(user, role)
		if (!instance) {
			return false
		}

		instance.delete(flush: flush)
		true
	}

	static void removeAll(User user) {
		executeUpdate 'DELETE FROM UserRole WHERE user=:user', [user: user]
	}

	static void removeAll(Role role) {
		executeUpdate 'DELETE FROM UserRole WHERE role=:role', [role: role]
	}

	static mapping = {
		id composite: ['role', 'user']
		version false
	}
	
	String toString() {
		this.dump();
	}
}
