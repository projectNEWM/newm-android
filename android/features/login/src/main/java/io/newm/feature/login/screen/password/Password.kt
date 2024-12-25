package io.newm.feature.login.screen.password

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import io.newm.core.ui.text.TextFieldWithLabel
import io.newm.core.ui.text.TextFieldWithLabelDefaults
import io.newm.feature.login.screen.TextFieldState

@Composable
fun Password(
    @StringRes label: Int,
    passwordState: TextFieldState,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = TextFieldWithLabelDefaults.KeyboardOptions.PASSWORD,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val context = LocalContext.current

    TextFieldWithLabel(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                passwordState.onFocusChange(focusState.isFocused)
                if (!focusState.isFocused) {
                    passwordState.enableShowErrors()
                }
            },
        onValueChange = { passwordState.text = it },
        value = passwordState.text,
        isPassword = true,
        labelResId = label,
        isError = passwordState.showErrors(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        helperText = passwordState.getError(context),
        singleLine = true,
    )
}
