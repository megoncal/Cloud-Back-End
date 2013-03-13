import java.text.SimpleDateFormat
import java.util.Date;


import com.moovt.CustomDomainMarshaller
import com.moovt.common.Address
import com.moovt.common.Role;
import com.moovt.common.Tenant;
import com.moovt.common.User;
import com.moovt.common.UserRole;
import com.moovt.taxi.Driver
import com.moovt.taxi.CarType
import com.moovt.taxi.Passenger
import com.moovt.taxi.Ride
import com.moovt.taxi.RideStatus
import com.moovt.common.AddressType;

import grails.converters.JSON;

class BootStrap {

	def grailsApplication

	def init = {

		//A couple of Marshallers
		
		
		servletContext ->
			JSON.registerObjectMarshaller(Ride) {
				def returnArray = [:]
				returnArray['id'] = it.id
				returnArray['rideStatus'] = it.rideStatus.toString()
				returnArray['driver'] = it.driver
				returnArray['passenger'] = it.passenger
				returnArray['pickupDateTime'] = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(it.pickupDateTime)
				returnArray['pickUpAddress'] = it.pickUpAddress
				returnArray['dropOffAddress'] = it.dropOffAddress
				returnArray['rating'] = it.rating
				returnArray['comments'] = it.comments
				return returnArray
		}
			JSON.registerObjectMarshaller(Address) {
				def returnArray = [:]
				returnArray['street'] = it.street
				returnArray['city'] = it.city
				returnArray['state'] = it.state
				returnArray['zip'] = it.zip
				returnArray['addressType'] = it.addressType.toString()
				return returnArray
		}
			JSON.registerObjectMarshaller(Passenger) {
				def returnArray = [:]
				String firstName
				String lastName
				String phone
				String email
				returnArray['firstName'] = it.firstName
				returnArray['lastName'] = it.lastName
				returnArray['phone'] = it.phone
				returnArray['email'] = it.email
				returnArray['address'] = it.address
				return returnArray
		}

			JSON.registerObjectMarshaller(Driver) {
				def returnArray = [:]
				String firstName
				String lastName
				String phone
				String email
				returnArray['firstName'] = it.firstName
				returnArray['lastName'] = it.lastName
				returnArray['phone'] = it.email
				returnArray['zip'] = it.email
				returnArray['servedMetro'] = it.servedMetro
				returnArray['carType'] = it.carType.toString()
				return returnArray
		}
			
					
		//servletContext ->
		//JSON.registerObjectMarshaller(new CustomDomainMarshaller(true, grailsApplication),1)
		

		//grailsApplication.mainContext.getBean("customObjectMarshallers" ).register();
		
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
		
		
		Address address = new Address (street: "123 Main St", city: "Wheanton", state: "IL", zip: "00001", addressType: AddressType.HOME);

		User worldTaxiPassengerUser = new User(
			tenantId: worldTaxiTenant.id,
			createdBy: worldTaxiAdminUser.id,
			lastUpdatedBy: worldTaxiAdminUser.id,
			username: 'jgoodrider',
			password: 'Welcome!1',
			email: 'jgoodrider@worldtaxi.com',
			firstName: 'John',
			lastName: 'Goodrider',
			phone: '800-800-8080').save(failOnError: true);

						
		def worldTaxiPassenger = new Passenger(
			tenantId: worldTaxiTenant.id,
			createdBy: worldTaxiAdminUser.id,
			lastUpdatedBy: worldTaxiAdminUser.id)
		worldTaxiPassenger.id = worldTaxiPassengerUser.id;
		worldTaxiPassenger.save(failOnError: true);

			if (!worldTaxiPassengerUser.authorities.contains(worldTaxiPassengerRole)) {
				UserRole.create ( worldTaxiTenant.id, worldTaxiPassengerUser, worldTaxiPassengerRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
			}
			
			if (!worldTaxiPassengerUser.authorities.contains(worldTaxiRideMgrRole)) {
				UserRole.create ( worldTaxiTenant.id, worldTaxiPassengerUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
			}

			User worldTaxiDriverUser = new User(
				tenantId: worldTaxiTenant.id,
				createdBy: worldTaxiAdminUser.id,
				lastUpdatedBy: worldTaxiAdminUser.id,
				username: 'jgoodarm',
				password: 'Welcome!1',
				email: 'jgoodarm@worldtaxi.com',
				firstName: 'John',
				lastName: 'Goodarm',
				phone: '800-800-2020').save(failOnError: true);
	
						
			def worldTaxiDriver = new Driver(
				id: worldTaxiDriverUser.id,
				tenantId: worldTaxiTenant.id,
				createdBy: worldTaxiAdminUser.id,
				lastUpdatedBy: worldTaxiAdminUser.id,
				carType: CarType.VAN,
				servedMetro:"Chicago-Naperville-Joliet, IL");
			worldTaxiDriver.id = worldTaxiDriverUser.id;
			worldTaxiDriver.save(failOnError: true);
	
				if (!worldTaxiDriverUser.authorities.contains(worldTaxiDriverRole)) {
					UserRole.create ( worldTaxiTenant.id, worldTaxiDriverUser, worldTaxiDriverRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
				}
				
				if (!worldTaxiDriverUser.authorities.contains(worldTaxiRideMgrRole)) {
					UserRole.create ( worldTaxiTenant.id, worldTaxiDriverUser, worldTaxiRideMgrRole, worldTaxiAdminUser.id, worldTaxiAdminUser.id)
				}
			
		Address pickUpAddress = new Address (street: "123 Main St", city: "Wheanton", state: "IL", zip: "00001", addressType: AddressType.HOME);
		Address dropOffAddress = new Address (street: "123 Main St", city: "Wheanton", state: "IL", zip: "00001", addressType: AddressType.HOME);
				
		def ride = new Ride(
			tenantId: worldTaxiTenant.id,
			createdBy: worldTaxiAdminUser.id,
			lastUpdatedBy: worldTaxiAdminUser.id,
			passenger: worldTaxiPassenger,
			pickupDateTime: new Date(),
			pickUpAddress: pickUpAddress,
			dropOffAddress: dropOffAddress,
			rideStatus: RideStatus.UNASSIGNED).save(failOnError: true);
			
//		if (Item.count()==0) {
//			Item item = new Item (
//					tenantId: naSavassiTenant.id,
//					title: 'Dadiva',
//					shortDescription: 'Very Good Restaurant',
//					longDescription: 'Very Good Restaurant',
//					type: 'com.moovt.Item.type.RESTAURANT',
//					createdBy: naSavassiAdminUser.id,
//					lastUpdatedBy: naSavassiAdminUser.id,
//					latitude:41.757437,
//					longitude:-88.073585
//					);
//			item.save();
//
//			new Item (
//					tenantId: naSavassiTenant.id,
//					title: 'Meu Restaurante 2',
//					shortDescription: 'Very Good Restaurant',
//					longDescription: 'Very Good Restaurant',
//					type: 'com.moovt.Item.type.RESTAURANT',
//					createdBy: naSavassiAdminUser.id,
//					lastUpdatedBy: naSavassiAdminUser.id,
//					latitude:80.757437,
//					longitude:88.073585
//					).save();
//
//			new Item (
//					tenantId: naSavassiTenant.id,
//					title: 'Meu Bar 1',
//					shortDescription: 'Very Good Bar',
//					longDescription: 'Very Good Bar',
//					type: 'com.moovt.Item.type.BAR',
//					createdBy: naSavassiAdminUser.id,
//					lastUpdatedBy: naSavassiAdminUser.id,
//					latitude:45.757437,
//					longitude:-80.073585
//					).save();
//
//			new Item (
//					tenantId: naSavassiTenant.id,
//					title: 'Meu Bar 2',
//					shortDescription: 'Very Good Bar',
//					longDescription: 'Very Good Bar',
//					type: 'com.moovt.Item.type.BAR',
//					createdBy: naSavassiAdminUser.id,
//					lastUpdatedBy: naSavassiAdminUser.id,
//					latitude:35.757437,
//					longitude:-80.073585
//					).save();
//		}

//		if (Asset.count() == 0) {
//
//			for (int i = 0; i < 3; i++) {
//
//				String a = 'Short Description ' + i;
//				String b = 'Long Description ' + i;
//
//				new Asset(
//						tenantId: moovtTenant.id,
//						shortDescription: a, longDescription: b
//						).save();
//			}
//		}

	}
	def destroy = {
	}
}