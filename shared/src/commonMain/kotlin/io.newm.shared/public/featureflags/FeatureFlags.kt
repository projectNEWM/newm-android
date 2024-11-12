package io.newm.shared.public.featureflags

interface FeatureFlag {
    val key: String
}

object FeatureFlags {
    object ShowRecordStore : FeatureFlag {
        override val key = "mobile-app-show-recordstore"
    }
}