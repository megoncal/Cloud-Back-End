package com.moovt



import org.junit.*
import grails.test.mixin.*

@TestFor(BananaController)
@Mock(Banana)
class BananaControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/banana/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.bananaInstanceList.size() == 0
        assert model.bananaInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.bananaInstance != null
    }

    void testSave() {
        controller.save()

        assert model.bananaInstance != null
        assert view == '/banana/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/banana/show/1'
        assert controller.flash.message != null
        assert Banana.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/banana/list'

        populateValidParams(params)
        def banana = new Banana(params)

        assert banana.save() != null

        params.id = banana.id

        def model = controller.show()

        assert model.bananaInstance == banana
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/banana/list'

        populateValidParams(params)
        def banana = new Banana(params)

        assert banana.save() != null

        params.id = banana.id

        def model = controller.edit()

        assert model.bananaInstance == banana
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/banana/list'

        response.reset()

        populateValidParams(params)
        def banana = new Banana(params)

        assert banana.save() != null

        // test invalid parameters in update
        params.id = banana.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/banana/edit"
        assert model.bananaInstance != null

        banana.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/banana/show/$banana.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        banana.clearErrors()

        populateValidParams(params)
        params.id = banana.id
        params.version = -1
        controller.update()

        assert view == "/banana/edit"
        assert model.bananaInstance != null
        assert model.bananaInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/banana/list'

        response.reset()

        populateValidParams(params)
        def banana = new Banana(params)

        assert banana.save() != null
        assert Banana.count() == 1

        params.id = banana.id

        controller.delete()

        assert Banana.count() == 0
        assert Banana.get(banana.id) == null
        assert response.redirectedUrl == '/banana/list'
    }
}
