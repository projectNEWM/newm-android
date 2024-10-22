package io.newm.shared.public.featureflags

interface FeatureFlag {
    val key: String
}

object FeatureFlags {
    object MarketPlace : FeatureFlag {
        override val key = "streams-marketplace"
    }
}