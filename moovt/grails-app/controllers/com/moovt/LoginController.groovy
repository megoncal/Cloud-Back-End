package com.moovt

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.core.AuthenticationException

class LoginController {

	def authenticationManager
	
	def index() {
		log.info ('index');
	}
	
    def signin() {
		log.info ('signin');
	 }
	
	def signout = {
			//TODO: Investigate / Test signout
		    log.info ('Signout requested');
			redirect url: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl;
	}
	
	def menu () {
		log.info ('Menu draw requested');
	}
	
	def dashboard () {
		log.info 'Dashboard draw requested'
	}

	def news () {
		log.info 'News draw requested'
	}

   def signup () {

	   log.info("Sign up page requested");

   }

	
	static navigation = [
		[group:'userOptions', action:'login', order: 0, isVisible: { session.user == null }],
		[action:'logout', order: 99, isVisible: { session.user != null }],
		[action:'profile', order: 1, isVisible: { session.user != null }]
	]
	
	/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

	/**
	 * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
	 */
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		}
		else {
			redirect action: 'auth', params: params
		}
	}

	/**
	 * Show the login page.
	 */
	def auth = {

		//log.info("An attempt to access an unauthorized request was made");
		//response.sendError HttpServletResponse.SC_UNAUTHORIZED
	}
	
   
	/**
	 * The redirect action for Ajax requests.
	 */
	def authAjax = {
		log.info("authAjax called");
		response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
		response.sendError HttpServletResponse.SC_UNAUTHORIZED
	}

	/**
	 * Show denied page.
	 */
	def denied = {
		if (springSecurityService.isLoggedIn() &&
				authenticationTrustResolver.isRememberMe(SecurityContextHolder.context?.authentication)) {
			// have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
			redirect action: 'full', params: params
		}
	}

	/**
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	 */
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render view: 'auth', params: params,
			model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
					postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}

	/**
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def authfail = {
		log.info("authfail called");
		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = g.message(code: "springSecurity.errors.login.expired")
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = g.message(code: "springSecurity.errors.login.passwordExpired")
			}
			else if (exception instanceof DisabledException) {
				msg = g.message(code: "springSecurity.errors.login.disabled")
			}
			else if (exception instanceof LockedException) {
				msg = g.message(code: "springSecurity.errors.login.locked")
			}
			else {
				msg = g.message(code: "springSecurity.errors.login.fail")
			}
		}

		if (springSecurityService.isAjax(request)) {
			render([code: "ERROR", msg: msg ] as JSON)
		}
		else {
			flash.message = msg
			redirect action: 'auth', params: params
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		log.info("ajaxSuccess action called");
		render(['code': "SUCCESS", 'msg': "User signed in successfully" ] as JSON)
		//render([success: true, username: springSecurityService.authentication.name] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		log.error ("ajaxDenied");
		render([error: 'access denied'] as JSON)
	}
	
	def authenticateUser = {
		String sessionId = session.getId();
		log.info("Session ID " + sessionId);
		String model = request.reader.getText();
		//TODO: Error Handling if unable to retrieve models
		log.info("Authenticating user with params:" + params + "and model: " + model);
		JSONObject jsonObject = new JSONObject(model);
		User userInstance = new User(jsonObject);

		//def theTenant = Customer.findByName(userInstance.tenantname)
		log.info("0");
		log.info("1" + userInstance.username + "-"+ userInstance.password);
		TenantAuthenticationToken token = new TenantAuthenticationToken(userInstance.username, userInstance.password,userInstance.tenantname);
		log.info("2");
		//getAuthenticationManager().authenticate(authentication)
		Authentication auth = null;
		try {
			auth = authenticationManager.authenticate(token);
		} catch (AuthenticationException e) {
			render([code: "ERROR", msg: e.message ] as JSON);
			return;
		}
		log.info("3 " + auth);
		SecurityContextHolder.getContext().setAuthentication(auth);
		Authentication tes = SecurityContextHolder.getContext().getAuthentication();
		log.info("Authentication " + tes.isAuthenticated() + tes.getPrincipal());
		log.info("Is logged in " +springSecurityService.isLoggedIn())
		log.info("User has been successfully authenticated")
		//log.info(springSecurityService.getAuthenticationTrustResolver());
		
		
		//Change the default language to the user's language
		Locale.setDefault(auth.getPrincipal().locale)
		
		render([code: "SUCCESS", msg: message(code: 'com.moovt.Login.success'), JSESSIONID: sessionId ] as JSON);
		return
		
		//springSecurityService.reauthenticate(userInstance.username, userInstance.password )
		//springSecurityService.a
		
		//Lookup user 
		//List results = User.withCriteria {
		//	and {
		//	  eq('userTenantId', userInstance.userTenantId)	
		//	  eq('username', userInstance.username)
		//	  eq('password', userInstance.password)
	//		}
		//  }

		//switch (results.size()) {
		//int tempvar = 0;
//		switch (tempvar) {
//		case 0:
//			render([code: "ERROR", msg: "Sorry. Unable to find user matching this company, user and password" ] as JSON);
//			break;
//		case 1:
//			log.info("User has been successfully validated")
//			log.info(springSecurityService.getAuthenticationTrustResolver());
//			
//			render([code: "SUCCESS", msg: "Login successfull" ] as JSON);
//			break;
//		default:	 
//			render([code: "ERROR", msg: "Login was not successfull. Please contact your Administrator." ] as JSON);
//			break;
//		}
		//return;

		}
		
}

