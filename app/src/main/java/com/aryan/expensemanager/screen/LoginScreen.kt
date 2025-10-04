package com.aryan.expensemanager.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aryan.expensemanager.data.ApprovalRequest
import com.aryan.expensemanager.data.ExpenseCategory
import com.aryan.expensemanager.data.ExpenseStatus
import com.aryan.expensemanager.data.UserRole
import com.aryan.expensemanager.data.remote.CountryResponse
import com.aryan.expensemanager.viewModel.AuthState
import com.aryan.expensemanager.viewModel.AuthViewModel
import com.aryan.expensemanager.viewModel.ExpenseViewModel
import com.aryan.expensemanager.viewModel.UserManagementViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Expense Management", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToSignup) {
            Text("Don't have an account? Sign Up")
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf<CountryResponse?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val countries by viewModel.countries.collectAsState()
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSignupSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Company", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Company Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCountry?.name?.common ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Country") },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countries.take(20).forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.name.common) },
                        onClick = {
                            selectedCountry = country
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = adminName,
            onValueChange = { adminName = it },
            label = { Text("Admin Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Admin Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedCountry?.let { country ->
                    val currency = country.currencies?.keys?.firstOrNull() ?: "USD"
                    val currencySymbol = country.currencies?.values?.firstOrNull()?.symbol ?: "$"
                    viewModel.signup(
                        companyName = companyName,
                        countryCode = country.name.common,
                        currency = currency,
                        currencySymbol = currencySymbol,
                        adminEmail = email,
                        adminPassword = password,
                        adminName = adminName
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading && selectedCountry != null
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Create Company")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToExpenses: () -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToRules: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentCompany by authViewModel.currentCompany.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Welcome, ${currentUser?.name}",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "${currentCompany?.name} (${currentCompany?.currency})",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            "Role: ${currentUser?.role?.name}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToExpenses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Expenses")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser?.role == UserRole.MANAGER || currentUser?.role == UserRole.ADMIN) {
            Button(
                onClick = onNavigateToApprovals,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pending Approvals")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (currentUser?.role == UserRole.ADMIN) {
            Button(
                onClick = onNavigateToUsers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Users")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToRules,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Approval Rules")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val expenses by viewModel.myExpenses.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val company by authViewModel.currentCompany.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Expenses", style = MaterialTheme.typography.headlineMedium)

            if (currentUser?.role == UserRole.EMPLOYEE || currentUser?.role == UserRole.MANAGER) {
                Button(onClick = onNavigateToSubmit) {
                    Text("Submit")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(expenses) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                expense.category.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${expense.currency} ${expense.amount}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(expense.description)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "In ${company?.currency}: ${company?.currencySymbol}${expense.amountInCompanyCurrency}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                java.text.SimpleDateFormat("dd/MM/yyyy").format(expense.date),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Surface(
                                color = when (expense.status) {
                                    ExpenseStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
                                    ExpenseStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                                    ExpenseStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    expense.status.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitExpenseScreen(
    viewModel: ExpenseViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var category by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var description by remember { mutableStateOf("") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val company by authViewModel.currentCompany.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Submit Expense", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it },
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = showCategoryMenu,
            onExpandedChange = { showCategoryMenu = !showCategoryMenu }
        ) {
            OutlinedTextField(
                value = category.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                ExpenseCategory.values().forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.name) },
                        onClick = {
                            category = cat
                            showCategoryMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.submitExpense(
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    currency = currency,
                    category = category,
                    description = description,
                    date = System.currentTimeMillis()
                )
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Expense")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun ApprovalsScreen(
    viewModel: ExpenseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    var selectedRequest by remember { mutableStateOf<ApprovalRequest?>(null) }
    var comments by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pending Approvals", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pendingApprovals) { request ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Expense ID: ${request.expenseId}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "Step: ${request.stepNumber}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.approveExpense(request.id, comments)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve")
                            }

                            Button(
                                onClick = {
                                    viewModel.rejectExpense(request.id, comments)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val users by viewModel.users.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Users", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = { showCreateDialog = true }) {
                Text("Add User")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        Text(user.email, style = MaterialTheme.typography.bodyMedium)
                        Text("Role: ${user.role.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    viewModel: UserManagementViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var showRoleMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create User") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = showRoleMenu,
                    onExpandedChange = { showRoleMenu = !showRoleMenu }
                ) {
                    OutlinedTextField(
                        value = role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        UserRole.values().forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = {
                                    role = r
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.createEmployee(name, email, password, role, null)
                    onDismiss()
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}