package com.moovt.common

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import java.security.Principal
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

import com.moovt.CallResult;
import com.moovt.CustomGrailsUser
import com.moovt.TenantAuthenticationToken;
import com.moovt.common.User;
import com.moovt.LocaleUtils;
import com.moovt.common.UserType;


/**
 * This class contains the APIs to manage the login operations in the application. The method <code>authenticateUser</code> should be used to
 * authenticate a user and obtain a JSESSIONID cookie for the subsequent calls.
 *
 * @author egoncalves
 *
 */
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
			redirect action: 'signinTab', params: params
		}
	}

	
	def test = {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		//CustomGrailsUser principal = auth.getPrincipal();
		
		render ("OK" + auth);
		
	}
	
	/**
	 * Show the login page.
	 */
//	def signinTab = {
//		
//		log.info("An attempt to access an unauthorized request was made");
//		response.sendError HttpServletResponse.SC_UNAUTHORIZED
//	}
	
   
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
		render view: 'signinTab', params: params,
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
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR, msg) as JSON)
		}
		else {
			flash.message = msg
			redirect action: 'signinTab', params: params
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		log.info("ajaxSuccess action called");
		render(new CallResult(CallResult.SYSTEM, CallResult.SUCCESS, "User signed in successfully") as JSON)
		//render([success: true, username: springSecurityService.authentication.name] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		log.error ("ajaxDenied");
		render([error: 'access denied'] as JSON)
	}

	
	/**
	 * This API authenticate a User using an authentication Service like Facebook or Self. Only Self is implemented at this time
	 *
	 * @param  url   <server-name>/login/authenticateUser
	 * @param  input-sample {"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
	 * @return output-sample {"type":"USER","code":"SUCCESS","message":"Login bem sucedido.","JSESSIONID":"6E7CE31F2EE0768A3AF7AF4310810FC1","userType":"PASSENGER"}
	 *
	 * userType can be DRIVER, PASSENGER, BOTH, or NO_TYPE
	 */

	def authenticateUser = { 
		//Make sure we have a session
		String sessionId = session.getId();
		String model = request.reader.getText();
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(model);
		} catch (Exception e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR, e.message) as JSON);
			return;
		}
		
		log.info("Authenticating user with params:" + params + "and model: " + model);
		User userInstance = new User(jsonObject);
		
		Locale authenticationLocale = LocaleUtils.stringToLocale(userInstance.locale ? userInstance.locale : "en_US");
		
		TenantAuthenticationToken token = new TenantAuthenticationToken(userInstance.username, userInstance.password,userInstance.tenantname, userInstance.locale);

		Authentication auth = null;
		try {
			auth = authenticationManager.authenticate(token);
		} catch (AuthenticationException e) {
			render(new CallResult(CallResult.USER, CallResult.ERROR, e.message) as JSON);
			throw e;
			return;
		} catch (Throwable e) {
			render(new CallResult(CallResult.SYSTEM, CallResult.ERROR, e.message) as JSON);
			throw e;
			return;
		}
		
		CustomGrailsUser principal = auth.getPrincipal();
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		
		log.info("User has been successfully authenticated " + principal.getAuthorities());
		String userType = UserType.NO_TYPE;
		for (grantedAuthority in principal.getAuthorities()) {
			println grantedAuthority;
			if (grantedAuthority.equals('ROLE_PASSENGER')) {
				if (userType == UserType.DRIVER) {
					userType = UserType.DRIVER_PASSENGER
				} else {
					userType = UserType.PASSENGER;
				}
			}
			if (grantedAuthority.equals('ROLE_DRIVER')) {
				if (userType == UserType.PASSENGER) {
					userType = UserType.DRIVER_PASSENGER
				} else {
					userType = UserType.DRIVER;
				}
			}
		}
		
		Authentication test = SecurityContextHolder.getContext().getAuthentication();
		
		log.info("TESTING .... " + test);
		
		
		//Change the default language to the user's language
		render([type: CallResult.USER, code: CallResult.SUCCESS, message: message(code: 'com.moovt.Login.success'), JSESSIONID: sessionId, userType: userType  ] as JSON);
		return
		
	}
		
}

