package io.newm.screens.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.Logout
import io.newm.screens.Screen.EditProfile
import io.newm.screens.Screen.WalletConnect
import io.newm.shared.config.NewmSharedBuildConfig
import io.newm.shared.public.usecases.ConnectWalletUseCase
import io.newm.shared.public.usecases.UserDetailsUseCase
import kotlinx.coroutines.launch

class UserAccountPresenter(
    private val navigator: Navigator,
    private val userDetailsUseCase: UserDetailsUseCase,
    private val connectWalletUseCase: ConnectWalletUseCase,
    private val logout: Logout,
    private val sharedBuildConfig: NewmSharedBuildConfig,
) : Presenter<UserAccountState> {
    @Composable
    override fun present(): UserAccountState {
        val isWalletConnected by remember { connectWalletUseCase.hasWalletConnections() }.collectAsState(
            false
        )

        val user by remember { userDetailsUseCase.fetchLoggedInUserDetailsFlow() }.collectAsState(
            null
        )

        return if (user == null) {
            UserAccountState.Loading
        } else {
            val coroutineScope = rememberStableCoroutineScope()

            UserAccountState.Content(
                profile = user!!,
                isWalletConnected = isWalletConnected,
                showWalletConnectButton = sharedBuildConfig.isStagingMode,
                eventSink = { event ->
                    when (event) {
                        is UserAccountEvent.OnConnectWallet -> coroutineScope.launch {
                            connectWalletUseCase.connect(event.xpubKey)
                        }

                        UserAccountEvent.OnDisconnectWallet -> coroutineScope.launch {
                            connectWalletUseCase.disconnect()
                        }

                        UserAccountEvent.OnEditProfile -> navigator.goTo(EditProfile)
                        UserAccountEvent.OnLogout -> logout.signOutUser()
                        UserAccountEvent.OnWalletConnectProtocol -> navigator.goTo(WalletConnect)
                    }
                }
            )
        }
    }
}
