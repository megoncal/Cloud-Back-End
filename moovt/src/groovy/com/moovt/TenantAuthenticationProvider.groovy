package com.moovt

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
import org.codehaus.groovy.grails.commons.ApplicationHolder

import com.moovt.common.Tenant;
import com.moovt.common.User;

/**
 * This class encapsulates the custom <code>AuthenticationProvider</code> that provides the <code>authenticate</code> method.
 *
 * @author egoncalves
 *
 */
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
		//The local variable locale will be used to get messages from Message Source
		Locale locale = LocaleUtils.stringToLocale(authentication.locale);
		
		MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource')
		
		log.info("Authenticating user: " + username + " tenant: " + tenantName + " password: " + password);

		if (tenantName == "") {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.blank.company", args, "Company/Tenant is blank", locale));
		}
		
		Tenant tenant = Tenant.findByName (tenantName);
		
		if (!tenant) {
			String [] args = [ tenantName ];
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.company.notFound", args, "Company/Tenant not found", locale));
		}

		if (username == "") {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.blank.username", args, "Username is blank", locale));
		}

		if (password == "") {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.blank.password", args, "Password is blank", locale));
		}
		
		CustomGrailsUser userDetails
		def authorities
		// use withTransaction to avoid lazy loading exceptions
		User.withTransaction {
		
		User user = User.findByTenantIdAndUsername(tenant.id, username)

		if (!user) {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.invalid.usernamepassword", args, "User was not found", locale));
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
			  
			  
	    MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource');
		//The local variable locale will be used to get messages from Message Source
		Locale locale = LocaleUtils.stringToLocale(authentication.locale);

		def salt = saltSource.getSalt(userDetails)

		if (authentication.credentials == null) {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.invalid.usernamepassword", args, "User was not found", locale));
		}

		String presentedPassword = authentication.credentials
		
		log.info ("Checking password. Presented password is " + presentedPassword + " vs. Stored password " + userDetails.password + " with salt as " + salt);
		
		if (!passwordEncoder.isPasswordValid(userDetails.password, presentedPassword, salt)) {
			String [] args = {};
			throw new TenantUserPasswordAuthenticationException(messageSource.getMessage("com.moovt.invalid.usernamepassword", args, "User was not found", locale));
		}
	}

	boolean supports(Class<? extends Object> authenticationClass) {
      TenantAuthenticationToken.isAssignableFrom authenticationClass
	}
}
