package com.github.codespeaker.betterstatusbar.services

import com.intellij.openapi.project.Project
import com.github.codespeaker.betterstatusbar.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
