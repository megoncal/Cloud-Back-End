import grails.converters.JSON

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

import java.text.SimpleDateFormat

import com.dumbster.smtp.SimpleSmtpServer;
import com.moovt.DynamicEnum
import com.moovt.NotificationServer
import com.moovt.common.Location
import com.moovt.common.LocationType;
import com.moovt.common.Role
import com.moovt.common.Tenant
import com.moovt.common.User
import com.moovt.common.UserRole
import com.moovt.common.MyTest
import com.moovt.common.NotificationTask
import com.moovt.common.TaskType
import com.moovt.common.TaskStatus
import com.moovt.taxi.CarType
import com.moovt.taxi.Driver
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import com.moovt.taxi.RideStatus
import com.moovt.CallResult
import grails.plugin.mail.MailService
import com.moovt.NotificationService

class BootStrap {

	def grailsApplication
	def dynEnumService
	def mailService
	def notificationService
	Thread notificationThread

	def init = { //A couple of Marshallers
		servletContext ->
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

		JSON.registerObjectMarshaller(MyTest) {
			def returnArray = [:]
			returnArray['a'] = it.a
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


		boolean platformFlag = grailsApplication.config.moovt.load.platform.data;
		log.info ("The load platform data flag is " + platformFlag);

		def worldTaxiTenant = null;
		def worldTaxiAdminUser = null;
		def worldTaxiDriverRole = null;
		def worldTaxiPassengerRole = null;

		if (platformFlag) {
			//Moovt (seed)
			def moovtAdminUser = new User(
					tenantId: 1,
					createdBy: 1,
					lastUpdatedBy: 1,
					username: 'admin',
					password: '911admin',
					email: 'admin@moovit.com',
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



			//WorldTaxi
			worldTaxiTenant = Tenant.findByName('WorldTaxi') ?: new Tenant(name: 'WorldTaxi', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
			worldTaxiAdminUser = User.findByTenantIdAndUsername(worldTaxiTenant.id, 'admin') ?: new User(
					tenantId: worldTaxiTenant.id,
					createdBy: moovtAdminUser.id,
					lastUpdatedBy: moovtAdminUser.id,
					username: 'admin',
					password: '911admin',
					email: 'admin@worldtaxi.com',
					firstName: 'System',
					lastName: 'Manager',
					phone: '800-800-8080',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true)

			worldTaxiDriverRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_DRIVER') ?: new Role(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_DRIVER').save(failOnError: true);
			worldTaxiPassengerRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_PASSENGER') ?: new Role(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_PASSENGER').save(failOnError: true);

			def worldTaxiAdminRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: worldTaxiTenant.id,createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true);

			if (!worldTaxiAdminUser.authorities.contains(worldTaxiAdminRole)) {
				UserRole.create ( worldTaxiTenant.id, worldTaxiAdminUser, worldTaxiAdminRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
			}

		}


		boolean testDataFlag = grailsApplication.config.moovt.load.test.data;
		log.info ("The load test data is " + testDataFlag);

		if (testDataFlag && !platformFlag) {
			log.error ("This is a configuration error. Trying to load test data without platform data");
			throw new Exception();
		}

		if (testDataFlag) {



			User worldTaxiPassengerUser = new User(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					username: 'jgoodrider',
					password: 'Welcome!1',
					email: 'jgoodrider@worldtaxi.com',
					firstName: 'John',
					lastName: 'Goodrider',
					phone: '800-800-8080',
					passenger: new Passenger(tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id)
					).save(failOnError: true);

			if (!worldTaxiPassengerUser.authorities.contains(worldTaxiPassengerRole)) {
				UserRole.create ( worldTaxiTenant.id, worldTaxiPassengerUser, worldTaxiPassengerRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
			}

			//Location to create different drivers - US

			Location wheatonCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Wheaton',  politicalName:'Illinois, United States', latitude: 41.8661403, longitude: -88.1070127, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location auroraCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Aurora',  politicalName:'Illinois, United States', latitude: 41.7605849, longitude: -88.32007150000001, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location plainfieldCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Plainfield',  politicalName:'Illinois, United States', latitude: 41.632223, longitude: -88.2120315, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location detroitCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Detroit',  politicalName:'Michigan, United States', latitude:42.331427, longitude: -83.0457538, locationType: LocationType.APPROXIMATE).save(failOnError: true);

			Location contagemCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Contagem',  politicalName:'Minas Gerais, Brazil', latitude: -19.9385599, longitude: -44.0529377, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location vespasianoCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Vespasiano',  politicalName:'Minas Gerais, Brazil', latitude: -19.6933911, longitude: -43.913722, locationType: LocationType.APPROXIMATE).save(failOnError: true);
			Location carmoDoCajuruCity = new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Carmo do Cajuru',  politicalName:'Minas Gerais, Brazil', latitude: -20.1870332, longitude: -44.7731276, locationType: LocationType.APPROXIMATE).save(failOnError: true);



			//Drivers for the citeis above
			User worldTaxiDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jgoodarm',	password: 'Welcome!1',	email: 'jgoodarm@worldtaxi.com', firstName: 'John', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!worldTaxiDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, worldTaxiDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User worldTaxiDriverSpeedy = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jspeedy',	password: 'Welcome!1',	email: 'jspeedy@worldtaxi.com', firstName: 'John', lastName: 'Speedy', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!worldTaxiDriverSpeedy.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, worldTaxiDriverSpeedy, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User worldTaxiDriverForgetful = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jforgetful',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'jforgetful@worldtaxi.com', firstName: 'John', lastName: 'Forgetful', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!worldTaxiDriverForgetful.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, worldTaxiDriverForgetful, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}


			User napervilleCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'napervilleCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'napervilleCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!napervilleCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, napervilleCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User wheatonCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'wheatonCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'wheatonCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: wheatonCity)).save(failOnError: true);
			if (!wheatonCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, wheatonCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User auroraCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'auroraCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'auroraCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: auroraCity)).save(failOnError: true);
			if (!auroraCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, auroraCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User plainfieldCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'plainfieldCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'plainfieldCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: plainfieldCity)).save(failOnError: true);
			if (!plainfieldCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, plainfieldCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User detroitCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'detroitCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'detroitCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: detroitCity)).save(failOnError: true);
			if (!detroitCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, detroitCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}




			User contagemCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'contagemCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'contagemCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: contagemCity)).save(failOnError: true);
			if (!contagemCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, contagemCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User vespasianoCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'vespasianoCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'vespasianoCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: vespasianoCity)).save(failOnError: true);
			if (!vespasianoCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, vespasianoCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User carmoDoCajuruCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'carmoDoCajuruCityDriverUser',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'carmoDoCajuruCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: carmoDoCajuruCity)).save(failOnError: true);
			if (!carmoDoCajuruCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, carmoDoCajuruCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			//Drivers for the token test
			User jWillGainApnsTokenTaxiDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jWillGainApnsToken',	password: 'Welcome!1',	apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504', email: 'egoncalves@moovt.com', firstName: 'JWillGainApnsToken', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!worldTaxiDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, jWillGainApnsTokenTaxiDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}

			User jKeepApnsTokenTaxiDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jKeepApnsToken',	password: 'Welcome!1', email: 'egoncalves@moovt.com', firstName: 'JKeepApnsToken', lastName: 'Goodarm', phone: '800-800-2020', apnsToken: '9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504',driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
			if (!worldTaxiDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, jKeepApnsTokenTaxiDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}


			//Rides

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date pickupDateTime = simpleDateFormat.parse("2013-03-13 20:10");

			def rideOne = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN,
					pickUpLocationComplement: 'Close to Vest Ride 1',
					messageToTheDriver: 'Please come fast ride 1').save(failOnError: true);

			def rideTwo = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					driver:worldTaxiDriverUser.driver,
					pickupDateTime: pickupDateTime,
					pickUpLocation: wheatonCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.ASSIGNED,
					carType: CarType.A_SEDAN,
					pickUpLocationComplement: 'Close to Vest Ride 2',
					messageToTheDriver: 'Please come fast ride 2').save(failOnError: true);

			def rideThree = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);

			def rideFour = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					driver:worldTaxiDriverUser.driver,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.ASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);

			def rideFive = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.COMPLETED,
					carType: CarType.A_SEDAN,
					rating: 3.5,
					comments: 'Great Driver',
					pickUpLocationComplement: 'Close to Vest Ride 5',
					messageToTheDriver: 'Please come fast Ride 5').save(failOnError: true);

			def rideSix = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);


			def rideSeven = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);

			def rideEight = new Ride(
					tenantId: worldTaxiTenant.id,
					createdBy: worldTaxiAdminUser.id,
					lastUpdatedBy: worldTaxiAdminUser.id,
					passenger: worldTaxiPassengerUser.passenger,
					pickupDateTime: pickupDateTime,
					pickUpLocation: contagemCity,
					dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
					rideStatus: RideStatus.UNASSIGNED,
					carType: CarType.A_SEDAN).save(failOnError: true);
		}


		//		notificationThread = Thread.start {
		//			try {
		//				Boolean runFlag = grailsApplication.config.moovt.run.notification.server;
		//				if (!runFlag) {
		//					log.info("The Notification thread is configured not to run on this server (please check the run.notification.server property)")
		//
		//				} else {
		//					log.info("Notification thread running.... ");
		//					while (true){
		//						log.info("Notification thread checking for work to do");
		//						int batchSize = notificationService.processNotification();
		//						if (batchSize == 0) {
		//							log.info("Notification thread is going to sleep for 1 minute");
		//							Thread.sleep(60000);
		//						}
		//					}
		//				}
		//			}
		//	catch (Throwable e) {
		//				log.info("Something went wrong or Notification Thread was interrupted " + e.message);
		//				e.printStackTrace();
		//			}
		//		}


	}
	def destroy = {
		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (InterruptedException e) {
			logger.warn("SEVERE problem cleaning up: " + e.getMessage());
			e.printStackTrace();
		}
	}
}



//server - mandatory - complete ride sending two messagens (Eduardo) - OK
//
//client - mandatory - app crashing after editing driver details (Marcos)
//
//client - mandatory - Rate ride view controller long text.... check for a place holder... (Marcos)
//
//client - mandatory - Design app icon and app launch screen (Marcos)
//
//server - mandatory - redundance and test enviroment (Eduardo) - OK
//
//server - mandatory - stop sending cancel email (Eduardo) - OK
//
//client / server - mandatory - test locations... ibis savassi... (Marcos/Eduardo)
//
//client / server - mandatory - test push notification... (Marcos / Eduardo)
//
//client - mandatory - prepare app to be deployed at apple store.. (Marcos)
//
//client - secondary - add status to ride detail view controller and then add comments and rating (Marcos)
//
//server - secondary - deactivate driver (Eduardo)
//
//server - secondary - notification one hour prior to scheduled ride (Marcos / Eduardo)
//
//server - nice to have - refactory - Generic Audit Fields (version, lastUpdated, etc) (Eduardo)
//
//client - nice to have - refactory - Rationalize keyboard on UI (Eduardo)
//
//client - nice to have - automatic update when scroll down
//
//client - nice to have - after take ride present my rides list