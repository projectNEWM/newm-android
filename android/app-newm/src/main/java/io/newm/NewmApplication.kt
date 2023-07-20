package io.newm

import android.app.Application
import co.touchlab.kermit.Logger
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import io.newm.di.android.androidModules
import io.newm.di.android.viewModule
import io.newm.shared.di.initKoin
import org.koin.android.ext.koin.androidContext


//@ShowkaseRoot
class NewmApplication : Application() { //}, ShowkaseRootModule {

    override fun onCreate() {
        super.onCreate()
        devtoolsSetup()

        initKoin(enableNetworkLogs = true) {
            androidContext(this@NewmApplication)
            modules(viewModule, androidModules)
        }

        Logger.d { "NewmAndroid - NewmApplication" }
    }

    private fun devtoolsSetup() {
        SoLoader.init(this, false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.addPlugin(CrashReporterPlugin.getInstance())
            client.addPlugin(DatabasesFlipperPlugin(this))
            client.addPlugin(NavigationFlipperPlugin.getInstance())
            client.addPlugin(SharedPreferencesFlipperPlugin(this, "newm_encrypted_shared_prefs"))
            client.start()
        }
    }
}
