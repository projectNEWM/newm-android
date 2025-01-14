package io.newm.screens.profile.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.newm.core.resources.R
import io.newm.core.theme.DarkViolet
import io.newm.core.theme.Gray16
import io.newm.core.theme.LightSkyBlue
import io.newm.core.theme.NewmTheme
import io.newm.core.theme.OceanGreen
import io.newm.core.theme.Pinkish
import io.newm.core.theme.Purple
import io.newm.core.theme.White
import io.newm.core.theme.inter
import io.newm.core.ui.ConfirmationDialog
import io.newm.core.ui.LoadingScreen
import io.newm.core.ui.permissions.AppPermission
import io.newm.core.ui.permissions.doWithPermission
import io.newm.core.ui.permissions.rememberRequestPermissionIntent
import io.newm.core.ui.utils.drawWithBrush
import io.newm.core.ui.utils.iconGradient
import io.newm.core.ui.utils.shortToast
import io.newm.feature.barcode.scanner.BarcodeScannerActivity
import io.newm.screens.profile.OnConnectWallet
import io.newm.screens.profile.OnDisconnectWallet
import io.newm.screens.profile.OnEditProfile
import io.newm.screens.profile.OnLogout
import io.newm.screens.profile.OnShowPrivacyPolicy
import io.newm.screens.profile.OnShowTermsAndConditions
import io.newm.screens.profile.ProfileAppBar
import io.newm.screens.profile.ProfileBottomSheetLayout
import io.newm.screens.profile.ProfileHeader
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.models.User
import kotlinx.coroutines.launch

internal const val TAG_USER_ACCOUNT_VIEW_SCREEN = "TAG_USER_ACCOUNT_VIEW_SCREEN"

private const val RECORD_STORE_URL = "https://recordstore.newm.io/"

private val recordStoreLabelStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Medium,
    color = White
)

private val defaultButtonLabelStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Medium,
    color = Purple
)

private val disconnectButtonLabelStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Medium,
    color = LightSkyBlue
)

private val defaultProfileButtonGradient =
    iconGradient(DarkViolet.copy(alpha = 0.08f), Pinkish.copy(alpha = 0.08f))

private val disconnectWalletButtonGradient =
    iconGradient(OceanGreen.copy(alpha = 0.08f), LightSkyBlue.copy(alpha = 0.08f))

private val disconnectWalletButtonTextGradient =
    iconGradient(OceanGreen, LightSkyBlue)

