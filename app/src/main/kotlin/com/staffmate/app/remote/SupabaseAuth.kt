package com.staffmate.app.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

private data class CallResult(val response: okhttp3.Response?, val parsed: AuthResult?, val exceptionMessage: String?)

class SupabaseAuth(private val client: OkHttpClient, private val sessionStore: SessionStore) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMedia = "application/json".toMediaType()

    private fun baseHeaders(builder: Request.Builder) {
        builder.addHeader("apikey", SupabaseConfig.ANON_KEY)
        builder.addHeader("Content-Type", "application/json")
    }

    suspend fun signUp(email: String, password: String): AuthOutcome {
        val result = call("${SupabaseConfig.AUTH_URL}/signup", """{"email":"$email","password":"$password"}""")
        if (result.response == null || result.parsed == null) {
            return AuthOutcome.Failure("خطا در ارتباط با سرور: ${result.exceptionMessage ?: "نامشخص"}")
        }
        if (!result.response.isSuccessful) {
            return AuthOutcome.Failure(result.parsed.error_description ?: result.parsed.msg ?: result.parsed.error ?: "خطای ثبت‌نام (${result.response.code})")
        }
        return if (result.parsed.access_token != null && result.parsed.user != null) {
            sessionStore.save(result.parsed.access_token, result.parsed.refresh_token ?: "", result.parsed.user.id)
            AuthOutcome.SignedIn
        } else {
            AuthOutcome.ConfirmationRequired
        }
    }

    suspend fun signIn(email: String, password: String): AuthOutcome {
        val result = call("${SupabaseConfig.AUTH_URL}/token?grant_type=password", """{"email":"$email","password":"$password"}""")
        if (result.response == null || result.parsed == null) {
            return AuthOutcome.Failure("خطا در ارتباط با سرور: ${result.exceptionMessage ?: "نامشخص"}")
        }
        if (!result.response.isSuccessful || result.parsed.access_token == null || result.parsed.user == null) {
            return AuthOutcome.Failure(result.parsed.error_description ?: result.parsed.msg ?: result.parsed.error ?: "ایمیل یا رمز عبور اشتباه است")
        }
        sessionStore.save(result.parsed.access_token, result.parsed.refresh_token ?: "", result.parsed.user.id)
        return AuthOutcome.SignedIn
    }

    suspend fun signOut() {
        sessionStore.clear()
    }

    suspend fun currentSession(): String? = sessionStore.currentAccessToken()

    private suspend fun call(url: String, body: String): CallResult = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url(url).post(body.toRequestBody(jsonMedia))
            baseHeaders(reqBuilder)
            val response = client.newCall(reqBuilder.build()).execute()
            val text = response.body?.string().orEmpty()
            val parsed = try { json.decodeFromString<AuthResult>(text) } catch (e: Exception) { AuthResult(msg = text) }
            CallResult(response, parsed, null)
        } catch (e: Exception) {
            CallResult(null, null, e.message ?: e.javaClass.simpleName)
        }
    }
}
