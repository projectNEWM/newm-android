package io.newm.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.newm.BuildConfig
import io.newm.core.resources.R
import io.newm.core.theme.Black90
import io.newm.core.theme.Gray400
import io.newm.core.ui.buttons.PrimaryButton
import io.newm.core.ui.buttons.SecondaryButton
import io.newm.core.ui.text.versionTextStyle
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState,
    eventLogger: NewmAppEventLogger,
    onLogout: () -> Unit,
    onShowTermsAndConditions: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetContent = {
            if(sheetState.isVisible) {
                LaunchedEffect(Unit) {
                    eventLogger.logPageLoad(AppScreens.AccountOptionsScreen.name)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colors.surface,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Divider(
                    thickness = 1.dp,
                    color = Gray400
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    SecondaryButton(
                        labelResId = R.string.privacy_policy,
                        onClick = onShowPrivacyPolicy
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SecondaryButton(
                        labelResId = R.string.profile_terms_and_condition,
                        onClick = onShowTermsAndConditions
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryButton(
                        text = stringResource(id = R.string.user_account_logout),
                        onClick = onLogout
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppVersion()
                }
            }
        },
        scrimColor = Black90,
        content = content
    )
}

@Composable
private fun AppVersion(){
    Column {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            text = "Version " + BuildConfig.VERSION_NAME,
            style = versionTextStyle.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            text = "Build: " + BuildConfig.VERSION_CODE,
            style = versionTextStyle
        )
    }
}
