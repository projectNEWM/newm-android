package io.newm.screens.profile

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface ProfileUiEvent : CircuitUiEvent
sealed interface ProfileEditUiEvent : CircuitUiEvent
sealed interface RecordStoreUiEvent : CircuitUiEvent
/** Profile UI Events */
data object OnDisconnectWallet : ProfileUiEvent
data object OnEditProfile : ProfileUiEvent

/** Profile Edit UI Events */
data object OnBack : ProfileEditUiEvent
data object OnSaveProfile : ProfileEditUiEvent

/** Profile edit and view UI Events */
data class OnConnectWallet(val newmCode: String) : ProfileEditUiEvent, ProfileUiEvent
data object OnLogout : ProfileEditUiEvent, ProfileUiEvent
data object OnShowTermsAndConditions : ProfileEditUiEvent, ProfileUiEvent
data object OnShowPrivacyPolicy : ProfileEditUiEvent, ProfileUiEvent

