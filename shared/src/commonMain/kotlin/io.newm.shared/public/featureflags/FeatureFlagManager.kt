package io.newm.shared.public.featureflags

interface FeatureFlagManager {
    fun isEnabled(flag: FeatureFlag, default: Boolean = false): Boolean
    suspend fun setUserId(id: String)
}