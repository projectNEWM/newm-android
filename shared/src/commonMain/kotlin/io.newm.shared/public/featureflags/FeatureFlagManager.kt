package io.newm.shared.public.featureflags

import io.newm.shared.public.models.User

interface FeatureFlagManager {
    fun isEnabled(flag: FeatureFlag, default: Boolean = false): Boolean
    suspend fun setUser(user: User)
}