package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.data.Employee
import kotlinx.coroutines.launch

@Composable
fun EmployeeFormScreen(navController: NavHostController, employeeId: Long) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository
    val scope = rememberCoroutineScope()
    val isNew = employeeId <= 0L

    var code by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var workplace by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(isNew) }

    LaunchedEffect(employeeId) {
        if (!isNew) {
            repository.employees.getById(employeeId)?.let { emp ->
                code = emp.personnelCode
                firstName = emp.firstName
                lastName = emp.lastName
                position = emp.position
                workplace = emp.workplace
                shift = emp.shift
                active = emp.active
            }
            loaded = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (isNew) "افزودن پرسنل" else "ویرایش پرسنل") }) }) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(code, { code = it }, label = { Text("کد پرسنلی *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(firstName, { firstName = it }, label = { Text("نام *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(lastName, { lastName = it }, label = { Text("نام خانوادگی *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(position, { position = it }, label = { Text("سمت") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(workplace, { workplace = it }, label = { Text("محل کار") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(shift, { shift = it }, label = { Text("شیفت") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = active, onCheckedChange = { active = it })
                Text("فعال")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    if (code.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                        error = "کد پرسنلی، نام و نام خانوادگی الزامی است."
                        return@Button
                    }
                    scope.launch {
                        try {
                            if (isNew) {
                                repository.employees.insert(
                                    Employee(
                                        personnelCode = code.trim(),
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        position = position.trim(),
                                        workplace = workplace.trim(),
                                        shift = shift.trim(),
                                        active = active
                                    )
                                )
                            } else {
                                repository.employees.update(
                                    Employee(
                                        id = employeeId,
                                        personnelCode = code.trim(),
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        position = position.trim(),
                                        workplace = workplace.trim(),
                                        shift = shift.trim(),
                                        active = active,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                            navController.popBackStack()
                            Toast.makeText(context, if (isNew) "پرسنل ثبت شد." else "تغییرات ذخیره شد.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            error = "کد پرسنلی تکراری است یا خطایی رخ داد."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ذخیره") }
        }
    }
}
