package com.moovt
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User


/**
 * This is the extention of a <code>GrailsUser</code> required to add the tenantId and locale of the user.
 * 
 *  @author egoncalves
 */
class CustomGrailsUser extends GrailsUser {

   final Long tenantId

   CustomGrailsUser(String username, String password, boolean enabled,
                 boolean accountNonExpired, boolean credentialsNonExpired,
                 boolean accountNonLocked,
                 Collection<GrantedAuthority> authorities,
                 long id, Long tenantId) {
      super(username, password, enabled, accountNonExpired,
            credentialsNonExpired, accountNonLocked, authorities, id)

	  this.tenantId = tenantId
   }
}