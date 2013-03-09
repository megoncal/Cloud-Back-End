package com.moovt.rideshare



import org.junit.*
import grails.test.mixin.*

@TestFor(RideController)
@Mock(Ride)
class RideControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/ride/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.rideInstanceList.size() == 0
        assert model.rideInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.rideInstance != null
    }

    void testSave() {
        controller.save()

        assert model.rideInstance != null
        assert view == '/ride/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/ride/show/1'
        assert controller.flash.message != null
        assert Ride.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/ride/list'

        populateValidParams(params)
        def ride = new Ride(params)

        assert ride.save() != null

        params.id = ride.id

        def model = controller.show()

        assert model.rideInstance == ride
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/ride/list'

        populateValidParams(params)
        def ride = new Ride(params)

        assert ride.save() != null

        params.id = ride.id

        def model = controller.edit()

        assert model.rideInstance == ride
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/ride/list'

        response.reset()

        populateValidParams(params)
        def ride = new Ride(params)

        assert ride.save() != null

        // test invalid parameters in update
        params.id = ride.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/ride/edit"
        assert model.rideInstance != null

        ride.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/ride/show/$ride.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        ride.clearErrors()

        populateValidParams(params)
        params.id = ride.id
        params.version = -1
        controller.update()

        assert view == "/ride/edit"
        assert model.rideInstance != null
        assert model.rideInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/ride/list'

        response.reset()

        populateValidParams(params)
        def ride = new Ride(params)

        assert ride.save() != null
        assert Ride.count() == 1

        params.id = ride.id

        controller.delete()

        assert Ride.count() == 0
        assert Ride.get(ride.id) == null
        assert response.redirectedUrl == '/ride/list'
    }
}
