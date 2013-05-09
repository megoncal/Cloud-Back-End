package com.moovt

//TODO: Rationalize Queries check test testCreateUserDuplicateUsernameEnglish

import com.grailsrocks.functionaltest.*

class RideFunctionalTests extends BrowserTestCase {


	void testRetrievePassengerRidesEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/retrievePassengerRides') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "id\":1"
		assertContentContains "passenger\":{\"id\":5}"
	}

	void testRetrieveUnassignedRideInServedAreaEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jspeedy","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/retrieveUnassignedRideInServedArea') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "id\":1"
		assertContentContains "passenger\":{\"id\":5}"
		assertContentContains "UNASSIGNED"
	}

	void testAssignRideToDriverEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jspeedy","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/assignRideToDriver') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"3","version":"1"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "updated"
	}

	void testRetrieveAllRidesEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"admin","password":"admin"}
				"""
			}
		}

		post('/ride/retrieveAllRides') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{}
				"""
			}
		}
		assertStatus 200
		assertContentContains "pickupDateTime\":\"2013-03-13 20:10"
	}


	void testCreateRideENglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/createRide') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
{"pickupDateTime":"2013-03-15 06:30",
 "pickUpLocation":{"locationName":"Rua PickUp Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"},
 "dropOffLocation":{"locationName":"Rua DropOff Major Lopes, 55","politicalName":"Belo Horizonte, MG, BR","latitude":-19.9413628,"longitude":-43.9373064,"locationType":"RANGE_INTERPOLATED"}
}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "created"
	}


	void testCloneRide() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/cloneRide') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
{"id":"5"}				"""
					}
				}
				assertStatus 200
				assertContentContains "\"driver\":null"
				assertContentContains "\"rideStatus\":\"UNASSIGNED\""
			}
	
	void testCloneRideBadMessage() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/cloneRide') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
{"idx":"5"}
				"""
					}
				}
				assertStatus 200
				assertContentContains "ERROR"
				assertContentContains "SYSTEM"
				assertContentContains "id"
				assertContentContains "not found"
			}
	
	void testCloseRideSuccessEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/closeRide') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"4","version":"1","rating":"5","comments":"Great ride!"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "Ride 4 updated"
	}




	void testCloseRideSuccessPortuguese() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/closeRide') {
			headers['Content-Type'] = 'application/json'
			headers['Accept-Language'] = 'pt-BR'
			body {
				"""
				{"id":"2","version":"1","rating":"5","comments":"Bom motorista"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "Corrida 2 atualizado"
	}





	void testCloseRideBadMessage() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/closeRide') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"3","version":"1"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "SYSTEM"
		assertContentContains "rating"
		assertContentContains "not found"
	}




	void testCloseRideAlreadyClosed() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/closeRide') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"5","version":"1","rating":"5","comments":"Great ride!"}
				"""
			}
		}
		assertStatus 200
		assertContentContains "ERROR"
		assertContentContains "USER"
		assertContentContains "This ride has already been completed. Unable to add comment"
	}


}
