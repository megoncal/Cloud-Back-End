package com.moovt
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomGrailsUser extends GrailsUser {

   final Integer tenantId
   final Locale locale

   CustomGrailsUser(String username, String password, boolean enabled,
                 boolean accountNonExpired, boolean credentialsNonExpired,
                 boolean accountNonLocked,
                 Collection<GrantedAuthority> authorities,
                 long id, Integer tenantId, Locale locale) {
      super(username, password, enabled, accountNonExpired,
            credentialsNonExpired, accountNonLocked, authorities, id)

	  this.locale = locale
	  this.tenantId = tenantId
   }
}