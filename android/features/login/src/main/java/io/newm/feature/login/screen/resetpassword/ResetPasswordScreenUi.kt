package io.newm.feature.login.screen.resetpassword

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.ui.Ui
import io.newm.core.resources.R
import io.newm.core.theme.NewmTheme
import io.newm.core.ui.ToastSideEffect
import io.newm.core.ui.buttons.PrimaryButton
import io.newm.core.ui.text.TextFieldWithLabelDefaults
import io.newm.feature.login.screen.TextFieldState
import io.newm.feature.login.screen.createaccount.EmailVerificationContent
import io.newm.feature.login.screen.email.Email
import io.newm.feature.login.screen.email.EmailState
import io.newm.feature.login.screen.password.Password
import io.newm.feature.login.screen.password.PasswordState
import io.newm.feature.login.screen.resetpassword.ResetPasswordScreenUiState.EnterEmail
import io.newm.feature.login.screen.resetpassword.ResetPasswordScreenUiState.EnterNewPassword
import io.newm.feature.login.screen.resetpassword.ResetPasswordScreenUiState.EnterVerificationCode
import io.newm.feature.login.screen.resetpassword.ResetPasswordUiEvent.EnterEmailUiEvent
import io.newm.feature.login.screen.resetpassword.ResetPasswordUiEvent.EnterNewPasswordUiEvent
import io.newm.feature.login.screen.resetpassword.ResetPasswordUiEvent.EnterVerificationCodeUiEvent
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens

class ResetPasswordScreenUi(val eventLogger: NewmAppEventLogger) : Ui<ResetPasswordScreenUiState> {
    @Composable
    override fun Content(state: ResetPasswordScreenUiState, modifier: Modifier) {
        ResetPasswordScreenContent(modifier = modifier, state = state, eventLogger = eventLogger)
    }
}

@Composable
internal fun ResetPasswordScreenContent(
    state: ResetPasswordScreenUiState,
    modifier: Modifier = Modifier,
    eventLogger: NewmAppEventLogger
) {
    ToastSideEffect(message = state.errorMessage)

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        when (state) {
            is EnterEmail -> {
                EnterEmailContent(state, modifier = Modifier.padding(padding), eventLogger)
            }

            is EnterVerificationCode -> {
                EnterCodeContent(state, modifier = Modifier.padding(padding), eventLogger)
            }

            is EnterNewPassword -> {
                SetNewPasswordContent(state, modifier = Modifier.padding(padding), eventLogger)
            }
        }
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EnterEmailContent(
    state: EnterEmail,
    modifier: Modifier = Modifier,
    eventLogger: NewmAppEventLogger
) {
    val eventSink = state.eventSink
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        eventLogger.logPageLoad(AppScreens.ResetPasswordEnterEmailScreen.name)
    }
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.reset_password_forgot_your_password),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h1
        )
        Text(
            stringResource(R.string.reset_password_enter_email),
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = Modifier.weight(1f))
        Email(
            modifier = Modifier.focusRequester(focusRequester),
            emailState = state.email,
            keyboardOptions = TextFieldWithLabelDefaults.KeyboardOptions.EMAIL.copy(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                keyboardController?.hide()
                if (state.submitButtonEnabled) {
                    eventSink(
                        EnterEmailUiEvent.OnSubmit
                    )
                }
            }),
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.reset_password_enter_email_continue),
            onClick = { eventSink(EnterEmailUiEvent.OnSubmit) },
            enabled = state.submitButtonEnabled,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EnterCodeContent(
    state: EnterVerificationCode,
    modifier: Modifier,
    eventLogger: NewmAppEventLogger
) {
    val eventSink = state.eventSink
    LaunchedEffect(Unit) {
        eventLogger.logPageLoad(AppScreens.ResetPasswordEnterCodeScreen.name)
    }
    EmailVerificationContent(
        modifier = modifier,
        verificationCode = state.code,
        errorMessage = state.errorMessage,
        nextButtonEnabled = state.submitButtonEnabled,
        onNextClicked = { eventSink(EnterVerificationCodeUiEvent.OnSubmit) },
    )
}

@Composable
private fun SetNewPasswordContent(
    state: EnterNewPassword,
    modifier: Modifier = Modifier,
    eventLogger: NewmAppEventLogger
) {
    val onEvent = state.eventSink
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        eventLogger.logPageLoad(AppScreens.NewPasswordScreen.name)
    }

    ToastSideEffect(message = state.errorMessage)

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.reset_password_enter_new_password),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        Password(
            modifier = Modifier.focusRequester(focusRequester),
            label = R.string.reset_password_new_password,
            passwordState = state.password,
            keyboardOptions = TextFieldWithLabelDefaults.KeyboardOptions.PASSWORD.copy(imeAction = ImeAction.Next),
        )

        Password(
            label = R.string.reset_password_confirm_new_password,
            passwordState = state.confirmPasswordState,
            keyboardOptions = TextFieldWithLabelDefaults.KeyboardOptions.PASSWORD.copy(
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    keyboardController?.hide()
                    if (state.submitButtonEnabled) {
                        onEvent(EnterNewPasswordUiEvent.OnSubmit)
                    }
                }
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.confirm),
            onClick = {
                onEvent(EnterNewPasswordUiEvent.OnSubmit)
            },
            enabled = state.submitButtonEnabled
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
private fun PreviewEnterEmail() {
    NewmTheme {
        ResetPasswordScreenContent(
            EnterEmail(
                email = EmailState(),
                errorMessage = null,
                isLoading = false,
                submitButtonEnabled = true,
                eventSink = {},
            ),
            eventLogger = NewmAppEventLogger()
        )
    }
}

@Composable
@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
private fun PreviewVerificationCode() {
    NewmTheme {
        ResetPasswordScreenContent(
            EnterVerificationCode(
                code = TextFieldState(),
                errorMessage = null,
                isLoading = false,
                submitButtonEnabled = true,
                eventSink = {},
            ),
            eventLogger = NewmAppEventLogger()
        )
    }
}

@Composable
@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
private fun PreviewSetNewPassword() {
    NewmTheme {
        ResetPasswordScreenContent(
            EnterNewPassword(
                password = PasswordState(),
                confirmPasswordState = PasswordState(),
                errorMessage = null,
                isLoading = false,
                submitButtonEnabled = true,
                eventSink = {},
            ),
            eventLogger = NewmAppEventLogger()
        )
    }
}

@Composable
@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
private fun PreviewLoading() {
    NewmTheme {
        ResetPasswordScreenContent(
            EnterNewPassword(
                password = PasswordState(),
                confirmPasswordState = PasswordState(),
                errorMessage = null,
                isLoading = true,
                submitButtonEnabled = true,
                eventSink = {},
            ),
            eventLogger = NewmAppEventLogger()
        )
    }
}

