grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.dependency.cache.dir="/study/dependencies-app/grails-ivy-cache"
grails.project.plugins.dir="/study/dependencies-app/grails-plugins"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]
//grails.plugin.location.audit ="/study/work/audit"

grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
	legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

	repositories {
		inherits true // Whether to inherit repository definitions from plugins

		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenLocal()
		mavenCentral()

		// uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
		//mavenRepo "http://snapshots.repository.codehaus.org"
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
	}

	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.

		compile 'mysql:mysql-connector-java:5.1.25'
		compile 'com.fasterxml.jackson.core:jackson-core:2.2.3'
		compile 'com.fasterxml.jackson.core:jackson-databind:2.2.3'
		compile 'com.fasterxml.jackson.core:jackson-annotations:2.2.3'

	}

	plugins {
		runtime ":hibernate:$grailsVersion"
		runtime ":jquery:1.8.3"
		runtime ":resources:1.2"
		runtime ":apns:1.0"

		build ":tomcat:$grailsVersion"

		//runtime ":database-migration:1.3.2"

		compile ':cache:1.0.1'
		//compile ':spring-security-core:1.2.7.3'
		compile ":mail:1.0.1"
		compile ":rest:0.7"
		compile ":functional-test:2.0.RC1"
		compile(":multi-tenant-security:0.1") {
			changing = true // should force Grails to check Maven repo for update instead of retrieving old version from Ivy cache
		}

	}
}
