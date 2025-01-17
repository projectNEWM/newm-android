package io.newm.feature.login.screen.welcome

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.recaptcha.RecaptchaAction
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.feature.login.screen.HomeScreen
import io.newm.feature.login.screen.LoginGoogle
import io.newm.feature.login.screen.LoginScreen
import io.newm.feature.login.screen.authproviders.RecaptchaClientProvider
import io.newm.feature.login.screen.authproviders.google.GoogleSignInLauncher
import io.newm.feature.login.screen.createaccount.CreateAccountScreen
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.models.error.KMMException
import io.newm.shared.public.usecases.LoginUseCase

class WelcomeScreenPresenter(
    private val navigator: Navigator,
    private val loginUseCase: LoginUseCase,
    private val googleSignInLauncher: GoogleSignInLauncher,
    private val activityResultContract: ActivityResultContract<Intent, ActivityResult>,
    private val recaptchaClientProvider: RecaptchaClientProvider,
    private val logger: NewmAppLogger,
    private val analyticsTracker: NewmAppEventLogger
) : Presenter<WelcomeScreenUiState> {


    @Composable
    override fun present(): WelcomeScreenUiState {
        val launchGoogleSignIn = rememberGoogleSignInLauncher()
        val context = LocalContext.current
        return WelcomeScreenUiState { event ->
            when (event) {
                WelcomeScreenUiEvent.CreateAccountClicked -> {
                    analyticsTracker.logClickEvent(AppScreens.WelcomeScreen.CREATE_ACCOUNT_BUTTON)
                    navigator.goTo(CreateAccountScreen)
                }

                WelcomeScreenUiEvent.LoginClicked -> {
                    analyticsTracker.logClickEvent(AppScreens.WelcomeScreen.LOGIN_WITH_EMAIL_BUTTON)
                    navigator.goTo(LoginScreen)
                }

                WelcomeScreenUiEvent.OnGoogleSignInClicked -> {
                    analyticsTracker.logClickEvent(AppScreens.WelcomeScreen.LOGIN_WITH_GOOGLE_BUTTON)
                    launchGoogleSignIn()
                }

                WelcomeScreenUiEvent.OnTermsOfServiceClicked -> {
                    analyticsTracker.logClickEvent(AppScreens.AccountScreen.TERMS_AND_CONDITIONS_BUTTON)
                    context.launchUrl("https://newm.io/app-tos")
                }

                WelcomeScreenUiEvent.OnPrivacyPolicyClicked -> {
                    analyticsTracker.logClickEvent(AppScreens.AccountScreen.PRIVACY_POLICY_BUTTON)
                    context.launchUrl("https://newm.io/app-privacy")
                }
            }
        }
    }

    private fun Context.launchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    @Composable
    private fun rememberGoogleSignInLauncher(): () -> Unit {
        var result by remember { mutableStateOf<Task<GoogleSignInAccount>?>(null) }

        LaunchedEffect(result) {
            result?.let { task ->
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    idToken ?: throw IllegalStateException("Google sign in failed. idToken is null")

                    recaptchaClientProvider.get().execute(RecaptchaAction.LoginGoogle)
                        .onSuccess { isHumanProof ->
                            loginUseCase.logInWithGoogle(
                                idToken,
                                humanVerificationCode = isHumanProof
                            )
                            navigator.goTo(HomeScreen)
                        }.onFailure {
                            Log.e("WelcomeScreenPresenter", "Recaptcha failed", it)
                        }
                } catch (e: ApiException) {
                    // The ApiException status code indicates the detailed failure reason.
                    // Please refer to the GoogleSignInStatusCodes class reference for more information.
                    if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED
                        && e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS
                    ) {
                        logger.error(
                            "WelcomeScreenPresenter",
                            "Google sign in failed",
                            e
                        )
                    }
                } catch (kmmException: KMMException) {
                    logger.error(
                        "WelcomeScreenPresenter",
                        "Google sign in failed kmmException: $kmmException",
                        kmmException
                    )
                }
            }
        }

        val activityResultLauncher =
            rememberLauncherForActivityResult(activityResultContract) { activityResult ->
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
                result = task
            }

        return remember { { googleSignInLauncher.launch(activityResultLauncher) } }
    }
}

