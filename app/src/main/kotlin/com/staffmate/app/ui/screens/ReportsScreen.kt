package com.staffmate.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.ui.Routes
import com.staffmate.app.util.DateUtil
import kotlinx.coroutines.launch
import java.io.File

private data class ReportRow(val employeeName: String, val type: String, val title: String, val date: Long, val extra: String)

@Composable
fun ReportsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository
    val scope = rememberCoroutineScope()

    val employees by repository.employees.observeAll().collectAsState(initial = emptyList())
    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    var typeFilter by remember { mutableStateOf("همه") }

    var rows by remember { mutableStateOf<List<ReportRow>>(emptyList()) }

    suspend fun reload() {
        val emps = if (selectedEmployeeId != null) employees.filter { it.id == selectedEmployeeId } else employees
        rows = buildReportRows(repository, emps, typeFilter)
    }

    LaunchedEffect(selectedEmployeeId, typeFilter, employees) { reload() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("گزارش‌ها") }) },
        bottomBar = { BottomNavBar(navController, current = Routes.REPORTS) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            var empExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = empExpanded, onExpandedChange = { empExpanded = it }) {
                OutlinedTextField(
                    value = employees.find { it.id == selectedEmployeeId }?.let { "${it.firstName} ${it.lastName}" } ?: "همه پرسنل",
                    onValueChange = {}, readOnly = true, label = { Text("پرسنل") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = empExpanded, onDismissRequest = { empExpanded = false }) {
                    DropdownMenuItem(text = { Text("همه پرسنل") }, onClick = { selectedEmployeeId = null; empExpanded = false })
                    employees.forEach { emp ->
                        DropdownMenuItem(text = { Text("${emp.firstName} ${emp.lastName}") }, onClick = { selectedEmployeeId = emp.id; empExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            var typeExpanded by remember { mutableStateOf(false) }
            val types = listOf("همه", "مثبت", "منفی", "شخصیتی", "انضباطی")
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(
                    value = typeFilter, onValueChange = {}, readOnly = true, label = { Text("نوع مورد") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { typeFilter = t; typeExpanded = false }) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        val file = exportCsv(context, rows)
                        if (file != null) {
                            val uri = FileProvider.getUriForFile(context, "com.staffmate.app.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری گزارش CSV"))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Export CSV") }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(rows) { row ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp)) {
                            Text("${row.employeeName} — ${row.type}", style = MaterialTheme.typography.titleSmall)
                            Text("${row.title}   ${DateUtil.format(row.date)}   ${row.extra}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun buildReportRows(
    repository: com.staffmate.app.data.Repository,
    emps: List<com.staffmate.app.data.Employee>,
    typeFilter: String
): List<ReportRow> {
    val result = mutableListOf<ReportRow>()
    for (emp in emps) {
        val name = "${emp.firstName} ${emp.lastName}"
        if (typeFilter == "همه" || typeFilter == "مثبت") {
            repository.positives.getAllForEmployee(emp.id).forEach {
                result.add(ReportRow(name, "مثبت", it.title, it.date, "اهمیت: ${it.importance}"))
            }
        }
        if (typeFilter == "همه" || typeFilter == "منفی") {
            repository.negatives.getAllForEmployee(emp.id).forEach {
                result.add(ReportRow(name, "منفی", it.title, it.date, "${it.importance} / ${it.status}"))
            }
        }
        if (typeFilter == "همه" || typeFilter == "شخصیتی") {
            repository.personalities.getAllForEmployee(emp.id).forEach {
                result.add(ReportRow(name, "شخصیتی", it.title, it.date, it.type))
            }
        }
        if (typeFilter == "همه" || typeFilter == "انضباطی") {
            repository.disciplinary.getSince(emp.id, 0L).forEach {
                result.add(ReportRow(name, "انضباطی", it.violationType, it.date, "شدت: ${it.severity}"))
            }
        }
    }
    return result.sortedByDescending { it.date }
}

private fun exportCsv(context: android.content.Context, rows: List<ReportRow>): File? {
    return try {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "گزارش_${System.currentTimeMillis()}.csv")
        file.bufferedWriter().use { w ->
            w.write("پرسنل,نوع,عنوان,تاریخ,توضیحات\n")
            rows.forEach { r ->
                w.write("\"${r.employeeName}\",\"${r.type}\",\"${r.title}\",\"${DateUtil.format(r.date)}\",\"${r.extra}\"\n")
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
