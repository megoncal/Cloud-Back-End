import java.util.Date;

import com.moovt.*

import grails.converters.JSON;

class BootStrap {

	def grailsApplication
	
	def init = {
		
 	  servletContext ->
		JSON.registerObjectMarshaller(new CustomDomainMarshaller(true, grailsApplication),1)

		def moovtTenant = Tenant.findByName('Moovt') ?: new Tenant(name: 'Moovt').save(failOnError: true)
		def naSavassiTenant = Tenant.findByName('naSavassi') ?: new Tenant(name: 'naSavassi').save(failOnError: true)

		def moovtAdminRole = Role.findByTenantIdAndAuthority(moovtTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: moovtTenant.id, authority: 'ROLE_ADMIN').save(failOnError: true)
		def naSavassiAdminRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_ADMIN') ?: new Role(tenantId: naSavassiTenant.id, authority: 'ROLE_ADMIN').save(failOnError: true)
		def naSavassiGuestRole = Role.findByTenantIdAndAuthority(naSavassiTenant.id, 'ROLE_GUEST') ?: new Role(tenantId: naSavassiTenant.id, authority: 'ROLE_GUEST').save(failOnError: true)
		
		def moovtAdminUser = User.findByTenantIdAndUsername(moovtTenant.id, 'admin') ?: new User(
					tenantId: moovtTenant.id,
					username: 'admin',
					password: 'admin',
					email: 'admin@moovit.com',
					enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
					locale: 'en_US').save(failOnError: true)

			if (!moovtAdminUser.authorities.contains(moovtAdminRole)) {
				UserRole.create ( moovtTenant.id, moovtAdminUser, moovtAdminRole)
			}

			def naSavassiAdminUser = User.findByTenantIdAndUsername(naSavassiTenant.id, 'admin') ?: new User(
				tenantId: naSavassiTenant.id,
				username: 'admin',
				password: 'admin',
				email: 'admin@moovit.com',
				enabled: true,
					accountExpired: false,
					accountLocked: false,
					passwordExpired: false,
				locale: 'pt_BR').save(failOnError: true)

		if (!naSavassiAdminUser.authorities.contains(naSavassiAdminRole)) {
			log.info("Bootstrap : Creating Role")
			UserRole.create ( naSavassiTenant.id, naSavassiAdminUser, naSavassiAdminRole)
		}



			if (Asset.count() == 0) {

				for (int i = 0; i < 3; i++) {

					String a = 'Short Description ' + i;
					String b = 'Long Description ' + i;

					new Asset(
						        tenantId: moovtTenant.id,
								shortDescription: a, longDescription: b
								).save();
				}
			}
			
			log.info ("Bootstrap " + Item.count())
			if (Item.count()==0) {
				log.info("Inserting Items")
				Item item = new Item (
					tenantId: naSavassiTenant.id,
					title: 'Dadiva',
					shortDescription: 'Very Good Restaurant',
					longDescription: 'Very Good Restaurant',
					type: 'com.moovt.Item.type.RESTAURANT',
					creationDate: new Date(),
					lastUpdateDate: new Date() ,
					createdBy: 1,
					lastUpdatedBy: 1,
					latitude:41.757437,
					longitude:-88.073585
					);
				item.save();
				log.info("Saved");
				for (fieldErrors in item.errors) {
					for (error in fieldErrors.allErrors) {
						//TODO: Investigate Field Error Handling
						log.info(error);
					}
				}

				
				new Item (
					tenantId: naSavassiTenant.id,
					title: 'Meu Restaurante 2',
					shortDescription: 'Very Good Restaurant',
					longDescription: 'Very Good Restaurant',
					type: 'com.moovt.Item.type.RESTAURANT',
					creationDate: new Date(),
					lastUpdateDate: new Date() ,
					createdBy: 1,
					lastUpdatedBy: 1,
					latitude:80.757437,
					longitude:88.073585
					).save();
				
				new Item (
					tenantId: naSavassiTenant.id,
					title: 'Meu Bar 1',
					shortDescription: 'Very Good Bar',
					longDescription: 'Very Good Bar',
					type: 'com.moovt.Item.type.BAR',
					creationDate: new Date() ,
					lastUpdateDate: new Date() ,
					createdBy: 1,
					lastUpdatedBy: 1,
					latitude:45.757437,
					longitude:-80.073585
					).save();
				
				new Item (
					tenantId: naSavassiTenant.id,
					title: 'Meu Bar 2',
					shortDescription: 'Very Good Bar',
					longDescription: 'Very Good Bar',
					type: 'com.moovt.Item.type.BAR',
					creationDate: new Date() ,
					lastUpdateDate: new Date() ,
					createdBy: 1,
					lastUpdatedBy: 1,
					latitude:35.757437,
					longitude:-80.073585
					).save();
			}

	}
	def destroy = {
	}
}