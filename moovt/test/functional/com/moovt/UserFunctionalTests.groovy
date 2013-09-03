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
		assertContentContains "This tenant does not exist (TheBadTenant)"
	}

	void testCreateUserSuccessEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "Moovt", "email":"movieGoer@test.com", "username": "moovieGoer", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
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
				{"tenantname": "Moovt", "email":"movieLover@test.com", "username": "moovieLover", "password":"moovieLover", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "Usuário moovieLover criado"
	}

//	void testUtf1() {
//		post('/user/utfwork') {
//			headers['Content-Type'] = 'application/json'
//			headers['Accept-Language'] = 'pt-BR'
//			body {
//				"""
//				"""
//			}
//		}
//		assertStatus 200
//		assertContentContains "á"
//		assertContentContains "ã"
//
//	}
//
//	void testUtf2() {
//		post('/user/utfbroke') {
//			headers['Content-Type'] = 'application/json'
//			headers['Accept-Language'] = 'pt-BR'
//			body {
//				"""
//				"""
//			}
//		}
//		assertStatus 200
//		assertContentContains "á"
//		assertContentContains "ã"
//
//	}

	void testCreateUserNoUserNameEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "Moovt", "email":"noUserName@test.com", "username": "", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
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
				{"tenantname": "Moovt", "email":"noUserName@test.com", "username": "", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
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
				{"tenantname": "Moovt", "email":"noPasswordUser@test.com", "username": "movieFan", "password":"", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
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
				{"tenantname": "Moovt", "email":"noPasswordUser@test.com", "username": "movieFan", "password":"", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
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
				{"tenantname": "Moovt","email":"", "username": "movieFan", "password":"Welcome!1", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
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
				{"tenantname": "Moovt","email":"", "username": "movieFan", "password":"Welcome!1", "firstName":"TestFirstName", "lastName": "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
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
				{"tenantname": "Moovt", "email":"duplicateUser@test.com", "username": "duplicateUser", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
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
				{"tenantname": "Moovt", "email":"duplicateUser@test.com", "username": "duplicateUser", "password":"moovieLover", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}				
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
				{"tenantname": "Moovt", "email":"existingEmail@moovt.com", "username": "userWithSameEmail", "password":"moovieGoer", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This email (existingEmail@moovt.com) already exists"
	}

	void testCreateUserDuplicateEmailPortuguese() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "Moovt", "email":"existingEmail@moovt.com", "username": "userWithSameEmail", "password":"moovieLover", "firstName":"TestFirstName", lastName: "TestLastName","phone":"800-800-8080", "locale": "pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este email (existingEmail@moovt.com) já existe"
	}

	void testResetPasswordSuccessEnglish() {
		
		
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "Moovt", "email":"jforgetful@moovt.com"}
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
				{"tenantname": "Moovt", "email":"jforgetful@moovt.com"}
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
				{"tenantXame": "Moovt", "email":"jforgetful@moovt.com"}
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
				{"tenantname": "Moovt", "emailX":"jforgetful@moovt.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "JSONObject[\\\"email\\\"] not found"
	}

	void testResetPasswordTenantNotFound() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "TheBadTenant", "email":"jforgetful@moovt.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "This tenant does not exist (TheBadTenant)"
	}
	
	void testResetPasswordTenantEmailNotFoundEnglish() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname": "Moovt", "email":"jforgetfulx@moovt.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This email (jforgetfulx@moovt.com) was not found in the system."
	}

	void testResetPasswordTenantEmailNotFoundPortuguese() {
		post('/user/resetPassword') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"tenantname": "Moovt", "email":"jforgetfulx@moovt.com"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Este email (jforgetfulx@moovt.com) não foi encontrado no sistema."
	}
}


