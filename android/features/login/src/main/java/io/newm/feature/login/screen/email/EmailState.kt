package io.newm.feature.login.screen.email

import android.content.Context
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import io.newm.core.resources.R
import io.newm.feature.login.screen.TextFieldState

class EmailState : TextFieldState(validator = ::isEmailValid, errorFor = ::emailValidationError)

/**
 * Returns an error to be displayed or null if no error was found
 *
 * @param context   needed to get string resource
 * @param email     invalid email we show the user
 */
private fun emailValidationError(context: Context, email: String): String {
    return context.getString(R.string.email_validation_error_message, email)
}

private fun isEmailValid(email: String): Boolean {
    return EMAIL_ADDRESS.matcher(email).matches()
}
