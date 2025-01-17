package io.newm

import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader

class DebugNewmApplication : NewmApplication() {
    override fun onCreate() {
        SoLoader.init(this, false)
        AndroidFlipperClient.getInstance(this).apply {
            addPlugin(
                InspectorFlipperPlugin(
                    this@DebugNewmApplication,
                    DescriptorMapping.withDefaults()
                )
            )
            addPlugin(CrashReporterPlugin.getInstance())
            addPlugin(DatabasesFlipperPlugin(this@DebugNewmApplication))
            addPlugin(NavigationFlipperPlugin.getInstance())
        }.start()

        super.onCreate()
    }
}