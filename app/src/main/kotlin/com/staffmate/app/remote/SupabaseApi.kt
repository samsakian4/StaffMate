package com.staffmate.app.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SupabaseApiException(message: String) : Exception(message)

/**
 * Thin REST client for Supabase's auto-generated PostgREST API.
 * Every call is scoped to the signed-in user automatically via RLS
 * (owner_id = auth.uid()), using the caller's access token.
 */
class SupabaseApi(private val client: OkHttpClient, private val sessionStore: SessionStore) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMedia = "application/json".toMediaType()

    private suspend fun authHeader(builder: Request.Builder) {
        val token = sessionStore.currentAccessToken() ?: throw SupabaseApiException("ابتدا وارد حساب کاربری شوید.")
        builder.addHeader("apikey", SupabaseConfig.ANON_KEY)
        builder.addHeader("Authorization", "Bearer $token")
        builder.addHeader("Content-Type", "application/json")
    }

    suspend fun select(table: String, query: String = ""): JsonArray = withContext(Dispatchers.IO) {
        val url = "${SupabaseConfig.REST_URL}/$table?select=*$query"
        val builder = Request.Builder().url(url).get()
        authHeader(builder)
        execute(builder).jsonArray
    }

    suspend fun insert(table: String, body: JsonObject): JsonObject = withContext(Dispatchers.IO) {
        val url = "${SupabaseConfig.REST_URL}/$table"
        val builder = Request.Builder().url(url)
            .post(body.toString().toRequestBody(jsonMedia))
            .addHeader("Prefer", "return=representation")
        authHeader(builder)
        execute(builder).jsonArray.first().jsonObject
    }

    suspend fun upsert(table: String, body: JsonObject, onConflict: String): JsonObject = withContext(Dispatchers.IO) {
        val url = "${SupabaseConfig.REST_URL}/$table?on_conflict=$onConflict"
        val builder = Request.Builder().url(url)
            .post(body.toString().toRequestBody(jsonMedia))
            .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
        authHeader(builder)
        execute(builder).jsonArray.first().jsonObject
    }

    suspend fun update(table: String, idColumn: String, idValue: String, body: JsonObject): Unit = withContext(Dispatchers.IO) {
        val url = "${SupabaseConfig.REST_URL}/$table?$idColumn=eq.$idValue"
        val builder = Request.Builder().url(url)
            .patch(body.toString().toRequestBody(jsonMedia))
        authHeader(builder)
        execute(builder)
        Unit
    }

    suspend fun delete(table: String, idColumn: String, idValue: String): Unit = withContext(Dispatchers.IO) {
        val url = "${SupabaseConfig.REST_URL}/$table?$idColumn=eq.$idValue"
        val builder = Request.Builder().url(url).delete()
        authHeader(builder)
        execute(builder)
        Unit
    }

    suspend fun deleteAll(table: String): Unit = withContext(Dispatchers.IO) {
        // owner_id is always non-null, so this condition matches every row the RLS policy already limits to this user.
        val url = "${SupabaseConfig.REST_URL}/$table?owner_id=not.is.null"
        val builder = Request.Builder().url(url).delete()
        authHeader(builder)
        execute(builder)
        Unit
    }

    private fun execute(builder: Request.Builder): JsonElement {
        val response = client.newCall(builder.build()).execute()
        val text = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            val message = try {
                json.parseToJsonElement(text).jsonObject["message"]?.toString()?.trim('"')
            } catch (e: Exception) { null }
            throw SupabaseApiException(message ?: "خطای سرور (${response.code})")
        }
        if (text.isBlank()) return JsonArray(emptyList())
        return json.parseToJsonElement(text)
    }
}
