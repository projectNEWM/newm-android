package io.newm.screens.library.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.newm.core.resources.R
import io.newm.core.ui.buttons.PrimaryButton
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens

private const val RECORD_STORE_URL = "https://recordstore.newm.io/"

@Composable
fun EmptyWalletScreen(eventLogger: NewmAppEventLogger) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.wallet_empty_title),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.wallet_empty_subtitle),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            PrimaryButton(
                modifier = Modifier.padding(all = 16.dp),
                text = stringResource(id = R.string.wallet_empty_button),
                onClick = {
                    eventLogger.logClickEvent(AppScreens.NFTLibraryEmptyWalletScreen.VISIT_RECORDS_BUTTON)
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RECORD_STORE_URL)))
                },
            )
        }
    }
}
