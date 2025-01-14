package io.newm.feature.login.screen.resetpassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.recaptcha.RecaptchaAction
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.core.resources.R
import io.newm.feature.login.screen.HomeScreen
import io.newm.feature.login.screen.authproviders.RecaptchaClientProvider
import io.newm.feature.login.screen.email.EmailState
import io.newm.feature.login.screen.password.ConfirmPasswordState
import io.newm.feature.login.screen.password.PasswordState
import io.newm.feature.login.screen.password.VerificationCodeState
import io.newm.feature.login.screen.resetpassword.ResetPasswordUiEvent.EnterEmailUiEvent
import io.newm.feature.login.screen.resetpassword.ResetPasswordUiEvent.EnterNewPasswordUiEvent
import io.newm.feature.login.screen.resetpassword.ResetPasswordUiEvent.EnterVerificationCodeUiEvent
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.usecases.LoginUseCase
import io.newm.shared.public.usecases.ResetPasswordUseCase
import io.newm.shared.public.usecases.SignupUseCase
import kotlinx.coroutines.launch

private enum class ResetPasswordStep {
    EnterEmail,
    EnterVerificationCode,
    EnterNewPassword,
}

class ResetPasswordScreenPresenter(
    private val navigator: Navigator,
    private val signupUseCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val recaptchaClientProvider: RecaptchaClientProvider,
    private val logger: NewmAppLogger,
    private val analyticsTracker: NewmAppEventLogger
) : Presenter<ResetPasswordScreenUiState> {
    @Composable
    override fun present(): ResetPasswordScreenUiState {
        var step by remember { mutableStateOf(ResetPasswordStep.EnterEmail) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val email = rememberRetained { EmailState() }
        val authCode = rememberRetained { VerificationCodeState() }
        val password = remember { PasswordState() }
        val passwordConfirmation = remember(password) { ConfirmPasswordState(password) }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        return when (step) {
            ResetPasswordStep.EnterEmail -> {
                ResetPasswordScreenUiState.EnterEmail(
                    email = email,
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    submitButtonEnabled = email.isValid && !isLoading,
                    eventSink = { event ->
                        when (event) {
                            EnterEmailUiEvent.OnSubmit -> {
                                analyticsTracker.logClickEvent(AppScreens.ResetPasswordEnterEmailScreen.CONTINUE_BUTTON)
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        recaptchaClientProvider.get()
                                            .execute(RecaptchaAction.custom("auth_code"))
                                            .onSuccess { token ->
                                                signupUseCase.requestEmailConfirmationCode(
                                                    email.text,
                                                    humanVerificationCode = token,
                                                    mustExists = true
                                                )
                                                step = ResetPasswordStep.EnterVerificationCode
                                            }.onFailure {
                                                logger.error(
                                                    "ResetPasswordScreenPresenter",
                                                    "Human verification error",
                                                    it
                                                )
                                            }
                                    } catch (e: Throwable) {
                                        logger.error(
                                            "ResetPasswordScreenPresenter",
                                            "Requesting email confirmation code failed",
                                            e
                                        )
                                        errorMessage = e.message
                                    }

                                    isLoading = false
                                }
                            }
                        }
                    },
                )
            }

            ResetPasswordStep.EnterVerificationCode -> {
                ResetPasswordScreenUiState.EnterVerificationCode(
                    code = authCode,
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    submitButtonEnabled = authCode.isValid && !isLoading,
                    eventSink = { event ->
                        when (event) {
                            EnterVerificationCodeUiEvent.OnSubmit -> {
                                analyticsTracker.logClickEvent(AppScreens.ResetPasswordEnterCodeScreen.CONTINUE_BUTTON)
                                step = ResetPasswordStep.EnterNewPassword
                            }
                        }
                    },
                )
            }

            ResetPasswordStep.EnterNewPassword -> {
                val submitEnabled = password.isValid && passwordConfirmation.isValid && !isLoading

                ResetPasswordScreenUiState.EnterNewPassword(
                    password = password,
                    confirmPasswordState = passwordConfirmation,
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    submitButtonEnabled = submitEnabled,
                    eventSink = { event ->
                        when (event) {
                            EnterNewPasswordUiEvent.OnSubmit -> {
                                analyticsTracker.logClickEvent(AppScreens.NewPasswordScreen.CONFIRM_BUTTON)
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        recaptchaClientProvider.get()
                                            .execute(RecaptchaAction.custom("password_reset"))
                                            .onSuccess { token ->
                                                resetPasswordUseCase.resetPassword(
                                                    email = email.text,
                                                    code = authCode.text,
                                                    newPassword = password.text,
                                                    confirmPassword = passwordConfirmation.text,
                                                    humanVerificationCode = token
                                                )
                                                errorMessage = context.getString(R.string.password_reset_successfully_message)
                                                recaptchaClientProvider.get()
                                                    .execute(RecaptchaAction.LOGIN)
                                                    .onSuccess { newToken ->
                                                        loginUseCase.logIn(
                                                            email.text,
                                                            password.text,
                                                            humanVerificationCode = newToken
                                                        )
                                                        navigator.goTo(HomeScreen)
                                                    }
                                            }.onFailure {
                                                logger.error(
                                                    "ResetPasswordScreenPresenter",
                                                    "Human verification error",
                                                    it
                                                )
                                            }
                                    } catch (e: Throwable) {
                                        logger.error(
                                            "ResetPasswordScreenPresenter",
                                            "Resetting password failed",
                                            e
                                        )
                                        errorMessage = e.message
                                    }

                                    isLoading = false
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}
