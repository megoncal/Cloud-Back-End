import com.moovt.audit.CustomExceptionTranslationFilter;
import com.moovt.audit.TenantAuthenticationProvider;

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
		grailsApplication = ref('grailsApplication')
	}
	
	

}
