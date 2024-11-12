package io.newm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.continuityRetainedStateRegistry
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import io.newm.core.theme.NewmTheme
import io.newm.screens.Screen
import io.newm.screens.Screen.NFTLibrary
import io.newm.screens.forceupdate.ForceAppUpdatePresenter
import io.newm.screens.forceupdate.ForceAppUpdateState
import io.newm.screens.forceupdate.ForceAppUpdateUi
import io.newm.screens.forceupdate.openAppPlayStore
import io.newm.screens.library.NFTLibraryPresenter
import io.newm.screens.library.NFTLibraryScreenUi
import io.newm.screens.library.NFTLibraryState
import io.newm.screens.profile.edit.ProfileEditPresenter
import io.newm.screens.profile.edit.ProfileEditUi
import io.newm.screens.profile.edit.ProfileEditUiState
import io.newm.screens.recordstore.RecordStorePresenter
import io.newm.screens.recordstore.RecordStoreScreenUi
import io.newm.screens.recordstore.RecordStoreState
import io.newm.screens.profile.view.ProfilePresenter
import io.newm.screens.profile.view.ProfileUi
import io.newm.screens.profile.view.ProfileUiState
import io.newm.shared.NewmAppLogger
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.featureflags.FeatureFlagManager
import io.newm.shared.public.featureflags.FeatureFlags
import io.newm.utils.ForceAppUpdateViewModel
import io.newm.utils.ui
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class HomeActivity : ComponentActivity() {
    private val circuit: Circuit = createCircuit()
    private val logger: NewmAppLogger by inject()
    private val forceAppUpdateViewModel: ForceAppUpdateViewModel by inject()
    private val eventLogger: NewmAppEventLogger by inject()
    private val featureFlagManager : FeatureFlagManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        setContent {
            NewmTheme(darkTheme = true) {
                CircuitDependencies {
                    val updateRequired by forceAppUpdateViewModel.updateRequiredState.collectAsState()
                    if (updateRequired) {
                        ForceAppUpdateUi(
                            state = ForceAppUpdateState.Content(eventSink = {
                                eventLogger.logClickEvent(AppScreens.ForceUpdateScreen.UPDATE_BUTTON)
                                openAppPlayStore()
                            }),
                            eventLogger
                        )
                    } else {
                        NewmApp(
                            logger = logger,
                            eventLogger = eventLogger,
                            showRecordStore = featureFlagManager.isEnabled(FeatureFlags.ShowRecordStore))
                    }
                }
            }
        }
    }

    private fun createCircuit(): Circuit {
        return Circuit.Builder()
            .addPresenterFactory(buildPresenterFactory())
            .addUiFactory(buildUiFactory())
            .build()
    }

    private fun buildUiFactory(): Ui.Factory {
        return Ui.Factory { screen, _ ->
            when (screen) {
                is Screen.UserAccount -> ui<ProfileUiState> { state, modifier ->
                    ProfileUi(
                        state = state,
                        modifier = modifier,
                        eventLogger = eventLogger
                    )
                }

                is Screen.RecordStore -> ui<RecordStoreState> { state, modifier ->
                    RecordStoreScreenUi(
                        state = state,
                        modifier = modifier,
                        eventLogger = eventLogger
                    )
                }

                is NFTLibrary -> ui<NFTLibraryState> { state, modifier ->
                    NFTLibraryScreenUi(
                        state = state,
                        modifier = modifier,
                        eventLogger = eventLogger
                    )
                }

                is Screen.EditProfile -> ui<ProfileEditUiState> { state, modifier ->
                    ProfileEditUi(
                        state = state,
                        modifier = modifier,
                        eventLogger = eventLogger
                    )
                }

                is Screen.ForceAppUpdate -> ui<ForceAppUpdateState> { state, modifier ->
                    ForceAppUpdateUi(
                        state = state,
                        modifier = modifier,
                        eventLogger = eventLogger
                    )
                }

                else -> null

            }
        }
    }

    private fun buildPresenterFactory(): Presenter.Factory {
        return Presenter.Factory { screen, navigator, _ ->
            when (screen) {
                is Screen.UserAccount -> inject<ProfilePresenter> {
                    parametersOf(
                        navigator
                    )
                }.value

                is NFTLibrary -> inject<NFTLibraryPresenter> {
                    parametersOf(
                        navigator
                    )
                }.value

                is Screen.RecordStore -> inject<RecordStorePresenter> {
                    parametersOf(
                        navigator
                    )
                }.value

                is Screen.EditProfile -> inject<ProfileEditPresenter> {
                    parametersOf(
                        navigator
                    )
                }.value

                is Screen.ForceAppUpdate -> inject<ForceAppUpdatePresenter> {
                    parametersOf(
                        navigator
                    )
                }.value

                else -> null
            }
        }
    }

    @Composable
    fun CircuitDependencies(
        content: @Composable () -> Unit
    ) {
        CircuitCompositionLocals(circuit) {
            CompositionLocalProvider(
                LocalRetainedStateRegistry provides continuityRetainedStateRegistry(),
                LocalIsBottomBarVisible provides isBottomBarVisible()
            ) {
                content()
            }
        }
    }
}