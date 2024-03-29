package com.moovt.audit


import javax.management.InstanceOfQueryExp;

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import com.moovt.audit.CustomGrailsUser;


class DomainHelper {

	static setAuditAttributes(Object aDomainObject) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = null;
		if (auth) {
			if (auth.getPrincipal() instanceof CustomGrailsUser) {
				principal = auth.getPrincipal();
			}
		}

		if (!aDomainObject.tenantId) {
			assert principal!=null, "tenant Id can't be null unless a principal exists"
			aDomainObject.tenantId = principal.tenantId;
		}


		if (!aDomainObject.createdBy) {
			assert principal!=null, "createdBy can't be null unless a principal exists"
			aDomainObject.createdBy = principal.id;
		}

		if (!aDomainObject.lastUpdatedBy) {
			assert principal!=null, "lastUpdatedBy can't be null unless a principal exists"
			aDomainObject.lastUpdatedBy = principal.id;
		}

		if (!aDomainObject.dateCreated) {
			aDomainObject.dateCreated = new Date();
		}

		aDomainObject.lastUpdated = new Date();

	}

	static setTenantAuditAttributes(Object aDomainObject) {
		
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomGrailsUser principal = null;
		if (auth) {
			principal = auth.getPrincipal();
		}

		if (!aDomainObject.createdBy) {
			assert principal!=null, "createdBy can't be null unless a principal exists"
			aDomainObject.createdBy = principal.id;
		}

		if (!aDomainObject.lastUpdatedBy) {
			assert principal!=null, "lastUpdatedBy can't be null unless a principal exists"
			aDomainObject.lastUpdatedBy = principal.id;
		}

		if (!aDomainObject.dateCreated) {
			aDomainObject.dateCreated = new Date();
		}

		aDomainObject.lastUpdated = new Date();

	}

	
}
