package com.staffmate.app

import android.app.Application
import com.staffmate.app.data.Repository
import com.staffmate.app.remote.SessionStore
import com.staffmate.app.remote.SupabaseApi
import com.staffmate.app.remote.SupabaseAuth
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class StaffMateApp : Application() {
    lateinit var sessionStore: SessionStore
        private set
    lateinit var auth: SupabaseAuth
        private set
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        sessionStore = SessionStore(this)
        auth = SupabaseAuth(client, sessionStore)
        val api = SupabaseApi(client, sessionStore)
        repository = Repository(api)
    }
}
