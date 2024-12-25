package io.newm.screens.library.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.newm.core.resources.R
import io.newm.core.theme.GraySuit
import io.newm.core.theme.White
import io.newm.core.theme.inter
import io.newm.core.ui.permissions.AppPermission
import io.newm.core.ui.permissions.doWithPermission
import io.newm.core.ui.permissions.rememberRequestPermissionIntent
import io.newm.core.ui.utils.shortToast
import io.newm.core.ui.wallet.ConnectWalletPanel
import io.newm.feature.barcode.scanner.BarcodeScannerActivity
import io.newm.screens.library.TAG_NFT_LIBRARY_SCREEN

@Composable
fun LinkWalletScreen(
    modifier: Modifier = Modifier,
    onConnectWallet: (String) -> Unit
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
            onConnectWallet(newmWalletConnectionId)
        }
    }

    val onRequestPermissionGranted = {
        val intent = Intent(context, BarcodeScannerActivity::class.java)
        launcher.launch(intent)
    }

    val navigateToAppSettings = {
        context.shortToast(context.getString(R.string.wallet_link_camera_is_needed))
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    val requestPermission =
        rememberRequestPermissionIntent(onGranted = { onRequestPermissionGranted.invoke() },
            onDismiss = { navigateToAppSettings.invoke() })
    Box(
        modifier = modifier
            .padding(vertical = 16.dp)
            .defaultMinSize(minHeight = 200.dp)
            .testTag(TAG_NFT_LIBRARY_SCREEN),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.welcome_to_newm),
                textAlign = TextAlign.Center,
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.library_no_wallet_connected_subtitle),
                textAlign = TextAlign.Center,
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = GraySuit
            )
            Spacer(modifier = Modifier.weight(1f))

            ConnectWalletPanel(onButtonClick = {
                context.run {
                    doWithPermission(
                        onGranted = { onRequestPermissionGranted.invoke() },
                        requestPermissionLauncher = requestPermission,
                        appPermission = AppPermission.CAMERA
                    )
                }
            })
        }
    }
}

