package io.newm.screens.profile.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.Logout
import io.newm.screens.Screen.EditProfile
import io.newm.screens.Screen.PrivacyPolicy
import io.newm.screens.Screen.TermsAndConditions
import io.newm.screens.profile.OnConnectWallet
import io.newm.screens.profile.OnDisconnectWallet
import io.newm.screens.profile.OnEditProfile
import io.newm.screens.profile.OnLogout
import io.newm.screens.profile.OnShowPrivacyPolicy
import io.newm.screens.profile.OnShowTermsAndConditions
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.usecases.ConnectWalletUseCase
import io.newm.shared.public.usecases.DisconnectWalletUseCase
import io.newm.shared.public.usecases.HasWalletConnectionsUseCase
import io.newm.shared.public.usecases.SyncWalletConnectionsUseCase
import io.newm.shared.public.usecases.UserDetailsUseCase
import kotlinx.coroutines.launch

class ProfilePresenter(
    private val navigator: Navigator,
    private val hasWalletConnectionsUseCase: HasWalletConnectionsUseCase,
    private val syncWalletConnectionsUseCase: SyncWalletConnectionsUseCase,
    private val disconnectWalletUseCase: DisconnectWalletUseCase,
    private val userDetailsUseCase: UserDetailsUseCase,
    private val connectWalletUseCase: ConnectWalletUseCase,
    private val logout: Logout,
    private val eventLogger: NewmAppEventLogger
) : Presenter<ProfileUiState> {
    @Composable
    override fun present(): ProfileUiState {

        val coroutineScope = rememberStableCoroutineScope()

        val isWalletConnected by remember { hasWalletConnectionsUseCase.hasWalletConnectionsFlow() }.collectAsState(
            false
        )

        LaunchedEffect(Unit) {
            syncWalletConnectionsUseCase.syncWalletConnectionsFromNetworkToDevice()
        }

        val user by remember { userDetailsUseCase.fetchLoggedInUserDetailsFlow() }.collectAsState(
            null
        )

        return if (user == null) {
            ProfileUiState.Loading
        } else {
            ProfileUiState.Content(
                profile = user!!,
                isWalletConnected = isWalletConnected,
                eventSink = { event ->
                    when (event) {
                        is OnConnectWallet -> coroutineScope.launch {
                            eventLogger.logClickEvent("Connect Wallet")
                            connectWalletUseCase.connect(event.newmCode)
                        }

                        OnDisconnectWallet -> coroutineScope.launch {
                            eventLogger.logClickEvent("Disconnect Wallet")
                            disconnectWalletUseCase.disconnect()
                        }

                        OnEditProfile -> {
                            eventLogger.logClickEvent("Edit Profile")
                            navigator.goTo(EditProfile)
                        }
                        OnLogout -> {
                            eventLogger.logClickEvent("Logout")
                            logout.signOutUser()
                        }
                        OnShowTermsAndConditions -> {
                            eventLogger.logClickEvent("Terms and Conditions")
                            navigator.goTo(TermsAndConditions)
                        }
                        OnShowPrivacyPolicy -> {
                            eventLogger.logClickEvent("Privacy Policy")
                            navigator.goTo(PrivacyPolicy)
                        }
                    }
                }
            )
        }
    }
}

