import java.util.Date;

import com.moovt.*

import grails.converters.JSON;

class BootStrap {

	def grailsApplication

	def init = {

		servletContext ->
		JSON.registerObjectMarshaller(new CustomDomainMarshaller(true, grailsApplication),1)

		def moovtAdminUser = new User(
				tenantId: 1,
				createdBy: 1,
				lastUpdatedBy: 1,
				username: 'admin',
				password: 'admin',
				email: 'admin@moovit.com',
				enabled: true,
				accountExpired: false,
				accountLocked: false,
				passwordExpired: false,
				locale: 'en_US').save(failOnError: true)

		def moovtTenant = Tenant.findByName('Moovt') ?: new Tenant(name: 'Moovt', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)
		def naSavassiTenant = Tenant.findByName('naSavassi') ?: new Tenant(name: 'naSavassi', createdBy: moovtAdminUser.id, lastUpdatedBy:moovtAdminUser.id).save(failOnError: true)

		def moovtAdminRole = Role.findByTenantIdAndAuthority(moovtTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: moovtTenant.id, createdBy: moovtAdminUser.id,	lastUpdatedBy: moovtAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true)


		if (!moovtAdminUser.authorities.contains(moovtAdminRole)) {
			UserRole.create ( moovtTenant.id, moovtAdminUser, moovtAdminRole, moovtAdminUser.id, moovtAdminUser.id)
		}

		def naSavassiAdminUser = User.findByTenantIdAndUsername(naSavassiTenant.id, 'admin') ?: new User(
				tenantId: naSavassiTenant.id,
				createdBy: moovtAdminUser.id,
				lastUpdatedBy: moovtAdminUser.id,
				username: 'admin',
				password: 'admin',
				email: 'admin@naSavassi.com',
				enabled: true,
				accountExpired: false,
				accountLocked: false,
				passwordExpired: false,
				locale: 'pt_BR').save(failOnError: true)

		def naSavassiAdminRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: naSavassiTenant.id,createdBy: naSavassiAdminUser.id,	lastUpdatedBy: naSavassiAdminUser.id, authority: 'ROLE_ADMIN').save(failOnError: true)
		def naSavassiGuestRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_GUEST') ?: new Role(tenantId: naSavassiTenant.id, createdBy: naSavassiAdminUser.id,	lastUpdatedBy: naSavassiAdminUser.id, authority: 'ROLE_GUEST').save(failOnError: true)


		if (!naSavassiAdminUser.authorities.contains(naSavassiAdminRole)) {
			UserRole.create ( naSavassiTenant.id, naSavassiAdminUser, naSavassiAdminRole, naSavassiAdminUser.id, naSavassiAdminUser.id)
		}



		if (Item.count()==0) {
			Item item = new Item (
					tenantId: naSavassiTenant.id,
					title: 'Dadiva',
					shortDescription: 'Very Good Restaurant',
					longDescription: 'Very Good Restaurant',
					type: 'com.moovt.Item.type.RESTAURANT',
					createdBy: naSavassiAdminUser.id,
					lastUpdatedBy: naSavassiAdminUser.id,
					latitude:41.757437,
					longitude:-88.073585
					);
			item.save();

			new Item (
					tenantId: naSavassiTenant.id,
					title: 'Meu Restaurante 2',
					shortDescription: 'Very Good Restaurant',
					longDescription: 'Very Good Restaurant',
					type: 'com.moovt.Item.type.RESTAURANT',
					createdBy: naSavassiAdminUser.id,
					lastUpdatedBy: naSavassiAdminUser.id,
					latitude:80.757437,
					longitude:88.073585
					).save();

			new Item (
					tenantId: naSavassiTenant.id,
					title: 'Meu Bar 1',
					shortDescription: 'Very Good Bar',
					longDescription: 'Very Good Bar',
					type: 'com.moovt.Item.type.BAR',
					createdBy: naSavassiAdminUser.id,
					lastUpdatedBy: naSavassiAdminUser.id,
					latitude:45.757437,
					longitude:-80.073585
					).save();

			new Item (
					tenantId: naSavassiTenant.id,
					title: 'Meu Bar 2',
					shortDescription: 'Very Good Bar',
					longDescription: 'Very Good Bar',
					type: 'com.moovt.Item.type.BAR',
					createdBy: naSavassiAdminUser.id,
					lastUpdatedBy: naSavassiAdminUser.id,
					latitude:35.757437,
					longitude:-80.073585
					).save();
		}

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