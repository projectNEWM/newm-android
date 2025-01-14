package io.newm.screens.profile.edit

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.Logout
import io.newm.core.resources.R
import io.newm.feature.login.screen.TextFieldState
import io.newm.feature.login.screen.password.isPasswordValid
import io.newm.feature.login.screen.password.passwordValidationError
import io.newm.screens.Screen.PrivacyPolicy
import io.newm.screens.Screen.TermsOfService
import io.newm.screens.profile.OnBack
import io.newm.screens.profile.OnConnectWallet
import io.newm.screens.profile.OnLogout
import io.newm.screens.profile.OnSaveProfile
import io.newm.screens.profile.OnShowPrivacyPolicy
import io.newm.screens.profile.OnShowTermsAndConditions
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.models.User
import io.newm.shared.public.models.canEditName
import io.newm.shared.public.usecases.ConnectWalletUseCase
import io.newm.shared.public.usecases.HasWalletConnectionsUseCase
import io.newm.shared.public.usecases.UserDetailsUseCase
import kotlinx.coroutines.launch

class ProfileEditPresenter(
    private val navigator: Navigator,
    private val hasWalletConnectionsUseCase: HasWalletConnectionsUseCase,
    private val userDetailsUseCase: UserDetailsUseCase,
    private val connectWalletUseCase: ConnectWalletUseCase,
    private val logout: Logout,
    private val logger: NewmAppLogger,
    private val eventLogger: NewmAppEventLogger
) : Presenter<ProfileEditUiState> {
    @Composable
    override fun present(): ProfileEditUiState {
        val storedUser by remember {
            userDetailsUseCase.fetchLoggedInUserDetailsFlow()
        }.collectAsState(initial = null)

        val isWalletConnected by remember {
            hasWalletConnectionsUseCase.hasWalletConnectionsFlow()
        }.collectAsState(initial = false)

        val context = LocalContext.current

        val profile = remember(storedUser) {
            storedUser?.let { user ->
                ProfileEditUiState.Content.Profile(
                    pictureUrl = user.pictureUrl.orEmpty(),
                    bannerUrl = user.bannerUrl.orEmpty(),
                    firstName = user.firstName.orEmpty(),
                    lastName = user.lastName.orEmpty(),
                    canUserEditName = user.canEditName(),
                    email = user.email.orEmpty(),
                )
            }
        }

        val firstNameState = remember(profile?.firstName) {
            TextFieldState(profile?.firstName.orEmpty())
        }

        val lastNameState = remember(profile?.lastName) {
            TextFieldState(profile?.lastName.orEmpty())
        }

        val currentPasswordState = remember {
            TextFieldState()
        }

        val newPasswordState = remember {
            TextFieldState()
        }

        val confirmPasswordState = remember {
            TextFieldState()
        }

        var errorMessage by remember {
            mutableStateOf<String?>(null)
        }

        val isFormDirty =
            remember(
                firstNameState.isFocusedDirty,
                lastNameState.isFocusedDirty,
                currentPasswordState.isFocusedDirty,
                newPasswordState.isFocusedDirty,
                confirmPasswordState.isFocusedDirty
            ) {
                listOf(
                    firstNameState,
                    lastNameState,
                    currentPasswordState,
                    newPasswordState,
                    confirmPasswordState
                ).any { it.isFocusedDirty }
            }

        val coroutineScope = rememberStableCoroutineScope()

        return if (profile == null) {
            ProfileEditUiState.Loading
        } else {
            ProfileEditUiState.Content(
                profile = profile,
                errorMessage = errorMessage,
                submitButtonEnabled = isFormDirty,
                firstName = firstNameState,
                lastName = lastNameState,
                canUserEditName = profile.canUserEditName,
                currentPasswordState = currentPasswordState,
                newPasswordState = newPasswordState,
                confirmPasswordState = confirmPasswordState,
                showConnectWallet = !isWalletConnected,
            ) { event ->
                when (event) {
                    is OnSaveProfile -> {
                        eventLogger.logClickEvent(AppScreens.EditProfileScreen.SAVE_CHANGES_BUTTON)
                        coroutineScope.launch {
                            try {
                                val error = getFormErrorOrNull(
                                    context,
                                    currentPasswordState,
                                    newPasswordState,
                                    confirmPasswordState,
                                    firstNameState,
                                    lastNameState
                                )

                                errorMessage = error

                                if (error != null) {
                                    return@launch
                                }

                                val updatedProfile = User(
                                    newPassword = newPasswordState.text.takeIf { it.isNotEmpty() },
                                    currentPassword = currentPasswordState.text.takeIf { it.isNotEmpty() },
                                    confirmPassword = confirmPasswordState.text.takeIf { it.isNotEmpty() },
                                    firstName = firstNameState.text,
                                    lastName = lastNameState.text,
                                    createdAt = "",
                                    id = ""
                                )
                                userDetailsUseCase.updateUserDetails(updatedProfile)
                                navigator.pop()
                            } catch (e: Throwable) {
                                logger.error("ProfileEditPresenter", "An error occurred", e)
                                errorMessage = "An error occurred. Please try again."
                            }
                        }
                    }

                    is OnConnectWallet -> coroutineScope.launch {
                        eventLogger.logClickEvent(AppScreens.AccountScreen.CONNECT_WALLET_BUTTON)
                        connectWalletUseCase.connect(event.newmCode)
                    }

                    OnLogout -> {
                        eventLogger.logClickEvent(AppScreens.AccountScreen.LOGOUT_BUTTON)
                        logout.signOutUser()
                    }

                    OnShowTermsAndConditions -> {
                        eventLogger.logClickEvent(AppScreens.AccountScreen.TERMS_AND_CONDITIONS_BUTTON)
                        navigator.goTo(TermsOfService)
                    }

                    OnShowPrivacyPolicy -> {
                        eventLogger.logClickEvent(AppScreens.AccountScreen.PRIVACY_POLICY_BUTTON)
                        navigator.goTo(PrivacyPolicy)
                    }

                    OnBack -> {
                        eventLogger.logClickEvent(AppScreens.EditProfileScreen.BACK_BUTTON)
                        navigator.pop()
                    }
                }
            }
        }
    }

    /**
     * Returns an error message if the form is invalid, or null if the form is valid.
     *
     * The form is invalid if any of the following conditions are met:
     *
     * - The new password is not empty and does not match the correct format.
     * - The confirm password does not match the new password.
     * - The new password is not empty, but the confirm password is empty.
     * - The new password is not empty, but the current password is empty.
     */

    private fun getFormErrorOrNull(
        context: Context,
        currentPasswordState: TextFieldState,
        newPasswordState: TextFieldState,
        confirmPasswordState: TextFieldState,
        firstNameState: TextFieldState,
        lastNameState: TextFieldState
    ): String? {
        if (newPasswordState.text.isNotEmpty() && isPasswordValid(newPasswordState.text).not()) {
            return passwordValidationError(context)
        }

        if (newPasswordState.text.isNotEmpty() && newPasswordState.text != confirmPasswordState.text) {
            return context.getString(R.string.password_confirmation_error_message)
        }

        if (newPasswordState.text.isNotEmpty() && confirmPasswordState.text.isEmpty()) {
            return context.getString(R.string.profile_confirm_new_password)
        }

        if (newPasswordState.text.isNotEmpty() && currentPasswordState.text.isEmpty()) {
            return context.getString(R.string.profile_enter_current_password)
        }

        if (firstNameState.text.isEmpty()) {
            return context.getString(R.string.profile_enter_first_name)
        }

        if (lastNameState.text.isEmpty()) {
            return context.getString(R.string.profile_enter_last_name)
        }

        return null
    }
}
