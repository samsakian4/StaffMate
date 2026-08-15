package com.staffmate.app.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "staffmate_auth")

/** Persists the Supabase auth session (access + refresh token) across app restarts. */
class SessionStore(private val context: Context) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")

    val accessToken: Flow<String?> = context.authDataStore.data.map { it[accessTokenKey] }
    val refreshToken: Flow<String?> = context.authDataStore.data.map { it[refreshTokenKey] }
    val userId: Flow<String?> = context.authDataStore.data.map { it[userIdKey] }

    suspend fun save(accessToken: String, refreshToken: String, userId: String) {
        context.authDataStore.edit {
            it[accessTokenKey] = accessToken
            it[refreshTokenKey] = refreshToken
            it[userIdKey] = userId
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }

    suspend fun currentAccessToken(): String? = accessToken.first()
    suspend fun currentRefreshToken(): String? = refreshToken.first()
    suspend fun currentUserId(): String? = userId.first()
}
