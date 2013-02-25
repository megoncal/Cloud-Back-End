import com.moovt.TenantAuthenticationProvider
import com.moovt.TenantFilter
import com.moovt.CustomLogoutSuccessHandler
import com.moovt.CustomAccessDeniedHandler
import com.moovt.CustomExceptionTranslationFilter;
import org.springframework.security.web.access.AccessDeniedHandlerImpl

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
beans = {

def conf = SpringSecurityUtils.securityConfig


 
  exceptionTranslationFilter(CustomExceptionTranslationFilter) {
  }
  
  
 
	// custom authentication
	daoAuthenticationProvider(TenantAuthenticationProvider) {
		passwordEncoder = ref('passwordEncoder')
		saltSource = ref('saltSource')
		preAuthenticationChecks = ref('preAuthenticationChecks')
		postAuthenticationChecks = ref('postAuthenticationChecks')
	}

}
