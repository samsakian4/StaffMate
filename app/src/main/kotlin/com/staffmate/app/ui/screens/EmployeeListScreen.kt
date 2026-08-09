package com.staffmate.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.data.Employee
import com.staffmate.app.ui.Routes
import com.staffmate.app.ui.components.EmptyState

@Composable
fun EmployeeListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository

    val prefilled = navController.previousBackStackEntry?.savedStateHandle?.get<String>("prefilledQuery")
    var query by remember { mutableStateOf(prefilled ?: "") }
    var onlyActive by remember { mutableStateOf(true) }

    val list by repository.employees.search(query, if (onlyActive) 1 else 0)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("پرسنل") }) },
        bottomBar = { BottomNavBar(navController, current = Routes.EMPLOYEES) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.employeeForm(-1L)) }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن پرسنل")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("جست‌وجو (نام یا کد پرسنلی)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = onlyActive, onCheckedChange = { onlyActive = it })
                Text("فقط پرسنل فعال")
            }
            Spacer(Modifier.height(8.dp))
            if (list.isEmpty()) {
                EmptyState(
                    title = if (query.isNotBlank()) "نتیجه‌ای یافت نشد" else "هنوز پرسنلی ثبت نشده",
                    subtitle = if (query.isNotBlank()) "عبارت جست‌وجو را تغییر دهید" else "با دکمه + یک پرسنل جدید اضافه کنید"
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { emp ->
                    EmployeeRow(emp) { navController.navigate(Routes.employeeProfile(emp.id)) }
                }
            }
        }
    }
}

@Composable
private fun EmployeeRow(emp: Employee, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${emp.firstName} ${emp.lastName}", style = MaterialTheme.typography.titleMedium)
            Text("کد: ${emp.personnelCode}  |  ${emp.position}  |  ${emp.shift}", style = MaterialTheme.typography.bodySmall)
            if (!emp.active) {
                Text("غیرفعال", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
