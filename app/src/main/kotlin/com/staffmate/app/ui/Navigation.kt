package com.staffmate.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.staffmate.app.ui.screens.*

object Routes {
    const val PIN = "pin"
    const val DASHBOARD = "dashboard"
    const val EMPLOYEES = "employees"
    const val EMPLOYEE_FORM = "employeeForm/{id}"
    const val EMPLOYEE_PROFILE = "employeeProfile/{id}"
    const val QUICK_ADD = "quickAdd"
    const val NOTE_FORM = "noteForm/{type}/{employeeId}/{noteId}"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    fun employeeForm(id: Long) = "employeeForm/$id"
    fun employeeProfile(id: Long) = "employeeProfile/$id"
    fun noteForm(type: String, employeeId: Long, noteId: Long = -1L) = "noteForm/$type/$employeeId/$noteId"
}

@Composable
fun StaffMateNavHost(startDestination: String) {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.PIN) { PinScreen(navController) }
        composable(Routes.DASHBOARD) { DashboardScreen(navController) }
        composable(Routes.EMPLOYEES) { EmployeeListScreen(navController) }
        composable(
            Routes.EMPLOYEE_FORM,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            EmployeeFormScreen(navController, id)
        }
        composable(
            Routes.EMPLOYEE_PROFILE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            EmployeeProfileScreen(navController, id)
        }
        composable(Routes.QUICK_ADD) { QuickAddScreen(navController) }
        composable(
            Routes.NOTE_FORM,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("employeeId") { type = NavType.LongType },
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "positive"
            val employeeId = backStackEntry.arguments?.getLong("employeeId") ?: -1L
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            NoteFormScreen(navController, type, employeeId, if (noteId == -1L) null else noteId)
        }
        composable(Routes.REPORTS) { ReportsScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
    }
}
