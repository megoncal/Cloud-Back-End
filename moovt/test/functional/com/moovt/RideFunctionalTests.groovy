package com.moovt

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

	void testRetrieveUnassignedRideInDriverMetroEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodarm","password":"Welcome!1"}
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
		assertContentContains "id\":2"
		assertContentContains "passenger\":{\"id\":5}"
	}

	void testAssignRideToDriverEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"WorldTaxi","username":"jgoodarm","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/assignRideToDriver') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"id":"1","version":"1"}
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
		
	
}


