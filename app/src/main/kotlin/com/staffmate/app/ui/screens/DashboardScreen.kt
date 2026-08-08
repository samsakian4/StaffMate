package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.ui.Routes

@Composable
fun DashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository

    val activeCount by repository.employees.observeActiveCount().collectAsState(initial = 0)
    val posCount by repository.positives.observeTotalCount().collectAsState(initial = 0)
    val negCount by repository.negatives.observeTotalCount().collectAsState(initial = 0)
    val discCount by repository.disciplinary.observeTotalCount().collectAsState(initial = 0)

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("پرسنل‌یار") }) },
        bottomBar = { BottomNavBar(navController, current = Routes.DASHBOARD) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.QUICK_ADD) }) {
                Icon(Icons.Default.Add, contentDescription = "ثبت مورد جدید")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("جست‌وجوی سریع پرسنل") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("prefilledQuery", searchQuery)
                            navController.navigate(Routes.EMPLOYEES)
                        }) { Text("جست‌وجو") }
                    }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("پرسنل فعال", activeCount.toString(), Modifier.weight(1f))
                    StatCard("مثبت", posCount.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("منفی", negCount.toString(), Modifier.weight(1f))
                    StatCard("انضباطی", discCount.toString(), Modifier.weight(1f))
                }
            }
            item {
                Button(
                    onClick = { navController.navigate(Routes.QUICK_ADD) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ثبت مورد جدید") }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController, current: String) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Routes.DASHBOARD,
            onClick = { navController.navigate(Routes.DASHBOARD) { launchSingleTop = true } },
            icon = {}, label = { Text("خانه") }
        )
        NavigationBarItem(
            selected = current == Routes.EMPLOYEES,
            onClick = { navController.navigate(Routes.EMPLOYEES) { launchSingleTop = true } },
            icon = {}, label = { Text("پرسنل") }
        )
        NavigationBarItem(
            selected = current == Routes.QUICK_ADD,
            onClick = { navController.navigate(Routes.QUICK_ADD) { launchSingleTop = true } },
            icon = {}, label = { Text("ثبت مورد") }
        )
        NavigationBarItem(
            selected = current == Routes.REPORTS,
            onClick = { navController.navigate(Routes.REPORTS) { launchSingleTop = true } },
            icon = {}, label = { Text("گزارش‌ها") }
        )
        NavigationBarItem(
            selected = current == Routes.SETTINGS,
            onClick = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
            icon = {}, label = { Text("تنظیمات") }
        )
    }
}
