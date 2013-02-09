package com.moovt

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.codehaus.groovy.grails.plugins.springsecurity.RequestHolderAuthenticationFilter
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.util.TextEscapeUtils

class TenantFilter extends RequestHolderAuthenticationFilter {

	@Override
	Authentication attemptAuthentication(HttpServletRequest request,
	                                     HttpServletResponse response)
			throws AuthenticationException {

				log.info("1.HERE")
		if (!request.post) {
			throw new AuthenticationServiceException(
				"Authentication method not supported: $request.method")
		}

		String username = (obtainUsername(request) ?: '').trim()
		String password = obtainPassword(request) ?: ''
		String orgName = request.getParameter('orgName')

		def authentication = new TenantAuthenticationToken(username, password, orgName)

		HttpSession session = request.getSession(false)
		if (session || getAllowSessionCreation()) {
			request.session[SPRING_SECURITY_LAST_USERNAME_KEY] =
				TextEscapeUtils.escapeEntities(username)
		}

		setDetails request, authentication

		return getAuthenticationManager().authenticate(authentication)
	}
}
