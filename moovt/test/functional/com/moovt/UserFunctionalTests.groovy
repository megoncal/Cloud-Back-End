package com.moovt

import com.grailsrocks.functionaltest.*

class UserFunctionalTests extends BrowserTestCase {
	void testCreateUserInExistingTenantBadMessage() {
		post('/user/createUserInExistingTenant') {
			body {
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "A JSONObject text must begin with '{' at character 0 of"
	}

	void testCreateUserInExistingTenantBadTenantEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "TheBadTenant", "username": "moovieGoer", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "This tenant (TheBadTenant) does not exist. Please use an existing tenant to create this user."
	}

	void testCreateUserInExistingTenantBadTenantPortuguese() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "TheBadTenant", "username": "moovieGoer", "password":"moovieGoer", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "Esta companhia (TheBadTenant) não existe. Favor usar uma companhia existente para criar este usuário."
	}

	void testCreateUserInExistingTenantSuccessEnglish() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "naSavassi", "username": "moovieGoer", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "User moovieGoer created"
	}
	
	void testCreateUserInExistingTenantSuccessPortuquese() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "naSavassi", "username": "moovieLover", "password":"moovieLover", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "Usuário moovieGoer created"
	}
	
	//TODO: Other functional tests for Users
}


