package io.newm.feature.login.screen.createaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.recaptcha.RecaptchaAction
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.core.resources.R
import io.newm.feature.login.screen.authproviders.RecaptchaClientProvider
import io.newm.feature.login.screen.createaccount.CreateAccountUiState.EmailAndPasswordUiState
import io.newm.feature.login.screen.createaccount.CreateAccountUiState.EmailVerificationUiState
import io.newm.feature.login.screen.email.EmailState
import io.newm.feature.login.screen.password.ConfirmPasswordState
import io.newm.feature.login.screen.password.PasswordState
import io.newm.feature.login.screen.password.VerificationCodeState
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.usecases.LoginUseCase
import io.newm.shared.public.usecases.SignupUseCase
import kotlinx.coroutines.launch

class CreateAccountScreenPresenter(
    private val navigateHome: () -> Unit,
    private val signupUseCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
    private val recaptchaClientProvider: RecaptchaClientProvider,
    private val appLogger: NewmAppLogger,
    private val eventLogger: NewmAppEventLogger
) : Presenter<CreateAccountUiState> {

    @Composable
    override fun present(): CreateAccountUiState {
        var step by rememberRetained { mutableStateOf(Step.EmailAndPassword) }
        val userEmail = rememberRetained { EmailState() }
        val password = rememberRetained { PasswordState() }
        val passwordConfirmation = rememberRetained { ConfirmPasswordState(password) }
        val verificationCode = rememberRetained { VerificationCodeState() }

        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        val emailAndPasswordValid =
            remember(userEmail.isValid, password.isValid, passwordConfirmation.isValid) {
                userEmail.isValid && password.isValid && passwordConfirmation.isValid
            }

        var errorMessage by remember { mutableStateOf<String?>(null) }

        return when (step) {
            Step.EmailAndPassword -> {
                EmailAndPasswordUiState(
                    emailState = userEmail,
                    passwordState = password,
                    submitButtonEnabled = emailAndPasswordValid,
                    passwordConfirmationState = passwordConfirmation,
                    errorMessage = errorMessage,
                ) { event ->
                    when (event) {
                        SignupFormUiEvent.Next -> {
                            eventLogger.logClickEvent(AppScreens.CreateAccountScreen.NEXT_BUTTON)
                            require(emailAndPasswordValid) {
                                "Email and password - next button should not be enabled if any of the fields are invalid"
                            }

                            coroutineScope.launch {
                                step = Step.Loading
                                step = try {
                                    recaptchaClientProvider.get()
                                        .execute(RecaptchaAction.custom("auth_code"))
                                        .onSuccess { token ->
                                            signupUseCase.requestEmailConfirmationCode(
                                                email = userEmail.text,
                                                humanVerificationCode = token
                                            )
                                        }.onFailure {
                                            errorMessage =
                                                context.getString(R.string.invalid_recaptcha_message)
                                        }
                                    Step.EmailVerification
                                } catch (e: Throwable) {
                                    appLogger.error(
                                        tag = "Sign up",
                                        message = "${e.message}",
                                        exception = e
                                    )
                                    errorMessage = e.message
                                    Step.EmailAndPassword
                                }
                            }
                        }
                    }
                }
            }

            Step.EmailVerification -> {
                eventLogger.logPageLoad(AppScreens.EmailVerificationScreen.name)
                EmailVerificationUiState(
                    verificationCode = verificationCode,
                    nextButtonEnabled = verificationCode.isValid,
                    errorMessage = errorMessage,
                ) { event ->
                    when (event) {
                        is EmailVerificationUiEvent.Next -> {
                            require(emailAndPasswordValid && verificationCode.isValid) {
                                "Email verification - next button should not be enabled if any of the fields are invalid"
                            }
                            eventLogger.logClickEvent(AppScreens.EmailVerificationScreen.CONTINUE_BUTTON)

                            coroutineScope.launch {
                                step = Step.Loading
                                try {
                                    recaptchaClientProvider.get()
                                        .execute(RecaptchaAction.SIGNUP)
                                        .onSuccess { token ->
                                            signupUseCase.registerUser(
                                                email = userEmail.text,
                                                verificationCode = verificationCode.text,
                                                password = password.text,
                                                passwordConfirmation = passwordConfirmation.text,
                                                humanVerificationCode = token,
                                            )
                                            recaptchaClientProvider.get()
                                                .execute(RecaptchaAction.LOGIN)
                                                .onSuccess { newToken ->
                                                    loginUseCase.logIn(
                                                        userEmail.text,
                                                        password.text,
                                                        humanVerificationCode = newToken
                                                    )
                                                    navigateHome()
                                                }
                                        }.onFailure {
                                            errorMessage =
                                                context.getString(R.string.invalid_recaptcha_message)
                                        }
                                } catch (e: Throwable) {
                                    appLogger.error(
                                        tag = "Create Account",
                                        message = "Email verification: ${e.message}",
                                        exception = e
                                    )
                                    errorMessage = e.message
                                    step = Step.EmailVerification
                                }
                            }
                        }
                    }
                }
            }

            Step.Loading -> {
                eventLogger.logPageLoad(AppScreens.LoadingScreen.name)
                CreateAccountUiState.Loading
            }
        }
    }
}

private enum class Step {
    Loading,
    EmailAndPassword,
    EmailVerification,
}
