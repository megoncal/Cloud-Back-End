package com.moovt



/**
 * TODO: Implement me!
 *
 * @see http://multi-tenant.github.com/grails-multi-tenant-single-db/
 */
class Tenant{

    String name
    
    static constraints = {
        name blank: false, unique: true
    }
    
    Integer tenantId() {
        return this.id
    }

}