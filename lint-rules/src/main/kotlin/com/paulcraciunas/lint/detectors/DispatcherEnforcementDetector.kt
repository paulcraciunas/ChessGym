package com.paulcraciunas.lint.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMember
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.getContainingUClass

internal class DispatcherEnforcementDetector : Detector(), SourceCodeScanner {
    override fun getApplicableReferenceNames(): List<String> =
        DISPATCHER_PROPERTIES

    override fun visitReference(
        context: JavaContext,
        reference: UReferenceExpression,
        referenced: PsiElement,
    ) {
        if (isTestSource(context)) return
        val containingClass = (referenced as? PsiMember)?.containingClass ?: return
        if (containingClass.qualifiedName != DISPATCHERS_CLASS) return
        if (isInsideDaggerModule(reference)) return
        val reportNode = reference.uastParent ?: reference
        context.report(ISSUE, reportNode, context.getLocation(reportNode), MESSAGE)
    }

    companion object {
        private const val MESSAGE =
            "Do not use hardcoded Dispatchers. " +
                "Inject a CoroutineDispatcher or DispatcherProvider instead for testability."

        private const val DAGGER_MODULE_ANNOTATION = "dagger.Module"
        private const val DISPATCHERS_CLASS = "kotlinx.coroutines.Dispatchers"

        private val DISPATCHER_PROPERTIES = listOf("IO", "Default", "Main", "Unconfined")

        private val SEP = java.io.File.separatorChar
        private val TEST_SOURCE_INDICATORS = listOf(
            "${SEP}src${SEP}test${SEP}",
            "${SEP}src${SEP}androidTest${SEP}",
            "${SEP}src${SEP}testFixtures${SEP}",
        )

        val ISSUE: Issue = Issue.create(
            id = "HardcodedDispatcher",
            briefDescription = "Hardcoded coroutine Dispatcher",
            explanation = "Using `Dispatchers.IO`, `Dispatchers.Default`, or `Dispatchers.Main` " +
                "directly makes code hard to test. Inject dispatchers through constructor " +
                "parameters or a DispatcherProvider interface instead. " +
                "Dagger @Module classes are exempt since they provide the bindings. " +
                "Test sources are also exempt since dispatchers are often hardcoded there.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                DispatcherEnforcementDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private fun isInsideDaggerModule(node: UReferenceExpression): Boolean {
            val containingClass = node.getContainingUClass() ?: return false
            return isDaggerModule(containingClass)
        }

        private fun isDaggerModule(clazz: UClass): Boolean =
            clazz.uAnnotations.any { it.qualifiedName == DAGGER_MODULE_ANNOTATION }

        private fun isTestSource(context: JavaContext): Boolean {
            val filePath = context.file.path
            return TEST_SOURCE_INDICATORS.any { filePath.contains(it) }
        }
    }
}
