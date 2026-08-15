package com.staffmate.app.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class AuthResult(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val user: AuthUser? = null,
    val error: String? = null,
    val error_description: String? = null,
    val msg: String? = null
)

@Serializable
data class AuthUser(val id: String, val email: String? = null)

sealed class AuthOutcome {
    object SignedIn : AuthOutcome()
    object ConfirmationRequired : AuthOutcome()
    data class Failure(val message: String) : AuthOutcome()
}

class SupabaseAuth(private val client: OkHttpClient, private val sessionStore: SessionStore) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMedia = "application/json".toMediaType()

    private fun baseHeaders(builder: Request.Builder) {
        builder.addHeader("apikey", SupabaseConfig.ANON_KEY)
        builder.addHeader("Content-Type", "application/json")
    }

    suspend fun signUp(email: String, password: String): AuthOutcome {
        val pair = call("${SupabaseConfig.AUTH_URL}/signup", """{"email":"$email","password":"$password"}""")
            ?: return AuthOutcome.Failure("خطا در ارتباط با سرور")
        val (response, parsed) = pair
        if (!response.isSuccessful) {
            return AuthOutcome.Failure(parsed.error_description ?: parsed.msg ?: parsed.error ?: "خطای ثبت‌نام (${response.code})")
        }
        return if (parsed.access_token != null && parsed.user != null) {
            sessionStore.save(parsed.access_token, parsed.refresh_token ?: "", parsed.user.id)
            AuthOutcome.SignedIn
        } else {
            AuthOutcome.ConfirmationRequired
        }
    }

    suspend fun signIn(email: String, password: String): AuthOutcome {
        val pair = call("${SupabaseConfig.AUTH_URL}/token?grant_type=password", """{"email":"$email","password":"$password"}""")
            ?: return AuthOutcome.Failure("خطا در ارتباط با سرور")
        val (response, parsed) = pair
        if (!response.isSuccessful || parsed.access_token == null || parsed.user == null) {
            return AuthOutcome.Failure(parsed.error_description ?: parsed.msg ?: parsed.error ?: "ایمیل یا رمز عبور اشتباه است")
        }
        sessionStore.save(parsed.access_token, parsed.refresh_token ?: "", parsed.user.id)
        return AuthOutcome.SignedIn
    }

    suspend fun signOut() {
        sessionStore.clear()
    }

    suspend fun currentSession(): String? = sessionStore.currentAccessToken()

    private fun call(url: String, body: String): Pair<okhttp3.Response, AuthResult>? {
        return try {
            val reqBuilder = Request.Builder().url(url).post(body.toRequestBody(jsonMedia))
            baseHeaders(reqBuilder)
            val response = client.newCall(reqBuilder.build()).execute()
            val text = response.body?.string().orEmpty()
            val parsed = try { json.decodeFromString<AuthResult>(text) } catch (e: Exception) { AuthResult(msg = text) }
            response to parsed
        } catch (e: Exception) {
            null
        }
    }
}
