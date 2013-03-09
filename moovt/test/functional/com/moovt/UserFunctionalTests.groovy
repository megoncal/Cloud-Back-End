package com.moovt

import com.grailsrocks.functionaltest.*

class UserFunctionalTests extends BrowserTestCase {
	void testCreateUserInExistingTenantBadMessage() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "A JSONObject text must begin with '{' at character 0 of"
	}

	void testCreateUserInExistingTenantBadTenantEnglish() {
		post('/user/createUserInExistingTenant') {
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

	void testCreateUserInExistingTenantSuccessEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "moovieGoer", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User moovieGoer created"
	}
	
	void testCreateUserInExistingTenantSuccessPortuquese() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieLover@test.com", "username": "moovieLover", "password":"moovieLover", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "Usuário moovieLover criado"
	}

	void testCreateUserInExistingTenantNoUserNameEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noUserName@test.com", "username": "", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The user name must be provided"
	}

	void testCreateUserInExistingTenantNoUserNamePortuguese() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noUserName@test.com", "username": "", "password":"moovieGoer", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "O nome do usuário deve ser preenchido"
	}
	
	void testCreateUserInExistingTenantNoPasswordEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noPasswordUser@test.com", "username": "movieFan", "password":"", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The password must be provided"
	}
	
	void testCreateUserInExistingTenantNoPasswordPortuguese() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"noPasswordUser@test.com", "username": "movieFan", "password":"", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "A senha deve ser preenchida."
	}
	
	void testCreateUserInExistingTenantNoEmailEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi","email":"", "username": "movieFan", "password":"", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The email must be provided"
	}
	
	void testCreateUserInExistingTenantNoEmailPortuguese() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi","email":"", "username": "movieFan", "password":"", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "O email deve ser preenchido"
	}

	void testCreateUserInExistingTenantDuplicateUsernameEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"duplicateUsernameTestEnglish@test.com", "username": "duplicateUser", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This user (duplicateUser) already exist"
	}

	void testCreateUserInExistingTenantDuplicateUsernamePortuguese() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"duplicateUsernameTestPortuguese@test.com", "username": "duplicateUser", "password":"moovieLover", "locale": "pt_BR"}				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este usuário (duplicateUser) já existe"
	}

	void testCreateUserInExistingTenantDuplicateEmailEnglish() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"existingEmail@test.com", "username": "userWithSameEmail", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This email (existingEmail@test.com) already exists"
	}

	void testCreateUserInExistingTenantDuplicateEmailPortuguese() {
		post('/user/createUserInExistingTenant') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "naSavassi", "email":"existingEmail@test.com", "username": "userWithSameEmail", "password":"moovieLover", "locale": "pt_BR"}				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este email (existingEmail@test.com) já existe"
	}

	
}


