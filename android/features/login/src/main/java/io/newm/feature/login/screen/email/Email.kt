package io.newm.feature.login.screen.email

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import io.newm.core.resources.R
import io.newm.core.ui.text.TextFieldWithLabel
import io.newm.core.ui.text.TextFieldWithLabelDefaults
import io.newm.feature.login.screen.TextFieldState

@Composable
fun Email(
    emailState: TextFieldState,
    modifier: Modifier = Modifier,
    label: Int = R.string.email,
    keyboardOptions: KeyboardOptions = TextFieldWithLabelDefaults.KeyboardOptions.EMAIL,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val context = LocalContext.current

    TextFieldWithLabel(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                emailState.onFocusChange(focusState.isFocused)
                if (!focusState.isFocused) {
                    emailState.enableShowErrors()
                }
            },
        onValueChange = { emailState.text = it },
        value = emailState.text,
        labelResId = label,
        isError = emailState.showErrors(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        helperText = emailState.getError(context),
        singleLine = true,
    )
}
