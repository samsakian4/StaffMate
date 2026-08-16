package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.remote.AuthOutcome
import com.staffmate.app.ui.Routes
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as StaffMateApp
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun doSignIn() {
        if (loading) return
        error = null; info = null; loading = true
        scope.launch {
            when (val result = app.auth.signIn(email.trim(), password)) {
                is AuthOutcome.SignedIn -> {
                    app.repository.refreshAll()
                    app.repository.ensureDefaultSettings()
                    navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
                is AuthOutcome.Failure -> error = result.message
                AuthOutcome.ConfirmationRequired -> error = "ابتدا ایمیل خود را تأیید کنید."
            }
            loading = false
        }
    }

    fun doSignUp() {
        if (loading) return
        error = null; info = null
        if (email.isBlank() || password.length < 6) {
            error = "ایمیل معتبر و رمز حداقل ۶ کاراکتری وارد کنید."
            return
        }
        loading = true
        scope.launch {
            when (val result = app.auth.signUp(email.trim(), password)) {
                is AuthOutcome.SignedIn -> {
                    app.repository.refreshAll()
                    app.repository.ensureDefaultSettings()
                    navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
                AuthOutcome.ConfirmationRequired -> info = "حساب ساخته شد. ایمیل خود را برای لینک تأیید بررسی کنید، سپس وارد شوید."
                is AuthOutcome.Failure -> error = result.message
            }
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 360.dp)) {
            Text("پرسنل‌یار", style = MaterialTheme.typography.headlineMedium)
            Text("ورود برای همگام‌سازی داده روی همه دستگاه‌ها", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("ایمیل") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("رمز عبور") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    doSignIn()
                }),
                modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester)
            )
            error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
            info?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !loading,
                onClick = { doSignIn() },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "..." else "ورود") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                enabled = !loading,
                onClick = { doSignUp() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ساخت حساب جدید (فقط بار اول)") }
        }
    }
}
