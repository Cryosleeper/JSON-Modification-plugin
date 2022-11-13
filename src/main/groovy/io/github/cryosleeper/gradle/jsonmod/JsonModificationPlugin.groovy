package io.github.cryosleeper.gradle.jsonmod

import org.gradle.api.Plugin
import org.gradle.api.Project

class JsonModificationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("modifyJsons", JsonModificationExtension)

        project.tasks.register("modifyJsons", JsonModificationTask) {
            setGroup("json modification")
            isDeleting = project.modifyJsons.allowDelete
            isAdding = project.modifyJsons.allowAdd
            modifications = project.modifyJsons.modifications
        }

        project.tasks.register("modifySingleJson", SingleJsonModificationTask) {
            setGroup("json modification")
            //TODO
        }
    }
}
