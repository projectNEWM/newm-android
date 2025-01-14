package io.newm.screens.library.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.newm.core.resources.R
import io.newm.screens.library.TAG_NFT_LIBRARY_SCREEN

@Composable
fun ZeroSearchResults() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag(TAG_NFT_LIBRARY_SCREEN),
        contentAlignment = Alignment.Center // This centers the content both horizontally and vertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(all = 16.dp),
                text = stringResource(id = R.string.search_zero_results)
            )
        }
    }
}