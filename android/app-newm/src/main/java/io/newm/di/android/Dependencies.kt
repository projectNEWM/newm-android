package io.newm.di.android

import android.annotation.SuppressLint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import io.newm.Logout
import io.newm.RestartApp
import io.newm.feature.login.screen.authproviders.RecaptchaClientProvider
import io.newm.feature.login.screen.authproviders.google.GoogleSignInLauncher
import io.newm.feature.login.screen.authproviders.google.GoogleSignInLauncherImpl
import io.newm.feature.login.screen.createaccount.CreateAccountScreenPresenter
import io.newm.feature.login.screen.login.LoginScreenPresenter
import io.newm.feature.login.screen.resetpassword.ResetPasswordScreenPresenter
import io.newm.feature.login.screen.welcome.WelcomeScreenPresenter
import io.newm.feature.musicplayer.service.DownloadManager
import io.newm.feature.musicplayer.service.DownloadManagerImpl
import io.newm.screens.forceupdate.ForceAppUpdatePresenter
import io.newm.screens.library.NFTLibraryPresenter
import io.newm.screens.profile.edit.ProfileEditPresenter
import io.newm.screens.profile.view.ProfilePresenter
import io.newm.screens.recordstore.RecordStorePresenter
import io.newm.shared.config.NewmSharedBuildConfig
import io.newm.shared.public.featureflags.FeatureFlagManager
import io.newm.utils.AndroidFeatureFlagManager
import io.newm.utils.ForceAppUpdateViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@SuppressLint("UnsafeOptInUsageError")
val viewModule = module {
    single<FeatureFlagManager> { AndroidFeatureFlagManager(get(), get()) }
    single { ForceAppUpdateViewModel(get(), get()) }
    single { RecaptchaClientProvider() }

    factory { params -> CreateAccountScreenPresenter(params.get(), get(), get(), get(), get(), get()) }
    factory { params -> LoginScreenPresenter(params.get(), get(), get(), get(), get()) }
    factory { params ->
        ResetPasswordScreenPresenter(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get()
        , get())
    }
    single {
        val sharedBuildConfig = get<NewmSharedBuildConfig>()
        GoogleSignIn.getClient(
            androidContext(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(sharedBuildConfig.googleAuthClientId)
                .requestScopes(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE))
                .requestEmail()
                .build()
        )
    }
    single<GoogleSignInLauncher> { GoogleSignInLauncherImpl(get()) }
    factory { params ->
        WelcomeScreenPresenter(
            navigator = params.get(),
            googleSignInLauncher = get(),
            recaptchaClientProvider = get(),
            loginUseCase = get(),
            activityResultContract = ActivityResultContracts.StartActivityForResult(),
            logger = get(),
            analyticsTracker = get()
        )
    }
    factory { params ->
        ProfilePresenter(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory { params ->
        NFTLibraryPresenter(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory { params ->
        RecordStorePresenter(
            params.get(),
            get(),
        )
    }
    factory { params ->
        ProfileEditPresenter(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory { params ->
        ForceAppUpdatePresenter(
            params.get(),
        )
    }
    single<DatabaseProvider> { StandaloneDatabaseProvider(androidContext()) }
    single<Cache> {
        val downloadDirectory = androidContext().getExternalFilesDir(null)!!
        SimpleCache(downloadDirectory, NoOpCacheEvictor(), get())
    }
    single<DownloadManager> { DownloadManagerImpl(androidContext()) }
}

val androidModules = module {
    single { Logout(get(), get(), get(), get(), get(), get()) }
    single { RestartApp(get()) }
}
