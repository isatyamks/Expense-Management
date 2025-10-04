package com.aryan.expensemanager

import ApprovalRulesScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aryan.expensemanager.screen.ApprovalsScreen
import com.aryan.expensemanager.screen.DashboardScreen
import com.aryan.expensemanager.screen.ExpenseListScreen
import com.aryan.expensemanager.screen.LoginScreen
import com.aryan.expensemanager.screen.SignupScreen
import com.aryan.expensemanager.screen.SubmitExpenseScreen
import com.aryan.expensemanager.screen.UserManagementScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ExpenseManagementApp()
            }
        }
    }
}


@Composable
fun ExpenseManagementApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("dashboard") },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess = { navController.navigate("dashboard") },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigateToExpenses = { navController.navigate("expenses") },
                onNavigateToApprovals = { navController.navigate("approvals") },
                onNavigateToUsers = { navController.navigate("users") },
                onNavigateToRules = { navController.navigate("rules") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("expenses") {
            ExpenseListScreen(
                onNavigateToSubmit = { navController.navigate("submit_expense") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("submit_expense") {
            SubmitExpenseScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("approvals") {
            ApprovalsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("users") {
            UserManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ADD THIS MISSING ROUTE
        composable("rules") {
            ApprovalRulesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
