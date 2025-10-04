package com.aryan.expensemanager.data.repository


import com.aryan.expensemanager.data.local.*
import com.aryan.expensemanager.data.*
import com.aryan.expensemanager.data.remote.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.google.gson.Gson

class ExpenseRepository @Inject constructor(
    private val companyDao: CompanyDao,
    private val userDao: UserDao,
    private val expenseDao: ExpenseDao,
    private val approvalRuleDao: ApprovalRuleDao,
    private val approvalRequestDao: ApprovalRequestDao,
    private val countryApi: CountryApi,
    private val exchangeRateApi: ExchangeRateApi
) {
    suspend fun createCompanyAndAdmin(
        companyName: String,
        countryCode: String,
        currency: String,
        currencySymbol: String,
        adminEmail: String,
        adminPassword: String,
        adminName: String
    ): Pair<Company, User> {
        val company = Company(
            name = companyName,
            currency = currency,
            currencySymbol = currencySymbol,
            countryCode = countryCode
        )
        val companyId = companyDao.insertCompany(company)

        val admin = User(
            companyId = companyId,
            email = adminEmail,
            password = adminPassword,
            name = adminName,
            role = UserRole.ADMIN
        )
        val adminId = userDao.insertUser(admin)

        return company.copy(id = companyId) to admin.copy(id = adminId)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun createUser(user: User): Long {
        return userDao.insertUser(user)
    }

    fun getUsersByCompany(companyId: Long): Flow<List<User>> {
        return userDao.getUsersByCompany(companyId)
    }

    fun getUsersByRole(companyId: Long, role: UserRole): Flow<List<User>> {
        return userDao.getUsersByRole(companyId, role)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)
    }

    suspend fun getCompanyById(id: Long): Company? {
        return companyDao.getCompanyById(id)
    }

    suspend fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        return try {
            if (fromCurrency == toCurrency) return amount
            val rates = exchangeRateApi.getExchangeRates(fromCurrency)
            val rate = rates.rates[toCurrency] ?: 1.0
            amount * rate
        } catch (e: Exception) {
            amount
        }
    }

    suspend fun submitExpense(
        expense: Expense,
        approvalRule: ApprovalRule?
    ): Long {
        val expenseId = expenseDao.insertExpense(expense)

        // Create approval requests based on rule
        if (approvalRule != null) {
            val approverIds = Gson().fromJson(approvalRule.approverSequence, Array<Long>::class.java).toList()

            if (approvalRule.isManagerApprover) {
                val employee = userDao.getUserById(expense.employeeId)
                employee?.managerId?.let { managerId ->
                    approvalRequestDao.insertApprovalRequest(
                        ApprovalRequest(
                            expenseId = expenseId,
                            approverId = managerId,
                            stepNumber = 0
                        )
                    )
                }
            }

            approverIds.forEachIndexed { index, approverId ->
                approvalRequestDao.insertApprovalRequest(
                    ApprovalRequest(
                        expenseId = expenseId,
                        approverId = approverId,
                        stepNumber = index + 1
                    )
                )
            }
        }

        return expenseId
    }

    fun getExpensesByEmployee(employeeId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByEmployee(employeeId)
    }

    fun getAllExpenses(companyId: Long): Flow<List<Expense>> {
        return expenseDao.getAllExpenses(companyId)
    }

    fun getPendingApprovals(approverId: Long): Flow<List<ApprovalRequest>> {
        return approvalRequestDao.getPendingApprovals(approverId)
    }

    suspend fun approveOrRejectExpense(
        requestId: Long,
        approve: Boolean,
        comments: String?,
        rule: ApprovalRule?
    ) {
        val request = approvalRequestDao.getApprovalsByExpense(0).find { it.id == requestId }
            ?: return

        val updatedRequest = request.copy(
            status = if (approve) ExpenseStatus.APPROVED else ExpenseStatus.REJECTED,
            comments = comments,
            actionDate = System.currentTimeMillis()
        )
        approvalRequestDao.updateApprovalRequest(updatedRequest)

        val expense = expenseDao.getExpenseById(request.expenseId) ?: return

        if (!approve) {
            expenseDao.updateExpense(expense.copy(status = ExpenseStatus.REJECTED))
            return
        }

        // Check if expense should be approved based on rules
        val allRequests = approvalRequestDao.getApprovalsByExpense(request.expenseId)

        if (rule != null) {
            when (rule.ruleType) {
                ApprovalRuleType.SPECIFIC_APPROVER -> {
                    if (request.approverId == rule.specificApproverId) {
                        expenseDao.updateExpense(expense.copy(status = ExpenseStatus.APPROVED))
                    }
                }
                ApprovalRuleType.PERCENTAGE -> {
                    val approvedCount = allRequests.count { it.status == ExpenseStatus.APPROVED }
                    val percentage = (approvedCount.toDouble() / allRequests.size) * 100
                    if (percentage >= (rule.percentageThreshold ?: 100)) {
                        expenseDao.updateExpense(expense.copy(status = ExpenseStatus.APPROVED))
                    }
                }
                ApprovalRuleType.HYBRID -> {
                    val approvedCount = allRequests.count { it.status == ExpenseStatus.APPROVED }
                    val percentage = (approvedCount.toDouble() / allRequests.size) * 100
                    if (request.approverId == rule.specificApproverId ||
                        percentage >= (rule.percentageThreshold ?: 100)) {
                        expenseDao.updateExpense(expense.copy(status = ExpenseStatus.APPROVED))
                    }
                }
                ApprovalRuleType.SEQUENTIAL -> {
                    val nextStep = request.stepNumber + 1
                    val hasNextStep = allRequests.any { it.stepNumber == nextStep }
                    if (!hasNextStep) {
                        expenseDao.updateExpense(expense.copy(status = ExpenseStatus.APPROVED))
                    }
                }
            }
        } else {
            val allApproved = allRequests.all { it.status == ExpenseStatus.APPROVED }
            if (allApproved) {
                expenseDao.updateExpense(expense.copy(status = ExpenseStatus.APPROVED))
            }
        }
    }

    suspend fun createApprovalRule(rule: ApprovalRule): Long {
        return approvalRuleDao.insertApprovalRule(rule)
    }

    fun getApprovalRules(companyId: Long): Flow<List<ApprovalRule>> {
        return approvalRuleDao.getApprovalRules(companyId)
    }

    suspend fun getAllCountries(): List<CountryResponse> {
        return countryApi.getAllCountries()
    }
}