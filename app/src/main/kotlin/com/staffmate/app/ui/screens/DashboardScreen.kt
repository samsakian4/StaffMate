package com.staffmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.ui.Routes
import com.staffmate.app.ui.ScoreColors

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
        topBar = {
            TopAppBar(
                title = { Text("پرسنل‌یار", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = { BottomNavBar(navController, current = Routes.DASHBOARD) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.QUICK_ADD) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ثبت مورد") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("جست‌وجوی سریع پرسنل") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            TextButton(onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("prefilledQuery", searchQuery)
                                navController.navigate(Routes.EMPLOYEES)
                            }) { Text("برو") }
                        }
                    }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("پرسنل فعال", activeCount.toString(), Icons.Outlined.Groups, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatCard("مثبت", posCount.toString(), Icons.Outlined.ThumbUp, ScoreColors.positive, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("منفی", negCount.toString(), Icons.Outlined.ThumbDown, ScoreColors.negative, Modifier.weight(1f))
                    StatCard("انضباطی", discCount.toString(), Icons.Outlined.Gavel, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
            }
            item {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.EMPLOYEES) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("مشاهده لیست پرسنل") }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = 0.16f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class NavItem(val route: String, val label: String, val filled: ImageVector, val outlined: ImageVector)

private val navItems = listOf(
    NavItem(Routes.DASHBOARD, "خانه", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Routes.EMPLOYEES, "پرسنل", Icons.Filled.Groups, Icons.Outlined.Groups),
    NavItem(Routes.QUICK_ADD, "ثبت مورد", Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
    NavItem(Routes.REPORTS, "گزارش‌ها", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    NavItem(Routes.SETTINGS, "تنظیمات", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun BottomNavBar(navController: NavHostController, current: String) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        navItems.forEach { item ->
            val selected = current == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                icon = { Icon(if (selected) item.filled else item.outlined, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}
