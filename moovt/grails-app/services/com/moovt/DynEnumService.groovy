package com.moovt

import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.web.servlet.support.RequestContextUtils

class DynEnumService {

	def messageSource;
	
	

		public List<DynamicEnum> getDynamicEnums(Object dynEnum) {
			def webUtils = WebUtils.retrieveGrailsWebRequest();
			Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
			List<DynamicEnum> l = new ArrayList<DynamicEnum>()
			for (item in dynEnum){
				l.add(new DynamicEnum(item.toString(),messageSource.getMessage(item.toString(), null, locale)));
			}
			return l;
		}
		
		public String getDescription(Object dynEnumElement) {
			def webUtils = WebUtils.retrieveGrailsWebRequest();
			Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
			return messageSource.getMessage(dynEnumElement.toString(), null, locale);
		}
		
		public DynamicEnum getDynEnum (Object dynEnum) {
			def webUtils = WebUtils.retrieveGrailsWebRequest();
			Locale locale = RequestContextUtils.getLocale(webUtils.getCurrentRequest());
			DynamicEnum aDynEnum = new DynamicEnum(dynEnum.toString(), messageSource.getMessage(dynEnum.toString(), null, locale));
			return aDynEnum;
			
		}
		 
	
	
}

