package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.data.Employee
import com.staffmate.app.ui.Routes

@Composable
fun QuickAddScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository
    val employees by repository.employees.observeActive().collectAsState(initial = emptyList())

    var selected by remember { mutableStateOf<Employee?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ثبت مورد جدید") }) },
        bottomBar = { BottomNavBar(navController, current = Routes.QUICK_ADD) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selected?.let { "${it.firstName} ${it.lastName} (${it.personnelCode})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("انتخاب پرسنل") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    employees.forEach { emp ->
                        DropdownMenuItem(
                            text = { Text("${emp.firstName} ${emp.lastName} (${emp.personnelCode})") },
                            onClick = { selected = emp; expanded = false }
                        )
                    }
                }
            }

            Text("نوع مورد را انتخاب کنید:", style = MaterialTheme.typography.titleMedium)

            val empId = selected?.id
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = empId != null, onClick = { navController.navigate(Routes.noteForm("positive", empId!!)) }, modifier = Modifier.weight(1f)) { Text("مثبت") }
                Button(enabled = empId != null, onClick = { navController.navigate(Routes.noteForm("negative", empId!!)) }, modifier = Modifier.weight(1f)) { Text("منفی") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = empId != null, onClick = { navController.navigate(Routes.noteForm("personality", empId!!)) }, modifier = Modifier.weight(1f)) { Text("شخصیتی") }
                Button(enabled = empId != null, onClick = { navController.navigate(Routes.noteForm("disciplinary", empId!!)) }, modifier = Modifier.weight(1f)) { Text("انضباطی") }
            }
        }
    }
}
