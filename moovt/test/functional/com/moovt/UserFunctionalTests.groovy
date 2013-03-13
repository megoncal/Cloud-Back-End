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

//Taxi Tests
	void testCreateUserDriverEnglish() {
		post('/user/createOrUpdateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname":"WorldTaxi","firstName":"David","lastName":"Ultrafast","username":"dultrafast","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedMetro":"Chicago-Naperville-Joliet, IL","activeStatus":"ENABLED"}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User dultrafast created"
	}
		
	void testCreateUserPassengerEnglish() {
		post('/user/createOrUpdateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname":"WorldTaxi","firstName":"John","lastName":"Airjunkie","username":"jairjunkie","password":"Welcome!1","phone":"773-329-1784","email":"jairjunkie@worldtaxi.com","locale":"en-US","passenger":{}}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User jairjunkie created"
	}
	
	void testUpdateUserDriverEnglish() {
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodarm","password":"Welcome!1",locale:"pt_BR"}
				"""
			}
		}
		
		post('/user/updateLoggedUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"version":"4","firstName":"John","lastName":"VeryGoodarm","username":"jverygooxdarm","password":"Welcome!1","phone":"773-329-1784","email":"jgoodarxm@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedMetro":"Chicago-Naperville-Joliet, IL","activeStatus":"ENABLED"}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User jverygoodarm updated"
	}

	void testUpdateUserPassengerEnglish() {
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1",locale:"pt_BR"}
				"""
			}
		}
		
		post('/user/updateLoggedUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"version":"7","firstName":"John","lastName":"DecidedToBeADriver","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedMetro":"Chicago-Naperville-Joliet, IL","activeStatus":"ENABLED"}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User jverygoodarm updated"
	}

	void testRetrieveAllUsersrEnglish() {
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "WorldTaxi", "username": "admin", "password":"admin","locale":"en_US"}
				"""
			}
		}
		
		post('user/retrieveAllUsers') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "users"
		assertContentContains "lastName\":\"Goodrider"
		assertContentContains "users"
		assertContentContains "lastName\":\"Goodarm"
	}

	void testRetrieveUserDetailByIdEnglish() {
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "WorldTaxi", "username": "admin", "password":"admin","locale":"en_US"}
				"""
			}
		}
		
		post('user/retrieveUserDetailById') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"6"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "user"
		assertContentContains "lastName\":\"Goodarm"
	}
	
	
	void testRetrieveLoggedUserDetailsIdEnglish() {
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1",locale:"pt_BR"}
				"""
			}
		}
		
		post('user/retrieveLoggedUserDetails') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "user"
		assertContentContains "lastName\":\"Goodrider"
	}
		
}


