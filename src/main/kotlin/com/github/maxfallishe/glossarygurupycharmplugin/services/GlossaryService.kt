package com.github.maxfallishe.glossarygurupycharmplugin

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBusConnection
import java.io.File
import java.io.FileNotFoundException


@Service(Service.Level.PROJECT)
class GlossaryService(private val project: Project) {

    private var glossary: Map<String, String> = loadGlossary()
    private var connection: MessageBusConnection? = null

    init {
        setupFileListener()
        setupDocumentListener()
    }

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

    private fun setupFileListener() {
        val projectPath = ProjectRootManager.getInstance(project).contentRoots.firstOrNull()?.path
            ?: return

        val glossaryFile = File("$projectPath/glossary.md").absolutePath

        connection = project.messageBus.connect()
        connection?.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                for (event in events) {
                    val filePath = event.file?.path
                    if (filePath != null && filePath == glossaryFile) {
                        glossary = loadGlossary()
                        println("Glossary updated. Documentation should be rebuilt.")
                    }
                }
            }
        })
    }

    private fun setupDocumentListener() {
        val projectPath = ProjectRootManager.getInstance(project).contentRoots.firstOrNull()?.path
            ?: return

        val glossaryFile = File("$projectPath/glossary.md")
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://${glossaryFile.absolutePath}")

        virtualFile?.let {
            val document = FileDocumentManager.getInstance().getDocument(it)
            document?.addDocumentListener(object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    glossary = parseGlossary(event.document.text)
                    println("Glossary updated from document change. Documentation should be rebuilt.")
                }
            })
        }
    }

    fun dispose() {
        connection?.disconnect()
    }
}
