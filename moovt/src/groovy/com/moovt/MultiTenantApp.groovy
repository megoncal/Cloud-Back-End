package com.moovt

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

//TODO: This class is not used
class MultiTenantApp implements GrailsApplicationAware {

	GrailsApplication grailsApplication;
		
	@Override
	public void setGrailsApplication(GrailsApplication grailsApplication) {
		// TODO Auto-generated method stub
		this.grailsApplication = grailsApplication
	}
	
	public GrailsApplication getGrailsApplication(){
		return grailsApplication;
	}

}
