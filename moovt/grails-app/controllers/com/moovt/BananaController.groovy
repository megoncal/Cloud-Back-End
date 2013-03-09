package com.moovt

import org.springframework.dao.DataIntegrityViolationException

class BananaController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [bananaInstanceList: Banana.list(params), bananaInstanceTotal: Banana.count()]
    }

    def create() {
        [bananaInstance: new Banana(params)]
    }

    def save() {
        def bananaInstance = new Banana(params)
        if (!bananaInstance.save(flush: true)) {
            render(view: "create", model: [bananaInstance: bananaInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'banana.label', default: 'Banana'), bananaInstance.id])
        redirect(action: "show", id: bananaInstance.id)
    }

    def show(Long id) {
        def bananaInstance = Banana.get(id)
        if (!bananaInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'banana.label', default: 'Banana'), id])
            redirect(action: "list")
            return
        }

        [bananaInstance: bananaInstance]
    }

    def edit(Long id) {
        def bananaInstance = Banana.get(id)
        if (!bananaInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'banana.label', default: 'Banana'), id])
            redirect(action: "list")
            return
        }

        [bananaInstance: bananaInstance]
    }

    def update(Long id, Long version) {
        def bananaInstance = Banana.get(id)
        if (!bananaInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'banana.label', default: 'Banana'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (bananaInstance.version > version) {
                bananaInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'banana.label', default: 'Banana')] as Object[],
                          "Another user has updated this Banana while you were editing")
                render(view: "edit", model: [bananaInstance: bananaInstance])
                return
            }
        }

        bananaInstance.properties = params

        if (!bananaInstance.save(flush: true)) {
            render(view: "edit", model: [bananaInstance: bananaInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'banana.label', default: 'Banana'), bananaInstance.id])
        redirect(action: "show", id: bananaInstance.id)
    }

    def delete(Long id) {
        def bananaInstance = Banana.get(id)
        if (!bananaInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'banana.label', default: 'Banana'), id])
            redirect(action: "list")
            return
        }

        try {
            bananaInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'banana.label', default: 'Banana'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'banana.label', default: 'Banana'), id])
            redirect(action: "show", id: id)
        }
    }
}
