package com.moovt.common
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * This class contains the API to logout a <code>User</code>
 *
 * @author egoncalves
 *
 */
class LogoutController {

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
