package com.moovt

//TODO: Rationalize Queries check test testCreateUserDuplicateUsernameEnglish

import com.grailsrocks.functionaltest.*;
import com.dumbster.smtp.*;

class RideFunctionalTests extends BrowserTestCase {


	void testRetrievePassengerRidesEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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
		assertContentContains "{\"rides\":[{\"id\":1,\"version\":0,\"rideStatus\":"
	}

	void testRetrieveUnassignedRideInServedAreaEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jspeedy","password":"Welcome!1"}
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
		assertContentContains "UNASSIGNED"
	}

	void testRetrieveUnassignedRideInServedAreaNoRideFoundEnglish() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"detroitCityDriverUser","password":"Welcome!1"}
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
//TODO: assert more specific
			}
	
	void testAssignRideToDriverEnglish() {

		//SimpleSmtpServer server = SimpleSmtpServer.start();
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jspeedy","password":"Welcome!1"}
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
		
		//server.stop();
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
		
		}

	void testRetrieveAllRidesEnglish() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"admin","password":"911admin"}
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

	void testRetrieveAssignedRidesEnglish() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
                        {"type":"Self","tenantname":"MTaxi","username":"jgoodarm","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/retrieveAssignedRides') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{}
				"""
					}
				}
				assertStatus 200
				//TODO: Include more tests
			}
		
	
	void testCreateRideENglish() {
		
		//SimpleSmtpServer server = SimpleSmtpServer.start();
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
			}
		}

		post('/ride/createRide') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
{
  "id" : 0,
  "pickUpLocation" : {
    "locationName" : "Naperville",
    "longitude" : -88.14729,
    "latitude" : 41.78586,
    "politicalName" : "Illinois, United States",
    "locationType" : "APPROXIMATE"
  },
  "pickUpLocationComplement" : "",
  "comments" : "",
  "rating" : "",
  "dropOffLocation" : {
    "locationName" : "Wheaton",
    "longitude" : -88.10701,
    "latitude" : 41.86614,
    "politicalName" : "Illinois, United States",
    "locationType" : "APPROXIMATE"
  },
  "version" : 0,
  "messageToTheDriver" : "",
  "carType" : {
    "code" : "B_VAN",
    "description" : "Van"
  },
  "pickUpDateTime" : "2013-06-05 23:04"
}
				"""
			}
		}
		
		//server.stop();
		
		assertStatus 200
		assertContentContains "SUCCESS"
		assertContentContains "USER"
		assertContentContains "created"
		
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
		
	}


	void testCloneRide() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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
				assertContentContains "rideStatus\":{\"code\":\"UNASSIGNED\""
			}
	
	void testCloneRideBadMessage() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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

		//SimpleSmtpServer server = SimpleSmtpServer.start();
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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
		
		//server.stop();
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
		
	}



		

	void testCloseRideSuccessPortuguese() {

		//SimpleSmtpServer server = SimpleSmtpServer.start();
		
		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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
		
		//server.stop();
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
	}





	void testCloseRideBadMessage() {

		post('/login/authenticateUser') {
			headers['Content-Type'] = 'application/json'
			body {
				"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
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

	void testCloseRideUnsassignedError() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/closeRide') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"id":"8","version":"1","rating":"5","comments":"Great ride!"}
				"""
					}
				}
				assertStatus 200
				assertContentContains "ERROR"
				assertContentContains "USER"
				assertContentContains "This ride is unassigned and can't be completed"
			}
		
	void testCancelRideSuccessEnglish() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/cancelRide') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"id":"6","version":"1"}
				"""
					}
				}
				assertStatus 200
				assertContentContains "SUCCESS"
				assertContentContains "Ride #6 canceled"
			}
		
	void testCancelRideSuccessPortuguese() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/cancelRide') {
					headers['Content-Type'] = 'application/json'
					headers['Accept-Language'] = 'pt-BR'
					body {
						"""
				{"id":"7","version":"1"}
				"""
					}
				}
				assertStatus 200
				assertContentContains "SUCCESS"
				assertContentContains "Corrida #7 cancelada."
			}
		
	void testCancelRideNotFoundEnglish() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/cancelRide') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"id":"40","version":"1"}
				"""
					}
				}
				assertStatus 200
				assertContentContains "ERROR"
				assertContentContains "Ride #40 not found"
			}
		
	void testCancelRideNotFoundPortuguese() {
		
				post('/login/authenticateUser') {
					headers['Content-Type'] = 'application/json'
					body {
						"""
				{"type":"Self","tenantname":"MTaxi","username":"jgoodrider","password":"Welcome!1"}
				"""
					}
				}
		
				post('/ride/cancelRide') {
					headers['Content-Type'] = 'application/json'
					headers['Accept-Language'] = 'pt-BR'
					body {
						"""
				{"id":"40","version":"1"}
				"""
					}
				}
				assertStatus 200
				assertContentContains "ERROR"
				assertContentContains "Corrida #40 não foi encontrada"
			}



}
