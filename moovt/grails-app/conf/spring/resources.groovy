import com.moovt.TenantAuthenticationProvider
import com.moovt.TenantFilter
import com.moovt.CustomLogoutSuccessHandler
import com.moovt.CustomAccessDeniedHandler
import com.moovt.CustomExceptionTranslationFilter;
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import com.moovt.marshalling.CustomObjectMarshallers
import com.moovt.marshalling.RideMarshaller

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
beans = {

def conf = SpringSecurityUtils.securityConfig

	//Just list all the custom marshallers here
//	customObjectMarshallers( CustomObjectMarshallers ) {
//		marshallers = [
//				new RideMarshaller()
//		]
//	}
 
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
