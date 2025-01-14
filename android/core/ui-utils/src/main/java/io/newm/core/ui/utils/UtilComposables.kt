package io.newm.core.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.newm.core.resources.R as CoreR

@Composable
fun ToBeImplemented(
    text: String = stringResource(CoreR.string.to_be_implemented_message),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clickable {
                context.shortToast(text)
            },
    ) {
        content()
    }
}