package com.sweak.qralarm.core.domain.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ShouldShowWhatsNewTest {

    private val userDataRepository = mockk<UserDataRepository>(relaxUnitFun = true)
    private val shouldShowWhatsNew = ShouldShowWhatsNew(userDataRepository)

    enum class Case(
        val isIntroductionFinished: Boolean,
        val lastShownVersionCode: Int?,
        val expected: Boolean
    ) {
        INTRODUCTION_NOT_FINISHED_NEVER_SHOWN(false, null, false),
        INTRODUCTION_NOT_FINISHED_OLDER_VERSION_SHOWN(false, WHATS_NEW_VERSION_CODE - 1, false),
        INTRODUCTION_FINISHED_NEVER_SHOWN(true, null, true),
        INTRODUCTION_FINISHED_OLDER_VERSION_SHOWN(true, WHATS_NEW_VERSION_CODE - 1, true),
        INTRODUCTION_FINISHED_CURRENT_VERSION_SHOWN(true, WHATS_NEW_VERSION_CODE, false)
    }

    @Test
    fun `given introduction state and last shown version, when checking, then What's new is shown only for unseen content after the introduction`(
        @TestParameter case: Case
    ) = runTest {
        // Given
        every { userDataRepository.isIntroductionFinished } returns
                flowOf(case.isIntroductionFinished)
        every { userDataRepository.whatsNewLastShownVersionCode } returns
                flowOf(case.lastShownVersionCode)

        // When
        val result = shouldShowWhatsNew()

        // Then
        assertThat(result).isEqualTo(case.expected)
    }
}
