package com.github.maxfallishe.glossarygurupycharmplugin

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.jetbrains.python.documentation.PythonDocumentationProvider
import org.jetbrains.annotations.Nullable
import org.jetbrains.annotations.NotNull
import java.io.File

class SimpleDocumentationProvider : PythonDocumentationProvider() {
    // Map to hold the glossary terms and their definitions
    private val glossary: Map<String, String>

    init {
        glossary = loadGlossary()
        thisLogger().warn("INIT DP")
    }

    // Function to load the glossary from the glossary.md file
    private fun loadGlossary(): Map<String, String> {
        val glossaryFile = File("/home/nick/PycharmProjects/testPluginv3/glossary.md")
        val glossaryContent = glossaryFile.readText()

        return parseGlossary(glossaryContent)
    }

    private fun parseGlossary(content: String): Map<String, String> {
        val glossaryMap = mutableMapOf<String, String>()

        // Split the content based on '## ' which indicates the start of a new term definition
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

    // Function to get all glossary words found in a term
    private fun findGlossaryWordsInName(name: String): List<String> {
        val matchedWords = mutableListOf<String>()

        for (term in glossary.keys) {
            if (name.contains(term, ignoreCase = true)) {
                matchedWords.add(term)
            }
        }

        return matchedWords
    }

    fun getDocumentationFor(term: String): String? {
        return  term + " — " + glossary[term]
    }


    override fun generateHoverDoc(@NotNull element: PsiElement, @Nullable originalElement: PsiElement?): String {
        thisLogger().warn("generateHoverDoc called for element: $element")
        return "Hover doc: ${element.toString()}"
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        // Call the original method to get the default documentation
        val originalDoc = super.generateDoc(element, originalElement)


        if (element is PsiNamedElement) {
            val elementName = element.name ?: return "No documentation available."
            val matchedGlossaryWords = findGlossaryWordsInName(elementName)

            val glossaryDocs = matchedGlossaryWords.mapNotNull { word ->
                getDocumentationFor(word)
            }

            thisLogger().warn("generateDoc called for element: $element || ${element.name}")
            return if (glossaryDocs.isNotEmpty()) {
                originalDoc + glossaryDocs.joinToString(separator = "\n\n") { doc -> "<p><b>$doc</b></p>" }
            } else {
                "$originalDoc<p>No glossary matches found in the name.<p>"
            }
        }
        return originalDoc
    }
}
