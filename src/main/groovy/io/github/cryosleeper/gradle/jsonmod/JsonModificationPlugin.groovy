package io.github.cryosleeper.gradle.jsonmod

import org.gradle.api.Plugin
import org.gradle.api.Project

class JsonModificationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("jsonsToModify", JsonModificationExtension)

        project.tasks.register("modifyJsons", JsonModificationTask) {
            setGroup("json modification")
            it.isDeleting = project.jsonsToModify.allowDelete
            it.isAdding = project.jsonsToModify.allowAdd
            modifications = project.jsonsToModify.modifications
        }

        project.tasks.register("modifySingleJson", SingleJsonModificationTask) {
            setGroup("json modification")
            //TODO
        }
    }
}
