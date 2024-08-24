package com.github.maxfallishe.glossarygurupycharmplugin

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.jetbrains.python.documentation.PythonDocumentationProvider
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class SimpleDocumentationProvider : PythonDocumentationProvider() {

    private fun getProjectGlossary(project: Project): Map<String, String> {
        val glossaryService = project.getService(GlossaryService::class.java)
        return glossaryService?.getGlossary() ?: emptyMap()
    }

    private fun findGlossaryWordsInName(name: String, glossary: Map<String, String>): List<String> {
        val matchedWords = mutableListOf<String>()
        for (term in glossary.keys) {
            if (name.contains(term, ignoreCase = true)) {
                matchedWords.add(term)
            }
        }
        return matchedWords
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        val project = element.project
        val glossary = getProjectGlossary(project)

        val originalDoc = super.generateDoc(element, originalElement)
        if (element is PsiNamedElement) {
            val elementName = element.name ?: return "No documentation available."
            val matchedGlossaryWords = findGlossaryWordsInName(elementName, glossary)

            val glossaryDocs = matchedGlossaryWords.mapNotNull { word ->
                glossary[word]?.let { "$word — $it" }
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
