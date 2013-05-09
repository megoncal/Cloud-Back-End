package com.moovt

import com.grailsrocks.functionaltest.*

class UserFunctionalTests extends BrowserTestCase {
	void testCreateUserBadMessage() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			body {
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "A JSONObject text must begin with '{' at character 0 of"
	}

	void testCreateUserBadTenantEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "TheBadTenant", "email":"movieGoer@test.com","username": "moovieGoer", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "This tenant (TheBadTenant) does not exist. Please use an existing tenant to create this user."
	}

	void testCreateUserSuccessEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "moovieGoer", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User moovieGoer created"
	}
	
	void testCreateUserSuccessPortuquese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieLover@test.com", "username": "moovieLover", "password":"moovieLover", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "Usuário moovieLover criado"
	}

	void testCreateUserNoUserNameEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noUserName@test.com", "username": "", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The user name must be provided"
	}

	void testCreateUserNoUserNamePortuguese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noUserName@test.com", "username": "", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "O nome do usuário deve ser preenchido"
	}
	
	void testCreateUserNoPasswordEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noPasswordUser@test.com", "username": "movieFan", "password":"", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The password must be provided"
	}
	
	void testCreateUserNoPasswordPortuguese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noPasswordUser@test.com", "username": "movieFan", "password":"", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "A senha deve ser preenchida."
	}
	
	void testCreateUserNoEmailEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "naSavassi","email":"", "username": "movieFan", "password":"Welcome!1", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The email must be provided"
	}
	
	void testCreateUserNoEmailPortuguese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "naSavassi","email":"", "username": "movieFan", "password":"Welcome!1", "firstName":"TestFirstName", "lastName": "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "O email deve ser preenchido"
	}

	void testCreateUserDuplicateUsernameEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "naSavassi", "email":"duplicateUsernameTestEnglish@test.com", "username": "duplicateUser", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This user (duplicateUser) already exist"
	}

	void testCreateUserDuplicateUsernamePortuguese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "naSavassi", "email":"duplicateUsernameTestPortuguese@test.com", "username": "duplicateUser", "password":"moovieLover", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}				
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este usuário (duplicateUser) já existe"
	}

	void testCreateUserDuplicateEmailEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "naSavassi", "email":"existingEmail@test.com", "username": "userWithSameEmail", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This email (existingEmail@test.com) already exists"
	}

	void testCreateUserDuplicateEmailPortuguese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "naSavassi", "email":"existingEmail@test.com", "username": "userWithSameEmail", "password":"moovieLover", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este email (existingEmail@test.com) já existe"
	}

}


