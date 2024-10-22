package io.newm

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.featureflags.FeatureFlagManager
import io.newm.shared.public.usecases.UserDetailsUseCase
import io.newm.shared.public.usecases.UserSessionUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.seconds

class AppLaunchGhostActivity : ComponentActivity() {
    private val tag = "AppLaunchGhostActivity"
    private val userSession: UserSessionUseCase by inject()
    private val userDetailsUseCase: UserDetailsUseCase by inject()
    private val featureFlagMager: FeatureFlagManager by inject()
    private val logger: NewmAppLogger by inject()

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { true }
        }

        if (userSession.isLoggedIn()) {
            lifecycleScope.launch {
                try {
                    userDetailsUseCase.fetchLoggedInUserDetailsFlow()
                        .filterNotNull()
                        .onEach(featureFlagMager::setUser)
                        .timeout(3.seconds)
                        .first()
                } catch (e: Exception) {
                    logger.error(
                        tag,
                        "Failed to identify feature flag because fetching user details failed.",
                        e
                    )
                } finally {
                    launchHomeActivity()
                }
            }
        } else {
            launchLoginActivity()
        }
    }

    private fun launchHomeActivity() {
        startActivity(Intent(this@AppLaunchGhostActivity, HomeActivity::class.java))
        finish()
    }

    private fun launchLoginActivity() {
        startActivity(Intent(this@AppLaunchGhostActivity, LoginActivity::class.java))
        finish()
    }
}
