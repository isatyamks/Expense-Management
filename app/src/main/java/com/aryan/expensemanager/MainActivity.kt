package com.aryan.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aryan.expensemanager.screen.ApprovalRulesScreen
import com.aryan.expensemanager.screen.ApprovalsScreen
import com.aryan.expensemanager.screen.DashboardScreen
import com.aryan.expensemanager.screen.ExpenseListScreen
import com.aryan.expensemanager.screen.LoginScreen
import com.aryan.expensemanager.screen.SignupScreen
import com.aryan.expensemanager.screen.SubmitExpenseScreen
import com.aryan.expensemanager.screen.UserManagementScreen
import com.aryan.expensemanager.viewModel.AuthViewModel
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
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    // FIXED: Determine start destination based on session
    val startDestination = if (currentUser != null) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
            // FIXED: Redirect to login if user is logged out
            LaunchedEffect(currentUser) {
                if (currentUser == null) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            DashboardScreen(
                onNavigateToExpenses = { navController.navigate("expenses") },
                onNavigateToApprovals = { navController.navigate("approvals") },
                onNavigateToUsers = { navController.navigate("users") },
                onNavigateToRules = { navController.navigate("rules") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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

        composable("rules") {
            ApprovalRulesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}