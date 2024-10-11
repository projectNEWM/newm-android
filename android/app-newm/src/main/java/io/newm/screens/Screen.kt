package io.newm.screens

import io.newm.shared.public.analytics.events.AppScreens
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.slack.circuit.runtime.screen.Screen as CircuitScreen

@Parcelize
sealed class Screen(val screenName: String) : CircuitScreen {

    data object UserAccount : Screen(screenName = AppScreens.AccountScreen.name)

    data object RecordStore : Screen(screenName = AppScreens.RecordStoreScreen.name)

    data object NFTLibrary : Screen(screenName = AppScreens.NFTLibraryScreen.name)

    data object Welcome : Screen(screenName = AppScreens.WelcomeScreen.name)

    data object EditProfile : Screen(screenName = AppScreens.EditProfileScreen.name)

    data object WalletConnect : Screen(screenName = AppScreens.ConnectWalletScannerScreen.name)

    data object ForceAppUpdate : Screen(screenName = AppScreens.ForceUpdateScreen.name)

    data object TermsOfService : Screen(screenName = AppScreens.TermsOfServiceScreen.name), WebBrowserScreen {
        @IgnoredOnParcel
        override val url: String = "https://newm.io/app-tos"
    }

    data object PrivacyPolicy : Screen(screenName = AppScreens.PrivacyPolicyScreen.name), WebBrowserScreen {
        @IgnoredOnParcel
        override val url: String = "https://newm.io/app-privacy"

    }
}

/**
 * A screen that launches a web browser with a specific URL.
 */
interface WebBrowserScreen {
    val url: String
}
