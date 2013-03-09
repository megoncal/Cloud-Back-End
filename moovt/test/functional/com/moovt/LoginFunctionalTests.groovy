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
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"admin","locale":"en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "JSESSIONID"
		assertContentContains "Login successful"
	}

	void testLoginSuccessPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"admin","locale":"pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "USER"
		assertContentContains "SUCCESS"
		assertContentContains "JSESSIONID"
		assertContentContains "Login bem sucedido."
	}

	void testLoginNoTenantEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "", "username": "admin", "password":"admin","locale":"en_US"}
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
			body {
				"""
				{"type":"Self","tenantname": "", "username": "admin", "password":"admin","locale":"pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "USER"
		assertContentContains "ERROR"
		assertContentContains "A companhia deve ser preenchida"
	}
	
	void testLoginTenantNotFoundEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "BadTenant", "username": "admin", "password":"admin","locale":"en_US"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This company (BadTenant) was not found"
	}

	void testLoginTenantNotFoundPortuguese() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "BadTenant", "username": "admin", "password":"admin","locale":"pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "USER"
		assertContentContains "ERROR"
		assertContentContains "Esta companhia (BadTenant) não foi encontrada"
	}

	void testLoginNoPasswordEnglish() {
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"","locale":"en_US"}
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
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "", "password":"admin","locale":"en_US"}
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
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"badPw","locale":"en_US"}
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
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"","locale":"pt_BR"}
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
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "", "password":"admin","locale":"pt_BR"}
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
			body {
				"""
				{"type":"Self","tenantname": "naSavassi", "username": "admin", "password":"badPw","locale":"pt_BR"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "Usuário e senha inválidos."
	}
	
}


