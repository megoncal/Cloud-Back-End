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
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
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
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"admin"}
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
		assertContentContains "SYSTEM"
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
		assertContentContains "SYSTEM"
		assertContentContains "ERROR"
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
		assertContentContains "SYSTEM"
		assertContentContains "This company (BadTenant) was not found"
	}

	void testLoginTenantNotFoundPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "BadTenant", "username": "admin", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SYSTEM"
		assertContentContains "ERROR"
		assertContentContains "Esta companhia (BadTenant) n‹o foi encontrada"
	}

	void testLoginNoPasswordEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":""}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "Authentication failed because no password was provided. Please enter a password."
	}
	
	void testLoginNoUsernamedEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "Authentication failed because no username was provided. Please enter a username."
	}
	
	void testLoginFailedEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'en-US'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"badPw"}
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
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":""}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "A senha deve ser preenchida."
	}
	
	void testLoginNoUsernamedPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "", "password":"admin"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "O nome do usu‡rio deve ser preenchido."
	}
	
	void testLoginFailedPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"badPw"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Usu‡rio e senha inv‡lidos."
	} 
	
}


