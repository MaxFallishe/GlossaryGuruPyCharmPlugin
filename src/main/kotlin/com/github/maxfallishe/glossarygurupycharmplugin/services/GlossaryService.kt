package com.github.maxfallishe.glossarygurupycharmplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import java.io.File
import java.io.FileNotFoundException


@Service(Service.Level.PROJECT)
class GlossaryService(private val project: Project) {

    private var glossary: Map<String, String> = loadGlossary()

    private fun loadGlossary(): Map<String, String> {
        val projectPath = ProjectRootManager.getInstance(project).contentRoots.firstOrNull()?.path
            ?: throw FileNotFoundException("Project root not found")

        val glossaryFile = File("$projectPath/glossary.md")
        if (!glossaryFile.exists()) {
            throw FileNotFoundException("glossary.md not found in project root")
        }

        val glossaryContent = glossaryFile.readText()
        return parseGlossary(glossaryContent)
    }

    private fun parseGlossary(content: String): Map<String, String> {
        val glossaryMap = mutableMapOf<String, String>()

        val entries = content.split("## ")
            .filter { it.isNotBlank() }
            .map { it.trim() }

        for (entry in entries) {
            val lines = entry.split("\n")
            if (lines.size > 1) {
                val term = lines[0].trim()
                val definition = lines.drop(1).joinToString(" ").trim()
                glossaryMap[term] = definition
            }
        }

        return glossaryMap
    }

    fun getGlossary(): Map<String, String> = glossary
}
