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

//Taxi Tests
	void testCreateUserDriverEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname":"WorldTaxi","firstName":"David","lastName":"Ultrafast","username":"dultrafast","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","radiusServed":"RADIUS_50","activeStatus":"ENABLED"}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User dultrafast created"
	}
		
	void testCreateUserPassengerEnglish() {
		post('/user/createUser') {
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
				{"version":"4","firstName":"John","lastName":"VeryGoodarm","username":"jverygoodarm","password":"Welcome!1","phone":"773-329-1784","email":"jgoodarxm@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","radiusServed":"RADIUS_50","activeStatus":"ENABLED"}}
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
				{"version":"7","firstName":"John","lastName":"DecidedToBeADriver","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"en-US","driver":{"carType":"SEDAN","servedLocation":"Chicago, IL, USA","radiusServed":"RADIUS_50","activeStatus":"ENABLED"}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User jgoodrider updated"
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
		
		post('/user/retrieveAllUsers') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "users"
		assertContentContains "firstName\":\"Admin"
		assertContentContains "firstName\":\"John"
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
		
		post('/user/retrieveUserDetailById') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"6"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "user"
		assertContentContains "firstName\":\"John"
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
		
		post('/user/retrieveLoggedUserDetails') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "username\":\"jgoodrider"
	}
		
}


