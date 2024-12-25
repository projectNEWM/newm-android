package io.newm.feature.login.screen.password

import android.content.Context
import io.newm.core.resources.R
import io.newm.feature.login.screen.TextFieldState
import io.newm.shared.login.util.LoginFieldValidator

class PasswordState : TextFieldState(
    validator = ::isPasswordValid,
    errorFor = { cxt, _ -> passwordValidationError(cxt) }
)

class ConfirmPasswordState(private val passwordState: PasswordState) : TextFieldState() {
    override val isValid
        get() = passwordAndConfirmationValid(passwordState.text, text)

    override fun getError(context: Context): String? {
        return if (showErrors()) {
            passwordConfirmationError(context)
        } else {
            null
        }
    }
}

private const val MINIMUM_VERIFICATION_CODE_LENGTH = 6

class VerificationCodeState : TextFieldState(
    validator = { it.length >= MINIMUM_VERIFICATION_CODE_LENGTH },
    errorFor = { cxt, _ ->
        cxt.getString(
            R.string.code_verification_error_message,
            MINIMUM_VERIFICATION_CODE_LENGTH
        )
    },
)

private fun passwordAndConfirmationValid(password: String, confirmedPassword: String): Boolean {
    return isPasswordValid(password) && password == confirmedPassword
}

fun isPasswordValid(password: String): Boolean {
    return LoginFieldValidator.isPasswordValid(password)
}

fun passwordValidationError(context: Context): String {
    return context.getString(R.string.password_validation_error_message)
}

private fun passwordConfirmationError(context: Context): String {
    return context.getString(R.string.password_confirmation_error_message)
}
