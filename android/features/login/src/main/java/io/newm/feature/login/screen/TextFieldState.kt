package io.newm.feature.login.screen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

open class TextFieldState(
    defaultValue: String = "",
    private val validator: (String) -> Boolean = { true },
    private val errorFor: (Context, String) -> String = { _, _ -> "" }
) {
    var text: String by mutableStateOf(defaultValue)

    // was the TextField ever focused
    var isFocusedDirty: Boolean by mutableStateOf(false)
    var isFocused: Boolean by mutableStateOf(false)
    private var displayErrors: Boolean by mutableStateOf(false)

    open val isValid: Boolean
        get() = validator(text)

    fun onFocusChange(focused: Boolean) {
        isFocused = focused
        if (focused) isFocusedDirty = true
    }

    fun enableShowErrors() {
        // only show errors if the text was at least once focused
        if (isFocusedDirty) {
            displayErrors = true
        }
    }

    fun showErrors() = !isValid && displayErrors

    /**
     * Gets error string
     * @param context   needed for fetching string resource
     */
    open fun getError(context: Context): String? {
        return if (showErrors()) {
            errorFor(context, text)
        } else {
            null
        }
    }
}

