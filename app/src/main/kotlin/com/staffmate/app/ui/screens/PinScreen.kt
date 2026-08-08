package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.ui.Routes
import com.staffmate.app.util.PinUtil
import kotlinx.coroutines.launch

@Composable
fun PinScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ورود PIN", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8) pin = it },
                label = { Text("کد PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                enabled = !locked
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !locked,
                onClick = {
                    scope.launch {
                        val storedHash = repository.getSetting("pin_hash", "")
                        if (storedHash.isNotEmpty() && PinUtil.matches(pin, storedHash)) {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.PIN) { inclusive = true }
                            }
                        } else {
                            attempts++
                            pin = ""
                            if (attempts >= 3) {
                                locked = true
                                error = "تعداد تلاش‌ها زیاد است. ۳۰ ثانیه صبر کنید."
                                kotlinx.coroutines.delay(30_000)
                                attempts = 0
                                locked = false
                                error = null
                            } else {
                                error = "PIN اشتباه است."
                            }
                        }
                    }
                }
            ) { Text("ورود") }
        }
    }
}
