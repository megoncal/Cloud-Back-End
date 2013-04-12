package com.moovt

import com.grailsrocks.functionaltest.*

class LocationFunctionalTests extends BrowserTestCase {
	void testSearchBadJSON() {
		post('/location/search') {
			headers['Content-Type'] = 'application/json'
			body {
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "A JSONObject text must begin with '{' at character 0 of"
	}

	void testSearchNoLocation() {
		post('/location/search') {
			headers['Content-Type'] = 'application/json'
			body {
				
				"""
{"thisMustBeLocation":"Rua Major Lopes 55, Belo Horizonte, MG"}				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "must contain a location element"
	}

	void testSearchCity() {
		post('/location/search') {
			headers['Content-Type'] = 'application/json'
			body {
				
				"""
{"location":"Belo Horizonte, MG"}	
				"""
			}
		}
		assertStatus 200
		assertContentContains "locations"
		assertContentContains "Belo Horizonte"
	}
	
	void testSearchStreet() {
		post('/location/search') {
			headers['Content-Type'] = 'application/json'
			body {
				
				"""
{"location":"Rua Major Lopes 55, Belo Horizonte, MG"}	
				"""
			}
		}
		assertStatus 200
		assertContentContains "locations"
		assertContentContains "Belo Horizonte"
	}
	
	void testState() {
		post('/location/search') {
			headers['Content-Type'] = 'application/json'
			body {
				
				"""
{"location":"MG"}	
				"""
			}
		}
		assertStatus 200
		assertContentContains "locations"
		assertContentDoesNotContain "MG"
		assertContentDoesNotContain "Minas Gerais"
	}
	
	
}


