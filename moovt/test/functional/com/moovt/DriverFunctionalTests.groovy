package com.moovt

import com.grailsrocks.functionaltest.*

class LoginFunctionalTests extends BrowserTestCase {
	void testLoginBadMessage() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "A JSONObject text must begin with '{' at character 0 of"
	}
	
	void createDriverSuccess() {
		post('/driver/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "TheBadTenant", "email":"movieGoer@test.com","username": "moovieGoer", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "This tenant (TheBadTenant) does not exist. Please use an existing tenant to create this user."
	}
}