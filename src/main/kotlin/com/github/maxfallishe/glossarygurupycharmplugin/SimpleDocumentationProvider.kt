package com.github.maxfallishe.glossarygurupycharmplugin

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Nullable
import org.jetbrains.annotations.NotNull
import com.jetbrains.python.documentation.PythonDocumentationProvider

class SimpleDocumentationProvider : DocumentationProvider {
    init {
        thisLogger().warn("INIT DP")
    }
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String {
        thisLogger().warn("getQuickNavigateInfo: $element")
        return "Quick navigate info: ${element.toString()}"
    }

    override fun generateHoverDoc(@NotNull element: PsiElement, @Nullable originalElement: PsiElement?): String {
        thisLogger().warn("generateHoverDoc called for element: $element")
        return "Hover doc: ${element.toString()}"
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String {
        thisLogger().warn("generateDoc called for element: $element")
        return "Doc: ${element.toString()}"
    }
}
