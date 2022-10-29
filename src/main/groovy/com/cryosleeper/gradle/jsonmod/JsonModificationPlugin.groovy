package com.cryosleeper.gradle.jsonmod

import org.gradle.api.Plugin
import org.gradle.api.Project

class JsonModificationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("modifyJsons", JsonModificationExtension)

        project.tasks.register("modifyJsons", JsonModificationTask) {
            isDeleting = project.modifyJsons.allowDelete
            modifications = project.modifyJsons.modifications
        }
    }
}
