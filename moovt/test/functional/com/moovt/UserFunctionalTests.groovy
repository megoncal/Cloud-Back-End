package com.moovt

import com.grailsrocks.functionaltest.*
import com.dumbster.smtp.*

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
		assertContentContains "Usu‡rio moovieLover criado"
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
		assertContentContains "SYSTEM"
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
		assertContentContains "SYSTEM"
		assertContentContains "O nome do usu‡rio deve ser preenchido"
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
		assertContentContains "SYSTEM"
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
		assertContentContains "SYSTEM"
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
		assertContentContains "SYSTEM"
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
		assertContentContains "SYSTEM"
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
		assertContentContains "Este usu‡rio (duplicateUser) j‡ existe"
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
		assertContentContains "Este email (existingEmail@test.com) j‡ existe"
	}

	void testResetPasswordSuccessEnglish() {
		
		
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "WorldTaxi", "email":"jforgetful@worldtaxi.com"}
				"""
			}
		}
		
		
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "Your new password was sent to "
		
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
		}

	void testResetPasswordSuccessPortuguese() {
		
				
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "WorldTaxi", "email":"jforgetful@worldtaxi.com"}
				"""
			}
		}
		
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "A sua nova senha foi enviada para "
		
		}

	void testResetPasswordBadMessageNoTenante() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantXame": "WorldTaxi", "email":"jforgetful@worldtaxi.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "JSONObject[\\\"tenantname\\\"] not found"
	}

	void testResetPasswordBadMessageNoEmail() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname": "WorldTaxi", "emailX":"jforgetful@worldtaxi.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "JSONObject[\\\"email\\\"] not found"
	}

	void testResetPasswordTenantEmailNotFoundEnglish() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "WorldTaxi", "email":"jforgetfulx@worldtaxi.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This email (jforgetfulx@worldtaxi.com) was not found in the system."
	}

	void testResetPasswordTenantEmailNotFoundPortuguese() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "WorldTaxi", "email":"jforgetfulx@worldtaxi.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este email (jforgetfulx@worldtaxi.com) n‹o foi encontrado no sistema."
	}
}


