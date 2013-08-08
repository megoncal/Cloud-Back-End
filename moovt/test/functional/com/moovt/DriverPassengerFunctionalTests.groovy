package com.moovt

import com.grailsrocks.functionaltest.*

class DriverPassengerFunctionalTests extends BrowserTestCase {


	void testGetCarTypeEnumEnglish() {
		
		
		post('/driver/getCarTypeEnum') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "[{\"code\":\"A_SEDAN\",\"description\":\"Sedan\"},{\"code\":\"B_VAN\",\"description\":\"Van\"},{\"code\":\"C_LIMO\",\"description\":\"Limo\"}]"
	}

	void testGetCarTypeEnumPortuguese() {
		
		
		post('/driver/getCarTypeEnum') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "[{\"code\":\"A_SEDAN\",\"description\":\"Sedan\"},{\"code\":\"B_VAN\",\"description\":\"Van\"},{\"code\":\"C_LIMO\",\"description\":\"Limo\"}]"
	}

	void testGetActiveStatusEnumEnglish() {
		
		
		post('/driver/getActiveStatusEnum') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "[{\"code\":\"ENABLED\",\"description\":\"Enabled\"},{\"code\":\"DISABLED\",\"description\":\"Disabled\"}]"
	}
	
	void testGetActiveStatusEnumPortugues() {
		
		
		post('/driver/getActiveStatusEnum') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "[{\"code\":\"ENABLED\",\"description\":\"Ativo\"},{\"code\":\"DISABLED\",\"description\":\"Inativo\"}]"
	}

	
		void testCreateUserDriverEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"tenantname":"WorldTaxi","firstName":"David","lastName":"Ultrafast","username":"dultrafast","password":"Welcome!1","phone":"773-329-1784","email":"dultrafast@worldtaxi.com","locale":"en-US","driver":{"carType" : {"code" : "A_SEDAN","description" : "Sedan"},"servedLocation":{"locationName":"ARua Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},"activeStatus":{"code" : "ENABLED"}}}
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
	
	void testCreateUserPassengerWithTokenEnglish() {
		post('/user/createUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"tenantname":"WorldTaxi","firstName":"John","lastName":"TestToken","username":"testToken","password":"Welcome!1","phone":"773-329-1784","email":"testToken@moovt.com","locale":"en-US","apnsToken":"9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504", "passenger":{}}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User testToken created"
	}
	
	void testUpdateUserDriverEnglish() {
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"wheatonCityDriverUser","password":"Welcome!1",locale:"pt_BR"}
				"""
			}
		}
		
		post('/user/updateLoggedUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"version":"4","firstName":"John","lastName":"OldWeathon","username":"wheatonCityDriverUser","password":"Welcome!1","phone":"773-329-1784","email":"oldWheaton@worldtaxi.com","locale":"en-US","driver":{"servedLocation":{"locationName":"Functional Test Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},"carType":{"code":"B_VAN","description":"Van"},"activeStatus":{"code":"ENABLED","description":"Enabled"}}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User wheatonCityDriverUser updated"
		assertContentContains "version"
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
				{"version":"7","firstName":"John","lastName":"DecidedToBeADriver","username":"jgoodrider","password":"Welcome!1","phone":"773-329-1784","email":"jgoodrider@worldtaxi.com","locale":"en-US","driver":{"servedLocation":{"locationName":"Rua Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},"carType":{"code":"B_VAN","description":"Van"},"activeStatus":{"code":"ENABLED","description":"Enabled"}}}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "User jgoodrider updated"
		assertContentContains "version"
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


