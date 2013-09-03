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
	void testLoginSuccessEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "JSESSIONID"
		assertContentContains "userType"
		assertContentContains "Login successful"
	}

	void testLoginSuccessPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "USER"
		assertContentContains "SUCCESS"
		assertContentContains "JSESSIONID"
		assertContentContains "userType"
		assertContentContains "Login bem sucedido."
	}

	void testLoginNoTenantEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Authentication failed because no company was provided. Please enter a company."
	}

	void testLoginNoTenantPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "A companhia deve ser preenchida"
	}
	
	void testLoginTenantNotFoundEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "BadTenant", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This company (BadTenant) was not found. Please enter a valid company name."
	}

	void testLoginTenantNotFoundPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			headers['Accept-Charset'] = 'UTF-8'
			body {
				"""
				{"type":"Self","tenantname": "BadTenant", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Esta companhia (BadTenant) não foi encontrada"
	}

	void testLoginNoPasswordEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "MTaxi", "username": "admin", "password":""}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Authentication failed because no password was provided. Please enter a password."
	}
	
	void testLoginNoUsernamedEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "MTaxi", "username": "", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Authentication failed because no username was provided. Please enter a username."
	}
	
	void testLoginFailedEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "MTaxi", "username": "admin", "password":"badPw"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "User name and or password is invalid. Please try again."
	}
	
	void testLoginNoPasswordPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "MTaxi", "username": "admin", "password":""}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "A senha deve ser preenchida."
	}
	
	void testLoginNoUsernamedPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "MTaxi", "username": "", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "O nome do usuário deve ser preenchido."
	}
	
	void testLoginFailedPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "MTaxi", "username": "admin", "password":"badPw"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Usuário e senha inválidos."
	} 

	void testLoginWithApnsToken() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jWillGainApnsToken","password":"Welcome!1","apnsToken":"9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504"}

				"""
			}
		}
		assertStatus 200
		//TODO: Check that jWillGainApnsToken gains a apnsToken
	}
	
	void testLoginKeepApnsToken() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jKeepApnsToken","password":"Welcome!1","apnsToken":"9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504"}

				"""
			}
		}
		assertStatus 200
		//TODO: Check that jKeepApnsToken does not loose his apnsToken
	}
}


