package io.projectnewm

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.karumi.shot.ScreenshotTest
import io.projectnewm.feature.login.screen.LoginScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest : ScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent {
            LoginScreen(onSignInSubmitted = {}, onSignupClick = {})
        }
    }

    @Test
    fun captureLoginScreen() {
        compareScreenshot(composeTestRule)
    }
}