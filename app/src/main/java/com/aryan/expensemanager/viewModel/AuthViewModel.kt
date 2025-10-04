package com.aryan.expensemanager.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryan.expensemanager.data.ApprovalRule
import com.aryan.expensemanager.data.ApprovalRuleType
import com.aryan.expensemanager.data.Company
import com.aryan.expensemanager.data.Expense
import com.aryan.expensemanager.data.ExpenseCategory
import com.aryan.expensemanager.data.User
import com.aryan.expensemanager.data.UserRole
import com.aryan.expensemanager.data.UserSession
import com.aryan.expensemanager.data.remote.CountryResponse
import com.aryan.expensemanager.data.repository.ExpenseRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val userSession: UserSession
) : ViewModel() {

    val currentUser: StateFlow<User?> = userSession.currentUser
    val currentCompany: StateFlow<Company?> = userSession.currentCompany

    private val _countries = MutableStateFlow<List<CountryResponse>>(emptyList())
    val countries: StateFlow<List<CountryResponse>> = _countries.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            try {
                _countries.value = repository.getAllCountries()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = repository.login(email, password)
                if (user != null) {
                    val company = repository.getCompanyById(user.companyId)
                    userSession.setUser(user, company)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signup(
        companyName: String,
        countryCode: String,
        currency: String,
        currencySymbol: String,
        adminEmail: String,
        adminPassword: String,
        adminName: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val (company, admin) = repository.createCompanyAndAdmin(
                    companyName, countryCode, currency, currencySymbol,
                    adminEmail, adminPassword, adminName
                )
                userSession.setUser(admin, company)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Signup failed")
            }
        }
    }

    fun logout() {
        userSession.clear()
        _authState.value = AuthState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val userSession: UserSession
) : ViewModel() {

    val users = userSession.currentCompany.flatMapLatest { company ->
        company?.let { repository.getUsersByCompany(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val managers = userSession.currentCompany.flatMapLatest { company ->
        company?.let { repository.getUsersByRole(it.id, UserRole.MANAGER) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createEmployee(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        managerId: Long?
    ) {
        viewModelScope.launch {
            userSession.currentCompany.value?.let { company ->
                repository.createUser(
                    User(
                        companyId = company.id,
                        email = email,
                        password = password,
                        name = name,
                        role = role,
                        managerId = managerId
                    )
                )
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }
}

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val userSession: UserSession
) : ViewModel() {

    val myExpenses = userSession.currentUser.flatMapLatest { user ->
        user?.let { repository.getExpensesByEmployee(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allExpenses = userSession.currentCompany.flatMapLatest { company ->
        company?.let { repository.getAllExpenses(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingApprovals = userSession.currentUser.flatMapLatest { user ->
        user?.let { repository.getPendingApprovals(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun submitExpense(
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        description: String,
        date: Long
    ) {
        viewModelScope.launch {
            val user = userSession.currentUser.value ?: return@launch
            val company = userSession.currentCompany.value ?: return@launch

            val convertedAmount = repository.convertCurrency(amount, currency, company.currency)

            val expense = Expense(
                companyId = company.id,
                employeeId = user.id,
                amount = amount,
                currency = currency,
                amountInCompanyCurrency = convertedAmount,
                category = category,
                description = description,
                date = date
            )

            repository.submitExpense(expense, null)
        }
    }

    fun approveExpense(requestId: Long, comments: String?) {
        viewModelScope.launch {
            repository.approveOrRejectExpense(requestId, true, comments, null)
        }
    }

    fun rejectExpense(requestId: Long, comments: String?) {
        viewModelScope.launch {
            repository.approveOrRejectExpense(requestId, false, comments, null)
        }
    }
}

@HiltViewModel
class ApprovalRuleViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val userSession: UserSession
) : ViewModel() {

    val approvalRules = userSession.currentCompany.flatMapLatest { company ->
        company?.let { repository.getApprovalRules(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createApprovalRule(
        name: String,
        isManagerApprover: Boolean,
        approverIds: List<Long>,
        ruleType: ApprovalRuleType,
        percentageThreshold: Int?,
        specificApproverId: Long?
    ) {
        viewModelScope.launch {
            userSession.currentCompany.value?.let { company ->
                val rule = ApprovalRule(
                    companyId = company.id,
                    name = name,
                    isManagerApprover = isManagerApprover,
                    approverSequence = Gson().toJson(approverIds),
                    ruleType = ruleType,
                    percentageThreshold = percentageThreshold,
                    specificApproverId = specificApproverId
                )
                repository.createApprovalRule(rule)
            }
        }
    }
}