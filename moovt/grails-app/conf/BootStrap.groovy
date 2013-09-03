import grails.converters.JSON

import java.text.SimpleDateFormat

import org.springframework.security.core.context.SecurityContextHolder

import com.moovt.CallResult
import com.moovt.DynamicEnum
import com.moovt.common.Role
import com.moovt.common.Tenant
import com.moovt.common.User
import com.moovt.common.UserRole
import com.moovt.taxi.CarType
import com.moovt.taxi.Driver
import com.moovt.taxi.Location
import com.moovt.taxi.LocationType
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import com.moovt.taxi.RideStatus
import com.mysql.jdbc.AbandonedConnectionCleanupThread

class BootStrap {

	def grailsApplication
	def notificationService
	def dynEnumService
	Thread notificationThread

	def init = { servletContext ->

		//TODO: Make registering JSON in Bootstrap scalable
		JSON.registerObjectMarshaller(Ride) {
			def returnArray = [:]
			returnArray['id'] = it.id
			returnArray['version'] = it.version
			returnArray['rideStatus'] = dynEnumService.getDynEnum (it.rideStatus)
			returnArray['carType'] = dynEnumService.getDynEnum (it.carType)
			returnArray['driver'] = (it.driver ? it.driver.user : null)
			returnArray['passenger'] = it.passenger.user
			returnArray['pickUpDateTime'] = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(it.pickupDateTime)
			returnArray['pickUpLocation'] = it.pickUpLocation
			returnArray['dropOffLocation'] = it.dropOffLocation
			returnArray['rating'] = it.rating
			returnArray['comments'] = it.comments
			returnArray['pickUpLocationComplement'] = it.pickUpLocationComplement
			returnArray['messageToTheDriver'] = it.messageToTheDriver
			return returnArray
		}


		JSON.registerObjectMarshaller(User) {
			def returnArray = [:]
			returnArray['id'] = it.id
			returnArray['version'] = it.version
			returnArray['username'] = it.username
			returnArray['firstName'] = it.firstName
			returnArray['lastName'] = it.lastName
			returnArray['phone'] = it.phone
			returnArray['email'] = it.email
			if (it.driver) {
				returnArray['driver'] = it.driver
			}

			if (it.passenger) {
				returnArray['passenger'] = it.passenger
			}
			return returnArray
		}

		JSON.registerObjectMarshaller(Driver) {
			def returnArray = [:]
			//returnArray['id'] = it.id
			returnArray['servedLocation'] = it.servedLocation
			returnArray['carType'] = dynEnumService.getDynEnum (it.carType)
			returnArray['activeStatus'] = dynEnumService.getDynEnum (it.activeStatus)
			return returnArray
		}

		JSON.registerObjectMarshaller(Passenger) {
			def returnArray = [:]
			//returnArray['id'] = it.id
			return returnArray
		}

		JSON.registerObjectMarshaller(Location) {
			def returnArray = [:]
			returnArray['locationName'] = it.locationName
			returnArray['politicalName'] = it.politicalName
			returnArray['latitude'] = it.latitude
			returnArray['longitude'] = it.longitude
			returnArray['locationType'] = it.locationType.toString()
			return returnArray
		}


		JSON.registerObjectMarshaller(DynamicEnum) {
			def returnArray = [:]
			returnArray['code'] = it.code
			returnArray['description'] = it.description
			return returnArray
		}

		JSON.registerObjectMarshaller(CallResult) {
			def returnArray = [:]
			returnArray['type'] = it.type
			returnArray['code'] = it.code
			returnArray['message'] = it.message
			return returnArray
		}


		log.info ("Starting Bootstrap Init ");

		boolean platformFlag = grailsApplication.config.moovt.load.platform.data;
		log.info ("The load platform data flag is " + platformFlag);

		def mTaxiTenant = null;
		def mTaxiAdminUser = null;
		def mTaxiDriverRole = null;
		def mTaxiPassengerRole = null;

		if (platformFlag) {
			//Moovt (seed)
			def moovtAdminUser = new User(
					tenantId: 1,
					createdBy: 1,
					lastUpdatedBy: 1,
					username: 'admin',
					password: '911admin',
					email: 'admin@moovt.com',
					firstName: 'System',
					lastName: 'Manager',
					phone: 'N/A',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true);

			def moovtTenant = Tenant.findByName('Moovt') ?: new Tenant(name: 'Moovt', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
			def moovtAdminRole = Role.findByTenantIdAndAuthority(moovtTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: moovtTenant.id, createdBy: moovtAdminUser.id,	lastUpdatedBy: moovtAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true)

			if (!moovtAdminUser.authorities.contains(moovtAdminRole)) {
				UserRole.create ( moovtTenant.id, moovtAdminUser, moovtAdminRole, moovtAdminUser.id, moovtAdminUser.id)
			}



			//Mtaxi
			mTaxiTenant = Tenant.findByName('MTaxi') ?: new Tenant(name: 'MTaxi', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
			mTaxiAdminUser = User.findByTenantIdAndUsername(mTaxiTenant.id, 'admin') ?: new User(
					tenantId: mTaxiTenant.id,
					createdBy: moovtAdminUser.id,
					lastUpdatedBy: moovtAdminUser.id,
					username: 'admin',
					password: '911admin',
					email: 'mtaxi@moovt.com',
					firstName: 'Moovt',
					lastName: 'Taxi',
					phone: '800-800-8080',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true)

			mTaxiDriverRole = Role.findByTenantIdAndAuthority(mTaxiTenant.id, 'ROLE_DRIVER') ?: new Role(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, authority: 'ROLE_DRIVER').save(failOnError: true);
			mTaxiPassengerRole = Role.findByTenantIdAndAuthority(mTaxiTenant.id, 'ROLE_PASSENGER') ?: new Role(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, authority: 'ROLE_PASSENGER').save(failOnError: true);

			def mTaxiAdminRole = Role.findByTenantIdAndAuthority(mTaxiTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: mTaxiTenant.id,createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true);

			if (!mTaxiAdminUser.authorities.contains(mTaxiAdminRole)) {
				UserRole.create ( mTaxiTenant.id, mTaxiAdminUser, mTaxiAdminRole, mTaxiAdminUser.id, mTaxiAdminUser.id)
			}

		}


		boolean testDataFlag = grailsApplication.config.moovt.load.test.data;
		log.info ("The load test data is " + testDataFlag);

		if (testDataFlag && !platformFlag) {
			log.error ("This is a configuration error. It is not possible to load test data without platform data");
			throw new Exception("This is a configuration error. It is not possible to load test data without platform data");
		}

		if (testDataFlag) {

			//For testing duplicate User
			def moovtDuplicateUser = new User(
					tenantId: 1,
					createdBy: 1,
					lastUpdatedBy: 1,
					username: 'duplicateUser',
					password: 'Welcome!1',
					email: 'duplicateUser@moovt.com',
					firstName: 'duplicateUser',
					lastName: 'duplicateUser',
					phone: 'N/A',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true);

			//For testing existing email
			def moovtExistingEmailUser = new User(
					tenantId: 1,
					createdBy: 1,
					lastUpdatedBy: 1,
					username: 'existingEmail',
					password: 'Welcome!1',
					email: 'existingEmail@moovt.com',
					firstName: 'existingEmail',
					lastName: 'existingEmail',
					phone: 'N/A',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true);

			//For testing password forgotten	
			def moovtForgotPasswordlUser = new User(
					tenantId: 1,
					createdBy: 1,
					lastUpdatedBy: 1,
					username: 'jforgetful',
					password: 'Welcome!1',
					email: 'jforgetful@moovt.com',
					firstName: 'jforgetful',
					lastName: 'jforgetful',
					phone: 'N/A',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true);

			//Passenger User 
				
			User mTaxiPassengerUser = new User(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					username: 'jgoodrider',
					password: 'Welcome!1',
					email: 'jgoodrider@mtaxi.com',
					firstName: 'John',
					lastName: 'Goodrider',
					phone: '800-800-8080',
					passenger: new Passenger(tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id)
					).save(failOnError: true);


			if (!mTaxiPassengerUser.authorities.contains(mTaxiPassengerRole)) {
				UserRole.create ( mTaxiTenant.id, mTaxiPassengerUser, mTaxiPassengerRole, mTaxiAdminUser.id, mTaxiAdminUser.id)
			}

			//Location to create different drivers - US
			Location wheatonCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Wheaton',  politicalName:'Illinois, United States', latitude: 41.8661403, longitude: -88.1070127, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location auroraCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Aurora',  politicalName:'Illinois, United States', latitude: 41.7605849, longitude: -88.32007150000001, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location plainfieldCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Plainfield',  politicalName:'Illinois, United States', latitude: 41.632223, longitude: -88.2120315, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location detroitCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Detroit',  politicalName:'Michigan, United States', latitude:42.331427, longitude: -83.0457538, locationType: LocationType.APPROXIMATE).save(failOnError: true);

			Location contagemCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Contagem',  politicalName:'Minas Gerais, Brazil', latitude: -19.9385599, longitude: -44.0529377, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location vespasianoCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Vespasiano',  politicalName:'Minas Gerais, Brazil', latitude: -19.6933911, longitude: -43.913722, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location carmoDoCajuruCity = new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Carmo do Cajuru',  politicalName:'Minas Gerais, Brazil', latitude: -20.1870332, longitude: -44.7731276, locationType: LocationType.APPROXIMATE).save(failOnError: true);



			//Drivers for the citeis above
			User mTaxiDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'jgoodarm',	password: 'Welcome!1',	email: 'jgoodarm@mtaxi.com', firstName: 'John', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!mTaxiDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, mTaxiDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User mTaxiDriverSpeedy = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'jspeedy',	password: 'Welcome!1',	email: 'jspeedy@mtaxi.com', firstName: 'John', lastName: 'Speedy', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!mTaxiDriverSpeedy.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, mTaxiDriverSpeedy, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User napervilleCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'napervilleCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'napervilleCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!napervilleCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, napervilleCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User wheatonCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'wheatonCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'wheatonCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: wheatonCity)).save(failOnError: true);
			if (!wheatonCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, wheatonCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User auroraCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'auroraCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'auroraCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: auroraCity)).save(failOnError: true);
			if (!auroraCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, auroraCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User plainfieldCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'plainfieldCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'plainfieldCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: plainfieldCity)).save(failOnError: true);
			if (!plainfieldCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, plainfieldCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User detroitCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'detroitCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'detroitCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: detroitCity)).save(failOnError: true);
			if (!detroitCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, detroitCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}




			User contagemCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'contagemCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'contagemCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: contagemCity)).save(failOnError: true);
			if (!contagemCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, contagemCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User vespasianoCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'vespasianoCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'vespasianoCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: vespasianoCity)).save(failOnError: true);
			if (!vespasianoCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, vespasianoCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User carmoDoCajuruCityDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'carmoDoCajuruCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'carmoDoCajuruCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: carmoDoCajuruCity)).save(failOnError: true);
			if (!carmoDoCajuruCityDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, carmoDoCajuruCityDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			//Drivers for the token test
			User jWillGainApnsTokenTaxiDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'jWillGainApnsToken',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'JWillGainApnsToken', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!mTaxiDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, jWillGainApnsTokenTaxiDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}

			User jKeepApnsTokenTaxiDriverUser = new User(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, username: 'jKeepApnsToken',	password: 'Welcome!1', email: 'egoncalves@moovt.com', firstName: 'JKeepApnsToken', lastName: 'Goodarm', phone: '800-800-2020', apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504',driver: new Driver(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!mTaxiDriverUser.authorities.contains(mTaxiDriverRole)) { UserRole.create ( mTaxiTenant.id, jKeepApnsTokenTaxiDriverUser, mTaxiDriverRole, mTaxiAdminUser.id, mTaxiAdminUser.id)	}


			//Rides

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date pickupDateTime = simpleDateFormat.parse("2013-03-13 20:10");

			def rideOne = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN,
					pickUpLocationComplement: 'Close to Vest Ride 1',
					messageToTheDriver: 'Please come fast ride 1').save(failOnError: true);

			def rideTwo = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					driver:mTaxiDriverUser.driver,
					pickupDateTime: pickupDateTime,
					pickUpLocation: wheatonCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.ASSIGNED,
					carType: CarType.A_SEDAN,
					pickUpLocationComplement: 'Close to Vest Ride 2',
					messageToTheDriver: 'Please come fast ride 2').save(failOnError: true);

			def rideThree = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);

			def rideFour = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					driver:mTaxiDriverUser.driver,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.ASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);

			def rideFive = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.COMPLETED,
					carType: CarType.A_SEDAN,
					rating: 3.5,
					comments: 'Great Driver',
					pickUpLocationComplement: 'Close to Vest Ride 5',
					messageToTheDriver: 'Please come fast Ride 5').save(failOnError: true);

			def rideSix = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);


			def rideSeven = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);

			def rideEight = new Ride(
					tenantId: mTaxiTenant.id,
					createdBy: mTaxiAdminUser.id,
					lastUpdatedBy: mTaxiAdminUser.id,
					passenger: mTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: mTaxiTenant.id, createdBy: mTaxiAdminUser.id,	lastUpdatedBy: mTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);
		}


				notificationThread = Thread.start {
					try {
						boolean runFlag = grailsApplication.config.moovt.run.notification.server;
						int sleepTime = grailsApplication.config.moovt.notification.server.sleep.time;
		
						if (!runFlag) {
							log.info("The Notification thread is configured not to run on this server (please check the run.notification.server property)")
		
						} else {
							log.info("Notification thread is configured to run on this server");
							while (true){
								log.info("Notification thread checking for work to do");
								int batchSize = notificationService.processNotification();
								if (batchSize == 0) {
									log.info("Notification thread is going to sleep for " + sleepTime + " seconds(s)");
									Thread.sleep(sleepTime*1000);
								}
							}
						}
					} catch (InterruptedException e) {
						log.info("Notification Thread has been interrupted and will stop running");
					} catch (Throwable e) {
						log.info("Something went wrong in the Notification Thread " + e.message);
						e.printStackTrace();
					}
				}


	}
	def destroy = {
		log.info ("Starting Bootstrap destroy");
		try {
			SecurityContextHolder.clearContext();
			AbandonedConnectionCleanupThread.shutdown();
			notificationThread.interrupt();
		} catch (Exception e) {
			log.info ("Problem found while destroying the app " +  e.getLocalizedMessage());
			e.printStackTrace();
		}
		log.info ("Done destroying the app");
	}
}