package io.newm

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import io.newm.core.resources.R
import io.newm.core.theme.Black
import io.newm.core.theme.BrightOrange
import io.newm.core.theme.DarkPink
import io.newm.core.theme.DarkViolet
import io.newm.core.theme.Gray100
import io.newm.core.theme.LightSkyBlue
import io.newm.core.theme.OceanGreen
import io.newm.core.theme.YellowJacket
import io.newm.core.theme.inter
import io.newm.core.ui.utils.drawWithBrush
import io.newm.core.ui.utils.iconGradient
import io.newm.feature.musicplayer.MiniPlayer
import io.newm.feature.musicplayer.MusicPlayerScreen
import io.newm.screens.Screen
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import kotlinx.coroutines.launch
import com.slack.circuit.runtime.screen.Screen as CircuitScreen

internal const val TAG_BOTTOM_NAVIGATION = "TAG_BOTTOM_NAVIGATION"

private val HomeIconGradient = iconGradient(LightSkyBlue, DarkViolet)
private val AccountIconGradient = iconGradient(LightSkyBlue, DarkViolet)
private val LibraryIconGradient = iconGradient(DarkViolet, DarkPink)
private val SearchIconGradient = iconGradient(DarkPink, BrightOrange)
private val WalletIconGradient = iconGradient(OceanGreen, LightSkyBlue)
private val MarketIconGradient = iconGradient(BrightOrange, YellowJacket)

val LocalIsBottomBarVisible = compositionLocalOf { mutableStateOf(true) }

@Composable
internal fun isBottomBarVisible() = remember { mutableStateOf(true) }

val initialScreen = Screen.NFTLibrary

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun NewmApp(
    logger: NewmAppLogger,
    eventLogger: NewmAppEventLogger,
    showRecordStore: Boolean
) {
    val context = LocalContext.current
    val backstack = rememberSaveableBackStack {
        push(initialScreen)
    }

    val circuitNavigator = rememberCircuitNavigator(
        backstack,
        // Disabling back handler because we are using our own
        enableBackHandler = false
    )

    val newmNavigator =
        rememberNewmNavigator(circuitNavigator, logger, {}, launchBrowser = { url ->
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }, eventLogger)

    val currentRootScreen = backstack.topRecord?.screen

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val coroutineScope = rememberCoroutineScope()

    BackHandler(
        enabled = backstack.size > 1 || currentRootScreen != initialScreen || sheetState.isVisible
    ) {
        when {
            sheetState.isVisible -> coroutineScope.launch { sheetState.hide() }
            backstack.size > 1 -> newmNavigator.pop()
            currentRootScreen != initialScreen -> newmNavigator.resetRoot(initialScreen)
        }
    }

    ModalBottomSheetLayout(
        modifier = Modifier,
        sheetState = sheetState,
        sheetContent = {
            MusicPlayerScreen(
                eventLogger = eventLogger,
                onNavigateUp = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                })
        },
    ) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = LocalIsBottomBarVisible.current.value,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = ExitTransition.None
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MiniPlayer(
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    eventLogger.logPageLoad(AppScreens.MusicPlayerScreen.name)
                                    sheetState.show()
                                }
                            },
                            eventLogger = eventLogger
                        )
                        Spacer(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                        )
                        NewmBottomNavigation(
                            currentRootScreen = currentRootScreen,
                            eventLogger = eventLogger,
                            showRecordStore = showRecordStore,
                            onNavigationSelected = {
                                circuitNavigator.resetRoot(it)
                            }
                        )
                    }
                }
            }
        ) { padding ->
            NavigableCircuitContent(
                modifier = Modifier.padding(padding),
                navigator = newmNavigator,
                backstack = backstack
            )
        }

    }
}

@Composable
internal fun NewmBottomNavigation(
    currentRootScreen: CircuitScreen?,
    eventLogger: NewmAppEventLogger,
    showRecordStore: Boolean,
    onNavigationSelected: (Screen) -> Unit
) {
    Column(Modifier.height(76.dp)) {
        BottomNavigation(
            modifier = Modifier
                .fillMaxHeight()
                .testTag(TAG_BOTTOM_NAVIGATION),
            backgroundColor = Black,
            contentColor = Gray100
        ) {
            HomeBottomNavigationItem(
                selected = currentRootScreen == Screen.NFTLibrary,
                iconResId = R.drawable.ic_library,
                labelResId = R.string.nft_library,
                selectedIconBrush = LibraryIconGradient,
                selectedLabelColor = DarkPink,
                onClick = {
                    eventLogger.logClickEvent(AppScreens.HomeScreen.NFT_LIBRARY_BUTTON)
                    onNavigationSelected(Screen.NFTLibrary)
                },
            )
            if (showRecordStore) {
                HomeBottomNavigationItem(
                    selected = currentRootScreen == Screen.RecordStore,
                    iconResId = R.drawable.ic_recordstore_active,
                    labelResId = R.string.record_store,
                    selectedIconBrush = AccountIconGradient,
                    selectedLabelColor = DarkPink,
                    onClick = {
                        eventLogger.logClickEvent(AppScreens.RecordStoreScreen.RECORD_STORE_BUTTON)
                        eventLogger.logPageLoad(AppScreens.RecordStoreScreen.name)
                        onNavigationSelected(Screen.RecordStore)
                    },
                )
            }
            HomeBottomNavigationItem(
                selected = currentRootScreen == Screen.UserAccount,
                iconResId = R.drawable.ic_profile,
                labelResId = R.string.account,
                selectedIconBrush = AccountIconGradient,
                selectedLabelColor = DarkPink,
                onClick = {
                    eventLogger.logClickEvent(AppScreens.HomeScreen.ACCOUNT_BUTTON)
                    eventLogger.logPageLoad(AppScreens.AccountScreen.name)
                    onNavigationSelected(Screen.UserAccount)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    NewmBottomNavigation(Screen.NFTLibrary, NewmAppEventLogger(), showRecordStore = false) {}
}

// Based on content from: https://github.com/wlara/android-next-gen/blob/main/app/src/main/java/com/github/wlara/nextgen/ui/home/HomeScreen.kt
@Composable
private fun RowScope.HomeBottomNavigationItem(
    selected: Boolean,
    @DrawableRes iconResId: Int,
    @StringRes labelResId: Int,
    selectedIconBrush: Brush,
    selectedLabelColor: Color,
    onClick: () -> Unit,
) {
    val label = stringResource(id = labelResId)
    BottomNavigationItem(
        icon = {
            Icon(
                modifier = if (selected) {
                    Modifier
                        .align(Alignment.CenterVertically)
                        .drawWithBrush(selectedIconBrush)
                } else {
                    Modifier.align(Alignment.CenterVertically)
                },
                painter = painterResource(id = iconResId),
                contentDescription = label,
            )
        },
        label = {
            Text(
                text = label,
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = if (selected) selectedLabelColor else Color.Unspecified
            )
        },
        selected = selected,
        onClick = onClick
    )
}
