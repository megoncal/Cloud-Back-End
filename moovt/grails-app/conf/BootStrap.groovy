import grails.converters.JSON

import java.text.SimpleDateFormat

import com.moovt.DynamicEnum
import com.moovt.common.Location
import com.moovt.common.LocationType;
import com.moovt.common.Role
import com.moovt.common.Tenant
import com.moovt.common.User
import com.moovt.common.UserRole
import com.moovt.taxi.CarType
import com.moovt.taxi.Driver
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import com.moovt.taxi.RideStatus
import com.moovt.CallResult

class BootStrap {

	def grailsApplication
	def dynEnumService

	def init = { //A couple of Marshallers
		servletContext ->
		JSON.registerObjectMarshaller(Ride) {
			def returnArray = [:]
			returnArray['id'] = it.id
			returnArray['version'] = it.version
			returnArray['rideStatus'] = dynEnumService.getDynEnum (it.rideStatus)
			returnArray['carType'] = dynEnumService.getDynEnum (it.carType)
			returnArray['driver'] = it.driver
			returnArray['passenger'] = it.passenger
			returnArray['pickUpDateTime'] = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(it.pickupDateTime)
			returnArray['pickUpLocation'] = it.pickUpLocation
			returnArray['dropOffLocation'] = it.dropOffLocation
			returnArray['rating'] = it.rating
			returnArray['comments'] = it.comments
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
			returnArray['id'] = it.id
			returnArray['servedLocation'] = it.servedLocation
			returnArray['carType'] = dynEnumService.getDynEnum (it.carType)
			returnArray['activeStatus'] = dynEnumService.getDynEnum (it.activeStatus)
			return returnArray
		}

		JSON.registerObjectMarshaller(Passenger) {
			def returnArray = [:]
			returnArray['id'] = it.id
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
		
		
		//Moovt
		def moovtAdminUser = new User(
				tenantId: 1,
				createdBy: 1,
				lastUpdatedBy: 1,
				username: 'admin',
				password: 'admin',
				email: 'admin@moovit.com',
				firstName: 'Joe',
				lastName: 'Doe',
				phone: '800-800-2020',
				enabled: true,
				accountExpired: false,
				accountLocked: false,
				passwordExpired: false,
				locale: 'en_US').save(failOnError: true)

		def moovtTenant = Tenant.findByName('Moovt') ?: new Tenant(name: 'Moovt', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
		def moovtAdminRole = Role.findByTenantIdAndAuthority(moovtTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: moovtTenant.id, createdBy: moovtAdminUser.id,	lastUpdatedBy: moovtAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true)

		if (!moovtAdminUser.authorities.contains(moovtAdminRole)) {
			UserRole.create ( moovtTenant.id, moovtAdminUser, moovtAdminRole, moovtAdminUser.id, moovtAdminUser.id)
		}


		//naSavassi
		def naSavassiTenant = Tenant.findByName('naSavassi') ?: new Tenant(name: 'naSavassi', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
		def naSavassiAdminUser = User.findByTenantIdAndUsername(naSavassiTenant.id, 'admin') ?: new User(
				tenantId: naSavassiTenant.id,
				createdBy: moovtAdminUser.id,
				lastUpdatedBy: moovtAdminUser.id,
				username: 'admin',
				password: 'admin',
				email: 'admin@naSavassi.com',
				firstName: 'Admin',
				lastName: 'Admin',
				phone: '800-800-8080',
				enabled: true,
				accountExpired: false,
				accountLocked: false,
				passwordExpired: false,
				locale: 'pt_BR').save(failOnError: true)

		def naSavassiAdminRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: naSavassiTenant.id,createdBy: naSavassiAdminUser.id,	lastUpdatedBy: naSavassiAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true)
		def naSavassiGuestRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_GUEST') ?: new Role(tenantId: naSavassiTenant.id, createdBy: naSavassiAdminUser.id,	lastUpdatedBy: naSavassiAdminUser.id, authority: 'ROLE_GUEST').save(failOnError: true)
		def naSavassiItemMgrRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_ITEM_MGR') ?: new Role(tenantId: naSavassiTenant.id, createdBy: naSavassiTenant.id,	lastUpdatedBy: naSavassiTenant.id, authority: 'ROLE_ITEM_MGR').save(failOnError: true)
		def naSavassiItemVwrRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_ITEM_VWR') ?: new Role(tenantId: naSavassiTenant.id, createdBy: naSavassiTenant.id,	lastUpdatedBy: naSavassiTenant.id, authority: 'ROLE_ITEM_VWR').save(failOnError: true)



		if (!naSavassiAdminUser.authorities.contains(naSavassiItemMgrRole)) {
			UserRole.create ( naSavassiTenant.id, naSavassiAdminUser, naSavassiItemMgrRole, naSavassiAdminUser.id, naSavassiAdminUser.id)
		}
		if (!naSavassiAdminUser.authorities.contains(naSavassiAdminRole)) {
			UserRole.create ( naSavassiTenant.id, naSavassiAdminUser, naSavassiAdminRole, naSavassiAdminUser.id, naSavassiAdminUser.id)
		}

		def naSavassiGuestUser = User.findByTenantIdAndUsername(naSavassiTenant.id, 'userWithAnotherEmail') ?: new User(
				tenantId: naSavassiTenant.id,
				createdBy: moovtAdminUser.id,
				lastUpdatedBy: moovtAdminUser.id,
				username: 'duplicateUser',
				password: 'guest',
				email: 'existingEmail@test.com',
				firstName: 'Joe',
				lastName: 'Doe',
				phone: '800-800-2020',
				enabled: true,
				accountExpired: false,
				accountLocked: false,
				passwordExpired: false,
				locale: 'en_US').save(failOnError: true)

		if (!naSavassiGuestUser.authorities.contains(naSavassiGuestRole)) {
			UserRole.create ( naSavassiTenant.id, naSavassiGuestUser, naSavassiGuestRole, naSavassiAdminUser.id, naSavassiAdminUser.id)
		}

		if (!naSavassiGuestUser.authorities.contains(naSavassiItemVwrRole)) {
			UserRole.create ( naSavassiTenant.id, naSavassiGuestUser, naSavassiItemVwrRole, naSavassiAdminUser.id, naSavassiAdminUser.id)
		}

		//WorldTaxi
		def worldTaxiTenant = Tenant.findByName('WorldTaxi') ?: new Tenant(name: 'WorldTaxi', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
		def worldTaxiAdminUser = User.findByTenantIdAndUsername(worldTaxiTenant.id, 'admin') ?: new User(
				tenantId: worldTaxiTenant.id,
				createdBy: moovtAdminUser.id,
				lastUpdatedBy: moovtAdminUser.id,
				username: 'admin',
				password: 'admin',
				email: 'admin@worldtaxi.com',
				firstName: 'Admin',
				lastName: 'Admin',
				phone: '800-800-8080',
				enabled: true,
				accountExpired: false,
				accountLocked: false,
				passwordExpired: false,
				locale: 'en_US').save(failOnError: true)

		def worldTaxiAdminRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: worldTaxiTenant.id,createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true);
		def worldTaxiRideMgrRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_RIDE_MGR') ?: new Role(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_RIDE_MGR').save(failOnError: true);
		def worldTaxiDriverRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_DRIVER') ?: new Role(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_DRIVER').save(failOnError: true);
		def worldTaxiPassengerRole = Role.findByTenantIdAndAuthority(worldTaxiTenant.id, 'ROLE_PASSENGER') ?: new Role(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, authority: 'ROLE_PASSENGER').save(failOnError: true);

		if (!worldTaxiAdminUser.authorities.contains(worldTaxiAdminRole)) {
			UserRole.create ( worldTaxiTenant.id, worldTaxiAdminUser, worldTaxiAdminRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
		}


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

		if (!worldTaxiPassengerUser.authorities.contains(worldTaxiRideMgrRole)) {
			UserRole.create ( worldTaxiTenant.id, worldTaxiPassengerUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
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
		if (!worldTaxiDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, worldTaxiDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User worldTaxiDriverSpeedy = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jspeedy',	password: 'Welcome!1',	email: 'jspeedy@worldtaxi.com', firstName: 'John', lastName: 'Speedy', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
		if (!worldTaxiDriverSpeedy.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, worldTaxiDriverSpeedy, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!worldTaxiDriverSpeedy.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, worldTaxiDriverSpeedy, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User worldTaxiDriverForgetful = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'jforgetful',	password: 'Welcome!1',	email: 'jforgetful@worldtaxi.com', firstName: 'John', lastName: 'Forgetful', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true))).save(failOnError: true);
		if (!worldTaxiDriverForgetful.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, worldTaxiDriverForgetful, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!worldTaxiDriverForgetful.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, worldTaxiDriverForgetful, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		
		User napervilleCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'napervilleCityDriverUser',	password: 'Welcome!1',	email: 'napervilleCityDriverUser@worldtaxi.com', firstName: 'napervilleCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),	)).save(failOnError: true);
		if (!napervilleCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, napervilleCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!napervilleCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, napervilleCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }
		
		User wheatonCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'wheatonCityDriverUser',	password: 'Welcome!1',	email: 'wheatonCityDriverUser@worldtaxi.com', firstName: 'wheatonCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: wheatonCity,	)).save(failOnError: true);
		if (!wheatonCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, wheatonCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!wheatonCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, wheatonCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User auroraCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'auroraCityDriverUser',	password: 'Welcome!1',	email: 'auroraCityDriverUser@worldtaxi.com', firstName: 'auroraCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: auroraCity,	)).save(failOnError: true);
		if (!auroraCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, auroraCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!auroraCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, auroraCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User plainfieldCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'plainfieldCityDriverUser',	password: 'Welcome!1',	email: 'plainfieldCityDriverUser@worldtaxi.com', firstName: 'plainfieldCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: plainfieldCity,	)).save(failOnError: true);
		if (!plainfieldCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, plainfieldCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!plainfieldCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, plainfieldCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User detroitCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'detroitCityDriverUser',	password: 'Welcome!1',	email: 'detroitCityDriverUser@worldtaxi.com', firstName: 'detroitCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: detroitCity,	)).save(failOnError: true);
		if (!detroitCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, detroitCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!detroitCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, detroitCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		
		
		
		User contagemCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'contagemCityDriverUser',	password: 'Welcome!1',	email: 'contagemCityDriverUser@worldtaxi.com', firstName: 'contagemCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: contagemCity,	)).save(failOnError: true);
		if (!contagemCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, contagemCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!contagemCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, contagemCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User vespasianoCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'vespasianoCityDriverUser',	password: 'Welcome!1',	email: 'vespasianoCityDriverUser@worldtaxi.com', firstName: 'vespasianoCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: vespasianoCity,	)).save(failOnError: true);
		if (!vespasianoCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, vespasianoCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!vespasianoCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, vespasianoCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

		User carmoDoCajuruCityDriverUser = new User(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, username: 'carmoDoCajuruCityDriverUser',	password: 'Welcome!1',	email: 'carmoDoCajuruCityDriverUser@worldtaxi.com', firstName: 'carmoDoCajuruCityDriverUser', lastName: 'Goodarm', phone: '800-800-2020', driver: new Driver(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, carType: CarType.B_VAN, servedLocation: carmoDoCajuruCity,	)).save(failOnError: true);
		if (!carmoDoCajuruCityDriverUser.authorities.contains(worldTaxiDriverRole)) { UserRole.create ( worldTaxiTenant.id, carmoDoCajuruCityDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)	}
		if (!carmoDoCajuruCityDriverUser.authorities.contains(worldTaxiRideMgrRole)) {	UserRole.create ( worldTaxiTenant.id, carmoDoCajuruCityDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id) }

	
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
				carType: CarType.A_SEDAN).save(failOnError: true);

			def rideTwo = new Ride(
				tenantId: worldTaxiTenant.id,
				createdBy: worldTaxiAdminUser.id,
				lastUpdatedBy: worldTaxiAdminUser.id,
				passenger: worldTaxiPassengerUser.passenger,
				pickupDateTime: pickupDateTime,
				pickUpLocation: wheatonCity,
				dropOffLocation: new Location(tenantId: worldTaxiTenant.id, createdBy: worldTaxiAdminUser.id,	lastUpdatedBy: worldTaxiAdminUser.id, locationName:'Naperville',  politicalName:'Illinois, United States', latitude: 41.78586290, longitude: -88.14728930, locationType: LocationType.APPROXIMATE).save(failOnError: true),
				rideStatus: RideStatus.UNASSIGNED,
				carType: CarType.A_SEDAN).save(failOnError: true);

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
				carType: CarType.A_SEDAN).save(failOnError: true);

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
	}
	def destroy = {
	}
}