package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.data.*
import com.staffmate.app.ui.Routes
import com.staffmate.app.ui.components.EmptyState
import com.staffmate.app.util.DateUtil

private enum class ProfileTab(val label: String) {
    POSITIVE("مثبت"), NEGATIVE("منفی"), PERSONALITY("شخصیتی"), DISCIPLINARY("انضباطی")
}

@Composable
fun EmployeeProfileScreen(navController: NavHostController, employeeId: Long) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository

    val employee by repository.employees.observeById(employeeId).collectAsState(initial = null)
    val positives by repository.positives.observeForEmployee(employeeId).collectAsState(initial = emptyList())
    val negatives by repository.negatives.observeForEmployee(employeeId).collectAsState(initial = emptyList())
    val personalities by repository.personalities.observeForEmployee(employeeId).collectAsState(initial = emptyList())
    val disciplinaries by repository.disciplinary.observeForEmployee(employeeId).collectAsState(initial = emptyList())

    var tab by remember { mutableStateOf(ProfileTab.POSITIVE) }
    var score by remember { mutableStateOf<Int?>(null) }
    var totalRecords by remember { mutableStateOf(0) }

    LaunchedEffect(positives, negatives, disciplinaries) {
        score = repository.calculateScore(employeeId)
        totalRecords = repository.totalRecordCount(employeeId)
    }

    val emp = employee
    Scaffold(
        topBar = { TopAppBar(title = { Text(emp?.let { "${it.firstName} ${it.lastName}" } ?: "پروفایل") }) },
        floatingActionButton = {
            val type = when (tab) {
                ProfileTab.POSITIVE -> "positive"
                ProfileTab.NEGATIVE -> "negative"
                ProfileTab.PERSONALITY -> "personality"
                ProfileTab.DISCIPLINARY -> "disciplinary"
            }
            FloatingActionButton(onClick = { navController.navigate(Routes.noteForm(type, employeeId)) }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن سابقه")
            }
        }
    ) { padding ->
        if (emp == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("کد: ${emp.personnelCode}  |  ${emp.position}  |  ${emp.workplace}  |  ${emp.shift}")
                Spacer(Modifier.height(8.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("خلاصه عملکرد", style = MaterialTheme.typography.titleMedium)
                        if (totalRecords < 3) {
                            Text("اطلاعات کافی برای ارزیابی وجود ندارد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val s = score ?: 0
                            val scoreColor = when {
                                s > 0 -> com.staffmate.app.ui.ScoreColors.positive
                                s < 0 -> com.staffmate.app.ui.ScoreColors.negative
                                else -> com.staffmate.app.ui.ScoreColors.neutral
                            }
                            Text(
                                "امتیاز فعلی: $s",
                                style = MaterialTheme.typography.titleMedium,
                                color = scoreColor
                            )
                        }
                        Text("مثبت: ${positives.size}   منفی: ${negatives.size}   شخصیتی: ${personalities.size}   انضباطی: ${disciplinaries.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            ScrollableTabRow(selectedTabIndex = tab.ordinal) {
                ProfileTab.values().forEach { t ->
                    Tab(selected = tab == t, onClick = { tab = t }, text = { Text("${t.label} (${countFor(t, positives.size, negatives.size, personalities.size, disciplinaries.size)})") })
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    ProfileTab.POSITIVE -> if (positives.isEmpty()) {
                        EmptyState("هنوز نکته مثبتی ثبت نشده", "با دکمه + یک مورد اضافه کنید")
                    } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(positives, key = { it.id }) { note ->
                            NoteRow(title = note.title, subtitle = "${DateUtil.format(note.date)} · اهمیت: ${note.importance}", body = note.description) {
                                navController.navigate(Routes.noteForm("positive", employeeId, note.id))
                            }
                        }
                    }
                    ProfileTab.NEGATIVE -> if (negatives.isEmpty()) {
                        EmptyState("هنوز نکته منفی ثبت نشده", "با دکمه + یک مورد اضافه کنید")
                    } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(negatives, key = { it.id }) { note ->
                            NoteRow(title = note.title, subtitle = "${DateUtil.format(note.date)} · اهمیت: ${note.importance} · ${note.status}", body = note.description) {
                                navController.navigate(Routes.noteForm("negative", employeeId, note.id))
                            }
                        }
                    }
                    ProfileTab.PERSONALITY -> if (personalities.isEmpty()) {
                        EmptyState("هنوز ویژگی شخصیتی ثبت نشده", "با دکمه + یک مورد اضافه کنید")
                    } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(personalities, key = { it.id }) { note ->
                            NoteRow(title = "${note.title} (${note.type})", subtitle = DateUtil.format(note.date), body = note.description) {
                                navController.navigate(Routes.noteForm("personality", employeeId, note.id))
                            }
                        }
                    }
                    ProfileTab.DISCIPLINARY -> if (disciplinaries.isEmpty()) {
                        EmptyState("هنوز مورد انضباطی ثبت نشده", "با دکمه + یک مورد اضافه کنید")
                    } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(disciplinaries, key = { it.id }) { rec ->
                            NoteRow(title = rec.violationType, subtitle = "${DateUtil.format(rec.date)} · شدت: ${rec.severity}", body = rec.description) {
                                navController.navigate(Routes.noteForm("disciplinary", employeeId, rec.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun countFor(tab: ProfileTab, p: Int, n: Int, per: Int, d: Int) = when (tab) {
    ProfileTab.POSITIVE -> p
    ProfileTab.NEGATIVE -> n
    ProfileTab.PERSONALITY -> per
    ProfileTab.DISCIPLINARY -> d
}

@Composable
private fun NoteRow(title: String, subtitle: String, body: String, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(16.dp))
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            if (body.isNotBlank()) Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
