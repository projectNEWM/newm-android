package io.newm.utils

import android.app.Application
import com.launchdarkly.sdk.ContextKind
import com.launchdarkly.sdk.LDContext
import com.launchdarkly.sdk.android.LDClient
import com.launchdarkly.sdk.android.LDConfig
import com.launchdarkly.sdk.android.LDConfig.Builder.AutoEnvAttributes
import io.newm.shared.config.NewmSharedBuildConfig
import io.newm.shared.public.featureflags.FeatureFlag
import io.newm.shared.public.featureflags.FeatureFlagManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Future

class AndroidFeatureFlagManager(
    private val application: Application,
    private val sharedBuildConfig: NewmSharedBuildConfig,
) : FeatureFlagManager {

    private val client: LDClient = buildClient()

    private fun buildClient(): LDClient {
        val context = LDContext.builder(ContextKind.DEFAULT, "anonymous")
            .anonymous(true)
            .build()

        val ldConfig: LDConfig = LDConfig.Builder(AutoEnvAttributes.Enabled)
            .mobileKey(sharedBuildConfig.launchDarklyKey)
            .build()

        return LDClient.init(application, ldConfig, context, 0)
    }

    override fun isEnabled(flag: FeatureFlag, default: Boolean): Boolean {
        return client.boolVariation(flag.key, default)
    }

    override suspend fun setUserId(id: String) {
        val ldContext = LDContext.builder(ContextKind.DEFAULT, id).build()

        client.identify(ldContext)
            .asDeferred()
            .await()
    }
}

private suspend fun <V> Future<V>.asDeferred(): Deferred<V> {
    val deferred = CompletableDeferred<V>()

    withContext(Dispatchers.IO) {
        deferred.complete(get())
    }

    return deferred
}
