package com.staffmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.staffmate.app.ui.Routes
import com.staffmate.app.ui.StaffMateNavHost
import com.staffmate.app.ui.StaffMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as StaffMateApp
        setContent {
            StaffMateTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var startDestination by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(Unit) {
                        val token = app.sessionStore.currentAccessToken()
                        if (token.isNullOrBlank()) {
                            startDestination = Routes.LOGIN
                        } else {
                            try {
                                app.repository.refreshAll()
                                app.repository.ensureDefaultSettings()
                                val pinEnabled = app.repository.getSetting("pin_enabled", "0") == "1"
                                startDestination = if (pinEnabled) Routes.PIN else Routes.DASHBOARD
                            } catch (e: Exception) {
                                // Session likely expired/invalid — send the user back to login.
                                app.sessionStore.clear()
                                startDestination = Routes.LOGIN
                            }
                        }
                    }
                    val dest = startDestination
                    if (dest == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        StaffMateNavHost(startDestination = dest)
                    }
                }
            }
        }
    }
}
