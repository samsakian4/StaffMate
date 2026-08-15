package com.staffmate.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.data.BackupData
import com.staffmate.app.ui.Routes
import com.staffmate.app.util.PinUtil
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val backupJson = Json { prettyPrint = true; ignoreUnknownKeys = true }

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as StaffMateApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var weightPositive by remember { mutableStateOf("1") }
    var weightNegative by remember { mutableStateOf("-1") }
    var sevLow by remember { mutableStateOf("-1") }
    var sevMed by remember { mutableStateOf("-3") }
    var sevHigh by remember { mutableStateOf("-5") }
    var sevVeryHigh by remember { mutableStateOf("-8") }

    var shiftsList by remember { mutableStateOf("") }
    var positionsList by remember { mutableStateOf("") }
    var workplacesList by remember { mutableStateOf("") }
    var violationTypesList by remember { mutableStateOf("") }

    var pinEnabled by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var lastBackup by remember { mutableStateOf("-") }
    var message by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    suspend fun loadAll() {
        weightPositive = repository.getSetting("weight_positive", "1")
        weightNegative = repository.getSetting("weight_negative", "-1")
        sevLow = repository.getSetting("severity_low", "-1")
        sevMed = repository.getSetting("severity_medium", "-3")
        sevHigh = repository.getSetting("severity_high", "-5")
        sevVeryHigh = repository.getSetting("severity_very_high", "-8")
        shiftsList = repository.getSetting("shifts_list", "")
        positionsList = repository.getSetting("positions_list", "")
        workplacesList = repository.getSetting("workplaces_list", "")
        violationTypesList = repository.getSetting("violation_types_list", "")
        pinEnabled = repository.getSetting("pin_enabled", "0") == "1"
        lastBackup = repository.getSetting("last_backup_date", "-").ifBlank { "-" }
    }

    LaunchedEffect(Unit) { loadAll() }

    val createBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val data = repository.exportAllData()
                    val text = backupJson.encodeToString(BackupData.serializer(), data)
                    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                    val now = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date())
                    repository.setSetting("last_backup_date", now)
                    lastBackup = now
                    message = "Backup با موفقیت ذخیره شد."
                } catch (e: Exception) {
                    message = "خطا در ذخیره Backup."
                }
            }
        }
    }

    val openRestoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("تنظیمات") }) },
        bottomBar = { BottomNavBar(navController, current = Routes.SETTINGS) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("امتیازدهی", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(weightPositive, { weightPositive = it }, label = { Text("وزن نکته مثبت") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(weightNegative, { weightNegative = it }, label = { Text("وزن نکته منفی") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sevLow, { sevLow = it }, label = { Text("وزن شدت کم") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sevMed, { sevMed = it }, label = { Text("وزن شدت متوسط") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sevHigh, { sevHigh = it }, label = { Text("وزن شدت زیاد") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sevVeryHigh, { sevVeryHigh = it }, label = { Text("وزن شدت بسیار زیاد") }, modifier = Modifier.fillMaxWidth())

            Divider()
            Text("لیست‌های قابل تنظیم (با کاما جدا کنید)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(shiftsList, { shiftsList = it }, label = { Text("شیفت‌ها") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(positionsList, { positionsList = it }, label = { Text("سمت‌ها") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(workplacesList, { workplacesList = it }, label = { Text("محل‌های کاری") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(violationTypesList, { violationTypesList = it }, label = { Text("انواع تخلف") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    scope.launch {
                        repository.setSetting("weight_positive", weightPositive)
                        repository.setSetting("weight_negative", weightNegative)
                        repository.setSetting("severity_low", sevLow)
                        repository.setSetting("severity_medium", sevMed)
                        repository.setSetting("severity_high", sevHigh)
                        repository.setSetting("severity_very_high", sevVeryHigh)
                        repository.setSetting("shifts_list", shiftsList)
                        repository.setSetting("positions_list", positionsList)
                        repository.setSetting("workplaces_list", workplacesList)
                        repository.setSetting("violation_types_list", violationTypesList)
                        message = "تنظیمات ذخیره شد."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ذخیره تنظیمات") }

            Divider()
            Text("امنیت", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = pinEnabled, onCheckedChange = { checked ->
                    pinEnabled = checked
                    scope.launch { repository.setSetting("pin_enabled", if (checked) "1" else "0") }
                })
                Text("فعال‌سازی قفل PIN")
            }
            if (pinEnabled) {
                OutlinedTextField(newPin, { if (it.length <= 8) newPin = it }, label = { Text("PIN جدید") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (newPin.isNotBlank()) {
                            scope.launch {
                                repository.setSetting("pin_hash", PinUtil.hash(newPin))
                                newPin = ""
                                message = "PIN ذخیره شد."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ذخیره PIN") }
            }

            Divider()
            Text("Backup / Restore", style = MaterialTheme.typography.titleMedium)
            Text("این Backup از داده‌های آنلاین (Supabase) گرفته می‌شود؛ فایل خروجی با نسخه وب هم سازگار است.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("آخرین Backup: $lastBackup")
            Button(
                onClick = {
                    val stamp = java.text.SimpleDateFormat("yyyy-MM-dd_HHmm", java.util.Locale.US).format(java.util.Date())
                    createBackupLauncher.launch("StaffMate_Backup_$stamp.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("تهیه Backup") }
            OutlinedButton(
                onClick = { openRestoreLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("بازیابی از Backup") }

            message?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Divider()
            OutlinedButton(
                onClick = { showSignOutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text("خروج از حساب") }
        }

        if (showRestoreConfirm && pendingRestoreUri != null) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirm = false },
                title = { Text("هشدار بازیابی") },
                text = { Text("تمام اطلاعات آنلاین فعلی با اطلاعات فایل Backup جایگزین می‌شود. ادامه می‌دهید؟") },
                confirmButton = {
                    TextButton(onClick = {
                        val uri = pendingRestoreUri!!
                        showRestoreConfirm = false
                        scope.launch {
                            try {
                                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                                if (text.isNullOrBlank()) {
                                    message = "فایل Backup نامعتبر یا خراب است."
                                    return@launch
                                }
                                val data = backupJson.decodeFromString(BackupData.serializer(), text)
                                repository.importAllData(data)
                                message = "بازیابی با موفقیت انجام شد."
                            } catch (e: Exception) {
                                message = "فایل Backup نامعتبر یا خراب است."
                            }
                        }
                    }) { Text("ادامه") }
                },
                dismissButton = { TextButton(onClick = { showRestoreConfirm = false }) { Text("انصراف") } }
            )
        }

        if (showSignOutConfirm) {
            AlertDialog(
                onDismissRequest = { showSignOutConfirm = false },
                title = { Text("خروج از حساب") },
                text = { Text("از حساب کاربری خارج می‌شوید. برای ورود مجدد به رمز عبور نیاز دارید.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSignOutConfirm = false
                        scope.launch {
                            app.auth.signOut()
                            navController.navigate(Routes.LOGIN) { popUpTo(0) }
                        }
                    }) { Text("خروج") }
                },
                dismissButton = { TextButton(onClick = { showSignOutConfirm = false }) { Text("انصراف") } }
            )
        }
    }
}
