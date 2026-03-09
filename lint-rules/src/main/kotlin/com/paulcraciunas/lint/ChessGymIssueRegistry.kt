package com.paulcraciunas.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.paulcraciunas.lint.detectors.CoroutineInCompositionDetector
import com.paulcraciunas.lint.detectors.DisallowedImportDetector
import com.paulcraciunas.lint.detectors.DispatcherEnforcementDetector
import com.paulcraciunas.lint.detectors.InternalTestClassDetector
import com.paulcraciunas.lint.detectors.PureDomainDetector
import com.paulcraciunas.lint.detectors.TestNamingDetector
import com.paulcraciunas.lint.detectors.TodoTicketDetector

@Suppress("unused") // Used in build Gradle file
class ChessGymIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(
        TestNamingDetector.ISSUE,
        InternalTestClassDetector.ISSUE,
        TodoTicketDetector.ISSUE,
        PureDomainDetector.ISSUE,
        CoroutineInCompositionDetector.ISSUE,
        DispatcherEnforcementDetector.ISSUE,
        DisallowedImportDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "ChessGym",
        feedbackUrl = "https://github.com/paulcraciunas/ChessGym/issues",
    )
}