@Composable
fun ProfileUi(
    state: ProfileUiState,
    modifier: Modifier = Modifier,
    eventLogger: NewmAppEventLogger
) {
    when (state) {
        ProfileUiState.Loading -> LoadingScreen()
        is ProfileUiState.Content -> {
            ProfileUiContent(
                state = state,
                modifier = modifier,
                eventLogger = eventLogger
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProfileUiContent(
    state: ProfileUiState.Content,
    modifier: Modifier,
    eventLogger: NewmAppEventLogger
) {
    val onEvent = state.eventSink
    val openWalletDialog: MutableState<Boolean> = remember { mutableStateOf(false) }
    val user = state.profile
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val scope = rememberCoroutineScope()

    ProfileBottomSheetLayout(
        modifier = modifier.fillMaxSize(),
        sheetState = sheetState,
        eventLogger = eventLogger,
        onLogout = { onEvent(OnLogout) },
        onShowTermsAndConditions = { onEvent(OnShowTermsAndConditions) },
        onShowPrivacyPolicy = { onEvent(OnShowPrivacyPolicy) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TAG_USER_ACCOUNT_VIEW_SCREEN),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAppBar(
                bannerUrl = user.bannerUrl.orEmpty(),
                avatarUrl = user.pictureUrl.orEmpty(),
                onOverflowTapped = { scope.launch { sheetState.show() } },
            )
            ProfileHeader(
                firstName = user.firstName.orEmpty(),
                lastName = user.lastName.orEmpty(),
                email = user.email.orEmpty(),
            )
            Spacer(Modifier.height(40.dp))
            ProfileButton(
                labelResId = R.string.profile_edit_button_label,
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { onEvent(OnEditProfile) },
            )
            Spacer(Modifier.height(12.dp))
            WalletButton(
                openWalletDialog = openWalletDialog,
                isWalletConnected = state.isWalletConnected,
                eventLogger = eventLogger,
                disconnectWallet = { onEvent(OnDisconnectWallet) }
            ) { newmWalletConnectionId -> onEvent(OnConnectWallet(newmWalletConnectionId)) }
            Spacer(Modifier.weight(1f))
            RecordStorePanel(eventLogger)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

@Composable
fun RecordStorePanel(eventLogger: NewmAppEventLogger) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Gray16)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = stringResource(id = R.string.profile_add_music),
                style = recordStoreLabelStyle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileButton(
                labelResId = R.string.profile_visit_store,
                onClick = {
                    eventLogger.logPageLoad(AppScreens.RecordStoreScreen.name)
                    eventLogger.logClickEvent(AppScreens.AccountScreen.VISIT_RECORDS_BUTTON)
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(RECORD_STORE_URL)
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun ProfileButton(
    labelResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundBrush: Brush = defaultProfileButtonGradient,
    textStyle: TextStyle = defaultButtonLabelStyle
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundBrush)
            .fillMaxWidth()
            .height(40.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
    ) {
        Text(
            text = stringResource(id = labelResId),
            style = textStyle
        )
    }
}

@Composable
private fun WalletButton(
    openWalletDialog: MutableState<Boolean>,
    isWalletConnected: Boolean,
    eventLogger: NewmAppEventLogger,
    disconnectWallet: () -> Unit,
    onConnectWalletClick: (String) -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle the returned result here
            val data = result.data
            // Do something with the data
            val newmWalletConnectionId =
                data?.getStringExtra(BarcodeScannerActivity.NEWM_WALLET_CONNECTION_ID).orEmpty()
            // create message
            val message =
                context.getString(R.string.wallet_link_connected_message, newmWalletConnectionId)
            // show message
            context.shortToast(message)
            onConnectWalletClick(newmWalletConnectionId)
        }
    }

    val requestPermission = rememberRequestPermissionIntent(
        onGranted = { /*TODO*/ },
        onDismiss = { /*TODO*/ })


    if (isWalletConnected) {

        ProfileButton(
            labelResId = R.string.profile_disconnect_wallet_button_label,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .drawWithBrush(disconnectWalletButtonTextGradient),
            onClick = { openWalletDialog.value = true },
            backgroundBrush = disconnectWalletButtonGradient,
            textStyle = disconnectButtonLabelStyle
        )
        ConfirmationDialog(
            title = stringResource(R.string.profile_unlink_dialog_title),
            message = stringResource(R.string.profile_unlink_dialog_message),
            eventLogger = eventLogger,
            isOpen = openWalletDialog,
            onConfirm = {
                disconnectWallet()
            },
            onDismiss = {
                // Handle the cancellation of logout here
                openWalletDialog.value = false
            }
        )
    } else {
        ProfileButton(
            labelResId = R.string.profile_connect_wallet_button_label,
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = {
                context.run {
                    doWithPermission(
                        onGranted = {
                            val intent = Intent(this, BarcodeScannerActivity::class.java)
                            launcher.launch(intent)
                        },
                        requestPermissionLauncher = requestPermission,
                        appPermission = AppPermission.CAMERA
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun UserAccountScreenPreview(
    @PreviewParameter(AccountScreenPreviewProvider::class)
    state: ProfileUiState,
) {
    NewmTheme(darkTheme = true) {
        ProfileUi(
            state = state,
            modifier = Modifier,
            eventLogger = NewmAppEventLogger()
        )
    }
}

internal class AccountScreenPreviewProvider : PreviewParameterProvider<ProfileUiState> {
    override val values: Sequence<ProfileUiState>
        get() = sequenceOf(
            ProfileUiState.Loading,
            ProfileUiState.Content(
                profile = User(
                    id = "",
                    createdAt = "",
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@doe.com",
                    biography = "I love music."
                ),
                isWalletConnected = false,
                eventSink = {},
            ),
            ProfileUiState.Content(
                profile = User(
                    id = "",
                    createdAt = "",
                ),
                isWalletConnected = true,
                eventSink = {},
            )
        )
}
