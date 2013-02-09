/**
 * 
 */
package com.moovt

/**
 * @author gonsaled
 *
 */
class QueryUtils {

	static String criteria(String a) {
		return a;
	
	}

	//This closure returns a closure
	static def c_c = {params->
		return {
			
			if (params.name) {
				like("name", "%${params.name}%")
			}
			if (params.type) {
				like("type", "%${params.type}%")
			}

		}
	  }
	
}
