package io.newm.screens.recordstore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.newm.core.resources.R
import io.newm.core.ui.LoadingScreen
import io.newm.core.ui.utils.ErrorScreen
import io.newm.core.ui.webview.FullScreenWebView
import io.newm.screens.library.TAG_NFT_LIBRARY_SCREEN
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens

private const val RECORD_STORE_URL = "https://recordstore.newm.io/"

@Composable
fun RecordStoreScreenUi(
    modifier: Modifier = Modifier,
    state: RecordStoreState,
    eventLogger: NewmAppEventLogger
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag(TAG_NFT_LIBRARY_SCREEN),
    ) {
        when (state) {
            is RecordStoreState.Content -> {
                FullScreenWebView(context, RECORD_STORE_URL)
            }

            RecordStoreState.Loading -> {
                LaunchedEffect(Unit) {
                    eventLogger.logPageLoad(AppScreens.LoadingScreen.name)
                }
                LoadingScreen()
            }

            RecordStoreState.Error -> {
                ErrorScreen(
                    title = stringResource(R.string.record_store_error_title),
                    message = stringResource(R.string.record_store_error_message)
                )
            }
        }
    }
}

