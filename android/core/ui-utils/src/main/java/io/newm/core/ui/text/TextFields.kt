package io.newm.core.ui.text

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.newm.core.resources.R
import io.newm.core.theme.Gray100
import io.newm.core.theme.Gray500
import io.newm.core.theme.inter

val formTitleStyle = TextStyle(
    fontSize = 12.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Bold,
    color = White
)

val formLabelStyle = TextStyle(
    fontSize = 12.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Bold,
    color = Gray100
)

val formTextFieldStyle = TextStyle(
    fontSize = 16.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Normal,
)

val formNameStyle = TextStyle(
    fontSize = 24.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Bold,
    color = White
)

val formEmailStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Normal,
    color = Gray100
)

val versionTextStyle = TextStyle(
    fontSize = 12.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Light,
    color = Gray100
)

object TextFieldWithLabelDefaults {
    object KeyboardOptions {
        @Stable
        val PASSWORD = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            autoCorrect = false,
        )

        @Stable
        val EMAIL = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Email,
        )

        @Stable
        val NON_UNDERLINED = KeyboardOptions(keyboardType = KeyboardType.Password)

        @Stable
        val Digits =
            KeyboardOptions(keyboardType = KeyboardType.NumberPassword, autoCorrect = false)
    }
}

@Composable
fun TextFieldWithLabel(
    modifier: Modifier = Modifier,
    labelResId: Int? = null,
    labelStyle: TextStyle = formLabelStyle,
    value: String? = null,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    placeholderResId: Int? = null,
    placeholderStyle: TextStyle = LocalTextStyle.current,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = if (isPassword) TextFieldWithLabelDefaults.KeyboardOptions.PASSWORD else TextFieldWithLabelDefaults.KeyboardOptions.NON_UNDERLINED,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    helperText: String? = null,
    textfieldBackgroundColor: Color = MaterialTheme.colors.surface,
    singleLine: Boolean = false,
) {
    val isInputMasked = remember { mutableStateOf(isPassword) }
    Column(
        modifier
    ) {
        var updatedValue by remember { mutableStateOf(value.orEmpty()) }
        labelResId?.let {
            Text(
                text = stringResource(id = labelResId),
                style = labelStyle
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val textColor =
            if (enabled) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurface.copy(
                alpha = 0.5f
            )

        val backgroundColor =
            if (enabled) textfieldBackgroundColor else textfieldBackgroundColor.copy(alpha = 0.5f)

        OutlinedTextField(
            value = updatedValue,
            onValueChange = {
                updatedValue = it
                onValueChange(it)
            },
            visualTransformation = if (isInputMasked.value) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = formTextFieldStyle,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Gray500,
                unfocusedBorderColor = Gray500,
                backgroundColor = backgroundColor,
                textColor = textColor,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = enabled,
            placeholder = placeholderResId?.let {
                {
                    Text(
                        text = stringResource(id = it),
                        style = placeholderStyle
                    )
                }
            },
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = if (isPassword) {
                { PasswordTrailingIcon(isInputMasked) }
            } else {
                null
            },
            singleLine = singleLine,
        )
        Text(
            text = helperText.orEmpty(),
            style = formLabelStyle,
            color = if (isError) MaterialTheme.colors.error else Color.Unspecified
        )
    }
}

@Composable
fun PasswordTrailingIcon(isInputMasked: MutableState<Boolean>) {
    Crossfade(targetState = isInputMasked.value) { isMasked ->
        if (isMasked) {
            IconButton(onClick = { isInputMasked.value = false }) {
                Icon(
                    imageVector = Icons.Filled.VisibilityOff,
                    contentDescription = stringResource(R.string.show_password_description)
                )
            }
        } else {
            IconButton(onClick = { isInputMasked.value = true }) {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = stringResource(R.string.hide_password_description)
                )
            }
        }
    }
}

