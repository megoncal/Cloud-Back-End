package com.moovt.audit

import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.dao.SaltSource
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.servlet.support.RequestContextUtils

import com.moovt.audit.BadDataAuthenticationException;
import com.moovt.audit.CustomGrailsUser;
import com.moovt.audit.TenantAuthenticationToken;
import com.moovt.audit.TenantUserPasswordAuthenticationException;


/**
 * This class encapsulates the custom <code>AuthenticationProvider</code> that provides the <code>authenticate</code> method.
 *
 * @author egoncalves
 *
 */
class TenantAuthenticationProvider implements AuthenticationProvider {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	GrailsApplication grailsApplication
	PasswordEncoder passwordEncoder
	SaltSource saltSource
	UserDetailsChecker preAuthenticationChecks
	UserDetailsChecker postAuthenticationChecks
		
	
	Authentication authenticate(Authentication auth) throws AuthenticationException {
		TenantAuthenticationToken authentication = auth

		String password = authentication.credentials
		String username = authentication.name
		String tenantName = authentication.tenantName
		
		log.info("Authenticating user: " + username + " tenant: " + tenantName + " password: " + password);
		
		String tenantClassName = grailsApplication.config.grails.plugins.audit.userLookup.tenantDomainClassName
		def tenantDomainClass = grailsApplication.getDomainClass(tenantClassName)
		if (!tenantDomainClass) {
			throw new RuntimeException("The specified user domain class '$tenantClassName' is not a domain class")
		}

		Class<?> Tenant = tenantDomainClass.clazz
		
		def tenant = Tenant.findByName (tenantName);
		
		if ((!tenant) || (username == "") || (password == "")) {
			throw new BadDataAuthenticationException();
		}
		
		CustomGrailsUser userDetails
		def authorities
		// use withTransaction to avoid lazy loading exceptions
		
		String userClassName = grailsApplication.config.grails.plugins.audit.userLookup.userDomainClassName
		System.out.println ("HERE " + userClassName);
		def userDomainClass = grailsApplication.getDomainClass(userClassName)
		if (!userDomainClass) {
			throw new RuntimeException("The specified user domain class '$userDomainClass' is not a domain class")
		}

		Class<?> User = userDomainClass.clazz
		
		User.withTransaction {
		
		def user = User.findByTenantIdAndUsername(tenant.id, username)

		if (!user) {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException();
		}

			authorities = user.authorities.collect { new GrantedAuthorityImpl(it.authority) }
			authorities = authorities ?: GormUserDetailsService.NO_ROLES

			userDetails = new CustomGrailsUser(user.username, user.password,
				user.enabled, !user.accountExpired, !user.passwordExpired,
				!user.accountLocked, authorities, user.id, user.tenantId)
		}

		preAuthenticationChecks.check userDetails
		additionalAuthenticationChecks userDetails, authentication
		postAuthenticationChecks.check userDetails

		def result = new TenantAuthenticationToken(userDetails,
					authentication.credentials, tenantName, authorities)
		result.details = authentication.details
		return result;
	}

	protected void additionalAuthenticationChecks(GrailsUser userDetails,
			  TenantAuthenticationToken authentication) throws AuthenticationException {
					  
		def salt = saltSource.getSalt(userDetails)

		if (authentication.credentials == null) {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException();
		}

		String presentedPassword = authentication.credentials
		
		log.info ("Checking password. Presented password is " + presentedPassword + " vs. Stored password " + userDetails.password + " with salt as " + salt);
		
		if (!passwordEncoder.isPasswordValid(userDetails.password, presentedPassword, salt)) {
				throw new TenantUserPasswordAuthenticationException();
		}
	}

	boolean supports(Class<? extends Object> authenticationClass) {
	  TenantAuthenticationToken.isAssignableFrom authenticationClass
	}
}
