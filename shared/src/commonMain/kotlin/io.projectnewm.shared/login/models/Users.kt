package io.projectnewm.shared.login.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LogInUser(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

data class NewUser(
    val firstName: String? = null,
    val lastName: String? = null,
    val pictureUrl: String? = null,
    val email: String,
    val newPassword: String,
    val confirmPassword: String,
    val authCode: String
)