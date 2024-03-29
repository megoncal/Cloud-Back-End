package com.moovt.common

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import grails.converters.JSON

import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import com.moovt.HandlerService
import com.moovt.audit.BadDataAuthenticationException;
import com.moovt.audit.CustomGrailsUser;
import com.moovt.audit.Tenant;
import com.moovt.audit.TenantAuthenticationToken;
import com.moovt.audit.TenantUserPasswordAuthenticationException;
import com.moovt.audit.User;
import com.moovt.audit.UserType;


/**
 * This class contains the APIs to manage the login operations in the application. The method <code>authenticateUser</code> should be used to
 * authenticate a user and obtain a JSESSIONID cookie for the subsequent calls.
 *
 * @author egoncalves
 *
 */
class LoginController {

	def authenticationManager
	HandlerService handlerService; //inject the utilService bean

	def index() {
		log.info ('index');
	}

	def signin() {
		log.info ('signin');
	}

	def signout = {
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
			handlerService.handleSystemError(msg);
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
		handlerService.handleSuccess('com.moovt.Login.success', null);

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


			log.info("Authenticating user with params:" + params + "and model: " + model);
			User userInstance = new User(jsonObject);

			TenantAuthenticationToken token = new TenantAuthenticationToken(userInstance.username, userInstance.password,userInstance.tenantname);

			if (token.tenantName == "") {
				handlerService.handleUserError('com.moovt.blank.tenantname');
				return;
			}
			
			//Verify if this token is good
			Tenant tenant = Tenant.findByName(token.tenantName);
			if (!tenant) {
				handlerService.handleUserError('com.moovt.company.notFound', [token.tenantName] as Object[])
				return;
			}


			if (token.name == "") {
				handlerService.handleUserError('com.moovt.blank.username');
				return;
			}

			if (token.credentials == "") {
				handlerService.handleUserError('com.moovt.blank.password');
				return;
			}


			Authentication auth = null;
			auth = authenticationManager.authenticate(token);

			CustomGrailsUser principal = auth.getPrincipal();
			SecurityContextHolder.getContext().setAuthentication(auth);


			log.info("User has been successfully authenticated " + principal.getAuthorities());
			String userType = UserType.NO_TYPE;
			for (grantedAuthority in principal.getAuthorities()) {
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

			//If an apnsToken exists, update the user for subsequent pushes
			User loggedUser = User.get(principal.id);
			if (!(loggedUser.apnsToken.equals(userInstance.apnsToken))) {
				log.warn("A user has a new apns token. The user id is " + principal.id);
				loggedUser.apnsToken = userInstance.apnsToken;
				loggedUser.save(failOnError:true);
			}

			handlerService.handleSuccess('com.moovt.Login.success', null, "\"additionalInfo\":" + ([JSESSIONID: sessionId, userType: userType] as JSON));
		} catch (TenantUserPasswordAuthenticationException e) {
			handlerService.handleUserError('com.moovt.invalid.usernamepassword', null);
		} catch (Throwable e) {
			handlerService.handleException(e);
		}


	}
}

