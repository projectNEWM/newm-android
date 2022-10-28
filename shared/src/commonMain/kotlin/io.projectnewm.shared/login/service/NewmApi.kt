package io.projectnewm.shared.login.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.projectnewm.shared.HttpRoutes
import io.projectnewm.shared.login.models.*
import io.projectnewm.shared.login.models.LogInUser
import org.koin.core.component.KoinComponent


class NewmApi(
    private val client: HttpClient,
    var baseUrl: String = HttpRoutes.HOST,
) : KoinComponent {

    suspend fun requestEmailConfirmationCode(email: String): RequestEmailStatus {
        val response = client.get("$baseUrl/v1/auth/code") {
            headers {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            parameter("email", email)
        }

        return when (response.status.value) {
            204 -> {
                RequestEmailStatus.Success
            }
            else -> {
                RequestEmailStatus.Failure
            }
        }
    }

    suspend fun register(user: NewUser): RegisterStatus {
        val response = client.put("$baseUrl/v1/users") {
            headers {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            setBody(user)
        }
        return when (response.status.value) {
            204 -> {
                RegisterStatus.Success
            }
            409 -> {
                RegisterStatus.UserAlreadyExists
            }
            403 -> {
                RegisterStatus.TwoFactorAuthenticationFailed
            }
            else -> {
                RegisterStatus.UnknownError
            }
        }
    }

    suspend fun logIn(user: LogInUser) = client.post("$baseUrl/v1/auth/login") {
        headers {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
        setBody(user)
    }.body<LoginResponse>()
}