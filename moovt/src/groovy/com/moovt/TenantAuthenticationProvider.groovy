package com.moovt

import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.dao.SaltSource
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UsernameNotFoundException

class TenantAuthenticationProvider implements AuthenticationProvider {

	protected final Logger log = LoggerFactory.getLogger(getClass());

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

		if (tenantName == "") {
			log.warn "Company is blank. Please enter your company name or sign up for your company."
			throw new TenantUserPasswordAuthenticationException("Company is blank. Please enter your company name or sign up for your company.")
		}
		
		Tenant tenant = Tenant.findByName (tenantName);
		
		if (!tenant) {
			// TODO customize 'springSecurity.errors.login.fail' i18n message in app's messages.properties with org name
			log.warn "$tenantName not found. Your can signup for this company."
			throw new TenantUserPasswordAuthenticationException("Company not found: ", tenantName)
		}

		if (username == "") {
			log.warn "Username is blank. Please enter a user name to sign in."
			throw new TenantUserPasswordAuthenticationException("Username is blank. Please enter a user name to sign in.")
		}

		
		CustomGrailsUser userDetails
		def authorities
		// use withTransaction to avoid lazy loading exceptions
		User.withTransaction {
		
		User user = User.findByTenantIdAndUsername(tenant.id, username)

		if (!user) {
			// TODO customize 'springSecurity.errors.login.fail' i18n message in app's messages.properties with org name
			log.warn "$username not found in company $tenantName"
			throw new UsernameNotFoundException("$username not found in company $tenantName")
		}

			authorities = user.authorities.collect { new GrantedAuthorityImpl(it.authority) }
			authorities = authorities ?: GormUserDetailsService.NO_ROLES

			userDetails = new CustomGrailsUser(user.username, user.password,
				user.enabled, !user.accountExpired, !user.passwordExpired,
				!user.accountLocked, authorities, user.id, user.tenantId, user.locale)
		}

		preAuthenticationChecks.check userDetails
		additionalAuthenticationChecks userDetails, authentication
		postAuthenticationChecks.check userDetails

		def result = new TenantAuthenticationToken(userDetails,
           	     authentication.credentials, tenantName, authorities)
		result.details = authentication.details
		result
	}

	protected void additionalAuthenticationChecks(GrailsUser userDetails,
			  TenantAuthenticationToken authentication) throws AuthenticationException {

		def salt = saltSource.getSalt(userDetails)

		if (authentication.credentials == null) {
			log.warn 'Authentication failed because no password was provided. Please enter a password.'
			throw new BadCredentialsException('Authentication failed because no password was provided. Please enter a password.', userDetails)
		}

		String presentedPassword = authentication.credentials
		
		log.info ("Checking password. Presented password is " + presentedPassword + " vs. Stored password " + userDetails.password + " with salt as " + salt);
		
		if (!passwordEncoder.isPasswordValid(userDetails.password, presentedPassword, salt)) {
			log.warn 'Authentication failed because password does not match the stored value.'

			throw new BadCredentialsException('Authentication failed because password does not match the stored value.', userDetails)
		}
	}

	boolean supports(Class<? extends Object> authenticationClass) {
      TenantAuthenticationToken.isAssignableFrom authenticationClass
	}
}
