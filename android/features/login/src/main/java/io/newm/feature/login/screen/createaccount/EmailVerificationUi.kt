package io.newm.feature.login.screen.createaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.newm.core.resources.R
import io.newm.core.ui.ToastSideEffect
import io.newm.core.ui.buttons.PrimaryButton
import io.newm.core.ui.text.TextFieldWithLabel
import io.newm.core.ui.text.TextFieldWithLabelDefaults
import io.newm.feature.login.screen.TextFieldState

@Composable
internal fun EmailVerificationUi(
    modifier: Modifier = Modifier,
    state: CreateAccountUiState.EmailVerificationUiState,
) {
    val onEvent = state.eventSink

    EmailVerificationContent(
        modifier = modifier,
        verificationCode = state.verificationCode,
        errorMessage = state.errorMessage,
        nextButtonEnabled = state.nextButtonEnabled,
        onNextClicked = { onEvent(EmailVerificationUiEvent.Next) },
    )
}

@Composable
internal fun EmailVerificationContent(
    verificationCode: TextFieldState,
    errorMessage: String?,
    nextButtonEnabled: Boolean,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToastSideEffect(errorMessage)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(MaterialTheme.colors.background),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.login_check_your_email),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h1
            )
            Text(
                text = stringResource(id = R.string.login_enter_verification_code_below),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h1,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = stringResource(id = R.string.login_receive_email),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.caption
            )
            Spacer(modifier = Modifier.height(36.dp))

            TextFieldWithLabel(
                labelResId = R.string.login_enter_verification_code,
                value = verificationCode.text,
                onValueChange = verificationCode::text::set,
                keyboardOptions = TextFieldWithLabelDefaults.KeyboardOptions.Digits.copy(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (nextButtonEnabled) {
                            onNextClicked()
                        }
                    }
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(id = R.string.login_continue),
                enabled = nextButtonEnabled,
                onClick = {
                    onNextClicked()
                }
            )
        }
    }
}
