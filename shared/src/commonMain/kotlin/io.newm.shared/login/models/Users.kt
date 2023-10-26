package io.newm.shared.login.models

import kotlinx.serialization.Serializable

@Serializable
data class LogInUser(
    val email: String,
    val password: String
)

@Serializable
data class GoogleSignInRequest(val accessToken: String)

@Serializable
data class AppleSignInRequest(val idToken: String)

@Serializable
data class FacebookSignInRequest(val accessToken: String)

@Serializable
data class LinkedInSignInRequest(val accessToken: String)

@Serializable
data class NewUser(
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val pictureUrl: String? = null,
    val email: String,
    val newPassword: String,
    val confirmPassword: String,
    val authCode: String
)
