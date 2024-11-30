package io.newm.shared.public.analytics.events

import io.newm.shared.public.analytics.ScreenEvents


object AppScreens {
    //General Screens
    object LoadingScreen : ScreenEvents {
        override val name = "Loading"
    }

    object ErrorScreen : ScreenEvents {
        override val name = "Error"
    }

    //Main Screens
    object AccountOptionsScreen : ScreenEvents {
        override val name = "Account Options"
    }

    object AccountScreen : ScreenEvents {
        override val name = "Account"
        const val VISIT_RECORDS_BUTTON: String = "Visit the Record Store"
        const val CONNECT_WALLET_BUTTON: String = "Connect Wallet"
        const val EDIT_PROFILE_BUTTON: String = "Edit Profile"
        const val DISCONNECT_WALLET_BUTTON: String = "Disconnect Wallet"
        const val LOGOUT_BUTTON: String = "Logout"
        const val TERMS_AND_CONDITIONS_BUTTON: String = "Terms and Conditions"
        const val PRIVACY_POLICY_BUTTON: String = "Privacy Policy"
    }

    object ConnectWalletScannerScreen : ScreenEvents {
        override val name = "Connect Wallet Scanner"
        const val COPY_URL_BUTTON: String = "Copy URL"
    }

    object EditProfileScreen : ScreenEvents {
        override val name = "Edit Profile"
        const val SAVE_CHANGES_BUTTON: String = "Save changes"
        const val BACK_BUTTON: String = "Back"
    }

    object EmailVerificationScreen : ScreenEvents {
        override val name = "Email Verification"
        const val CONTINUE_BUTTON: String = "Continue"
    }

    object ForceUpdateScreen : ScreenEvents {
        override val name = "Force App Update"
        const val UPDATE_BUTTON: String = "Update Now"

    }

    object LogoutConfirmationDialogScreen : ScreenEvents {
        override val name = "Logout Confirmation Dialog"
    }

    object MusicPlayerScreen : ScreenEvents {
        override val name = "Music Player"
        const val PLAY_BUTTON: String = "Play"
        const val PAUSE_BUTTON: String = "Pause"
        const val NEXT_BUTTON: String = "Next"
        const val PREVIOUS_BUTTON: String = "Previous"
        const val STOP_BUTTON: String = "Stop"
        const val REPEAT_BUTTON: String = "Repeat"
        const val SEEK_ACTION: String = "Seek Action"
        const val TOGGLE_SHUFFLE_BUTTON: String = "Toggle Shuffle"
    }

    object NFTLibraryEmptyWalletScreen : ScreenEvents {
        override val name = "NFT Library Empty Wallet"
        const val VISIT_RECORDS_BUTTON: String = "Visit the Record Store"
    }

    object NFTLibraryFilterScreen : ScreenEvents {
        override val name = "NFT Library Filter"
        const val APPLY_BUTTON: String = "Apply Filters"
    }

    object NFTLibraryLinkWalletScreen : ScreenEvents {
        override val name = "NFT Library Link Wallet"
    }

    object NFTLibraryScreen : ScreenEvents {
        override val name = "NFT Library"
        const val REFRESH_BUTTON: String = "Refresh"
        const val SEARCH_BUTTON: String = "Search"
        const val PLAYLIST_SIZE_EVENT: String = "Playlist Size"
    }

    object NewPasswordScreen : ScreenEvents {
        override val name = "Reset Password Set New Password"
        const val CONFIRM_BUTTON: String = "Confirm"
    }

    object PrivacyPolicyScreen : ScreenEvents {
        override val name = "Privacy Policy"
    }

    object RecordStoreScreen : ScreenEvents {
        override val name = "Record Store"
        const val RECORD_STORE_BUTTON: String = "RecordStore"
    }

    object ResetPasswordEnterCodeScreen : ScreenEvents {
        override val name = "Reset Password Enter Code"
        const val CONTINUE_BUTTON: String = "Continue"
    }

    object ResetPasswordEnterEmailScreen : ScreenEvents {
        override val name = "Reset Password Enter Email"
        const val CONTINUE_BUTTON: String = "Continue"
    }

    object TermsOfServiceScreen : ScreenEvents {
        override val name = "Terms of Service"
    }

    object WalletInstructionsScreen : ScreenEvents {
        override val name = "Wallet Instructions"
    }

    object WelcomeScreen : ScreenEvents {
        override val name = "Welcome"
        const val CREATE_ACCOUNT_BUTTON: String = "Create account"
        const val LOGIN_WITH_EMAIL_BUTTON: String = "Login with Email"
        const val LOGIN_WITH_GOOGLE_BUTTON: String = "Login with Google"
    }

    object LogInWithEmailScreen : ScreenEvents {
        override val name = "Login With Email"
        const val FORGOT_PASSWORD_BUTTON: String = "Forgot your password?"
        const val LOGIN_BUTTON: String = "Login"
    }

    object CreateAccountScreen : ScreenEvents {
        override val name = "Create Account"
        const val NEXT_BUTTON: String = "Next"
    }

    object HomeScreen : ScreenEvents {
        override val name = "Home"
        const val NFT_LIBRARY_BUTTON: String = "NFT Library"
        const val ACCOUNT_BUTTON: String = "Account"
    }

}