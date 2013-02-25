package com.moovt

import com.grailsrocks.functionaltest.*

class LoginFunctionalTests extends BrowserTestCase {
	void testLoginBadMessage() {
		post('/login/authenticateUser') {
			body {
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "A JSONObject text must begin with '{' at character 0 of"
	}
	void testLoginSuccessEnglish() {
		post('/login/authenticateUser') {
			body {
				"""
				{"type":"Moovt","tenantname": "naSavassi", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "Login successfull"
	}

	void testLoginSuccessEnglish() {
		post('/login/authenticateUser') {
			body {
				"""
				{"type":"Moovt","tenantname": "naSavassi", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "Login bem sucedido."
	}
	
	//TODO: Translate login error messages
	//TODO: Other functional tests for Users
}


