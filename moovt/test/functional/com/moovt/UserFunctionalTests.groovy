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
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieLover@test.com", "username": "moovieLover", "password":"moovieLover", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "Usuário moovieGoer criado"
	}

	void testCreateUserInExistingTenantNoUserNameEnglish() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "", "password":"moovieGoer", "locale": "en_US"}
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
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "", "password":"moovieGoer", "locale": "pt_BR"}
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
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "movieFan", "password":"", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "The password must be provided"
	}
	
	void testCreateUserInExistingTenantNoEmailEnglish() {
		post('/user/createUserInExistingTenant') {
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
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "moovieGoer", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This user (moovieGoer) already exist"
	}

	void testCreateUserInExistingTenantDuplicateUsernamePortuguese() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieLover@test.com", "username": "moovieLover", "password":"moovieLover", "locale": "pt_BR"}				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este usuário já existe"
	}

	void testCreateUserInExistingTenantDuplicateEmailEnglish() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieGoer@test.com", "username": "userWithSameEmail", "password":"moovieGoer", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This email (movieGoer@test.com) already exists"
	}

	void testCreateUserInExistingTenantDuplicateEmailPortuguese() {
		post('/user/createUserInExistingTenant') {
			body {
				"""
				{"tenantname": "naSavassi", "email":"movieLover@test.com", "username": "userWithSameEmail", "password":"moovieLover", "locale": "pt_BR"}				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este email (movieLover@test.com) já existe"
	}

	
}


