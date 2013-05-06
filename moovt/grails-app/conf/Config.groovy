//
grails.war.dependencies = {}


grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
	development {
		grails.serverURL = "http://localhost:8080/moovt"
		grails.logging.jul.usebridge = true
		//imageStoreDir="C:\\study\\imagestore\\"
		imageStoreDir="C:\\study\\workspace\\mworks_1.9\\moovt\\web-app\\images\\"
	}
	production {
		grails.logging.jul.usebridge = false
		// TODO: grails.serverURL = "http://www.changeme.com"
		grails.serverURL = "http://localhost:8080/moovt"
		imageStoreDir="/usr/share/tomcat6/webapps/moovt/images/"
	}
}

// log4j configuration
//log4j = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

//    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
//           'org.codehaus.groovy.grails.web.pages',          // GSP
//           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
//           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
//           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
//           'org.codehaus.groovy.grails.commons',            // core / classloading
//           'org.codehaus.groovy.grails.plugins',            // plugins
//           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
//           'org.springframework',
//           'org.hibernate',
//           'net.sf.ehcache.hibernate'
//	info  'org.codehaus.groovy.grails.web.servlet'
//	}

log4j = {
	appenders {
		console name:'stdout'
		environments {
			development {
				rollingFile name: "mworksAppender", maxFileSize: 1024,
						file: "c:\\study\\logs\\mworksApp.log"
				rollingFile name: "stacktrace", maxFileSize: 1024,
						file: "c:\\study\\logs\\mworksStackTrace.log"
			}
			production {
				rollingFile name: "mworksAppender", maxFileSize: 1024,
						file: "/usr/share/tomcat6/logs/mworksApp.log"
				rollingFile name: "stacktrace", maxFileSize: 1024,
						file: "/usr/share/tomcat6/logs/mworksStackTrace.log"
			}
		}
	}

		// other shared config
	root {
			info '*'
	}
			
}

grails.stamp.audit.createdBy = createdBy

grails.stamp.audit.lastUpdatedBy = lastUpdatedBy

grails.stamp.audit.lastUpdated = lastUpdated

grails.stamp.audit.createdDate = createdDate

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.moovt.common.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.moovt.common.UserRole'
grails.plugins.springsecurity.authority.className = 'com.moovt.common.Role'
grails.plugins.springsecurity.dao.reflectionSaltSourceProperty = 'username'
grails.plugins.springsecurity.password.encodeHashAsBase64 = true
grails.plugins.springsecurity.filterChain.filterNames = [
   'securityContextPersistenceFilter', 'logoutFilter',
   'authenticationProcessingFilter', 'securityContextHolderAwareRequestFilter',
   'anonymousAuthenticationFilter', 'rememberMeAuthenticationFilter', 
   'exceptionTranslationFilter', 'filterInvocationInterceptor'
]

grails.gorm.save.failOnError = true


moovt.driver.search.radius=66

//TODO: Finish RetrieveNearByRides
//TODO: Enhance AssignRide **********************
//TODO: Complete Ride and Review (5 stars)