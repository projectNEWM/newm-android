package io.newm.feature.login.screen

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.newm.core.test.utils.SnapshotTest
import io.newm.core.test.utils.SnapshotTestConfiguration
import io.newm.feature.login.screen.createaccount.CreateAccountViewModel
import io.newm.feature.login.screen.createaccount.EnterVerificationScreenContent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class EnterVerificationScreenContentTest(
    @TestParameter private val testConfiguration: SnapshotTestConfiguration,
) : SnapshotTest(testConfiguration) {
    @Test
    fun default() {
        snapshot {
            EnterVerificationScreenContent(
                state = CreateAccountViewModel.SignupUserState(),
                onVerificationComplete = {},
                setEmailVerificationCode = {},
                verifyAccount = {}
            )
        }
    }
}
