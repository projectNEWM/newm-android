package io.newm.screens.profile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.newm.Logout
import io.newm.shared.public.models.User
import io.newm.shared.public.usecases.UserProfileUseCase
import io.newm.shared.public.usecases.ConnectWalletUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileReadOnlyViewModel(
    private val userProviderUserCase: UserProfileUseCase,
    private val connectWalletUseCase: ConnectWalletUseCase,
    private val logout: Logout
) : ViewModel() {

    private var _state = MutableStateFlow<ProfileViewState>(ProfileViewState.Loading)

    val state: StateFlow<ProfileViewState>
        get() = _state.asStateFlow()

    init {
        println("NewmAndroid - ProfileViewModel")
        viewModelScope.launch {
            val user = userProviderUserCase.getCurrentUser()
            Logger.d { "NewmAndroid - ProfileViewModel user: $user" }
            _state.value = ProfileViewState
                .Content(
                    profile = user,
                    isWalletConnected = connectWalletUseCase.isConnected()
                )
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            logout.call()
        }
    }

    fun disconnectWallet() {
        viewModelScope.launch(Dispatchers.IO) {
            connectWalletUseCase.disconnect()
            _state.value = ProfileViewState
                .Content(
                    profile = (state.value as ProfileViewState.Content).profile,
                    isWalletConnected = false
                )
        }
    }

    fun connectWallet(xpubKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            connectWalletUseCase.connect(xpubKey)
            _state.value = ProfileViewState
                .Content(
                    profile = (state.value as ProfileViewState.Content).profile,
                    isWalletConnected = true
                )
        }
    }
}

sealed class ProfileViewState {
    object Loading : ProfileViewState()
    data class Content(val profile: User, val isWalletConnected: Boolean) : ProfileViewState()
}