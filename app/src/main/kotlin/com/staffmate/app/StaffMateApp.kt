package com.staffmate.app

import android.app.Application
import com.staffmate.app.data.Repository
import com.staffmate.app.data.StaffMateDatabase
import com.staffmate.app.data.ensureDefaultSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StaffMateApp : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = StaffMateDatabase.getInstance(this)
        repository = Repository(db)
        CoroutineScope(Dispatchers.IO).launch {
            db.ensureDefaultSettings()
        }
    }
}
