package com.github.maxfallishe.glossarygurupycharmplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class MyProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        val projectPath = project.basePath
        if (projectPath != null) {
            println("Project Path: $projectPath")
        } else {
            println("Could not retrieve the project path.")
        }
    }

    override fun projectClosed(project: Project) {
        // Handle project close if needed
    }
}