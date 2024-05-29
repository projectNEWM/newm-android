package io.newm.screens.profile

import com.slack.circuit.runtime.CircuitUiEvent
import io.newm.shared.public.models.User

sealed interface ProfileUiEvent : CircuitUiEvent
sealed interface ProfileEditUiEvent : CircuitUiEvent

/** Profile UI Events */
data object OnDisconnectWallet : ProfileUiEvent
data object OnEditProfile : ProfileUiEvent

/** Profile Edit UI Events */
data object OnBack : ProfileEditUiEvent
data class OnSaveProfile(val updatedProfile: User) : ProfileEditUiEvent

/** Profile edit and view UI Events */
data class OnConnectWallet(val xpub: String) : ProfileEditUiEvent, ProfileUiEvent
data object OnLogout : ProfileEditUiEvent, ProfileUiEvent
data object OnShowTermsAndConditions : ProfileEditUiEvent, ProfileUiEvent
data object OnShowPrivacyPolicy : ProfileEditUiEvent, ProfileUiEvent
data object OnShowDocuments : ProfileEditUiEvent, ProfileUiEvent
data object OnShowAskTheCommunity : ProfileEditUiEvent, ProfileUiEvent
data object OnShowFaq : ProfileEditUiEvent, ProfileUiEvent
