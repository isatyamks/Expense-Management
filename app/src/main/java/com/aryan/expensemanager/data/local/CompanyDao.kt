package com.aryan.expensemanager.data.local

import androidx.room.*
import com.aryan.expensemanager.data.ApprovalRequest
import com.aryan.expensemanager.data.ApprovalRule
import com.aryan.expensemanager.data.ApprovalRuleType
import com.aryan.expensemanager.data.Company
import com.aryan.expensemanager.data.Expense
import com.aryan.expensemanager.data.ExpenseCategory
import com.aryan.expensemanager.data.ExpenseStatus
import com.aryan.expensemanager.data.User
import com.aryan.expensemanager.data.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Insert
    suspend fun insertCompany(company: Company): Long

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Long): Company?
}

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE companyId = :companyId")
    fun getUsersByCompany(companyId: Long): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE companyId = :companyId AND role = :role")
    fun getUsersByRole(companyId: Long, role: UserRole): Flow<List<User>>
}

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE employeeId = :employeeId ORDER BY submittedAt DESC")
    fun getExpensesByEmployee(employeeId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE companyId = :companyId ORDER BY submittedAt DESC")
    fun getAllExpenses(companyId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Update
    suspend fun updateExpense(expense: Expense)
}

@Dao
interface ApprovalRuleDao {
    @Insert
    suspend fun insertApprovalRule(rule: ApprovalRule): Long

    @Query("SELECT * FROM approval_rules WHERE companyId = :companyId")
    fun getApprovalRules(companyId: Long): Flow<List<ApprovalRule>>

    @Query("SELECT * FROM approval_rules WHERE id = :id")
    suspend fun getApprovalRuleById(id: Long): ApprovalRule?
}

@Dao
interface ApprovalRequestDao {
    @Insert
    suspend fun insertApprovalRequest(request: ApprovalRequest): Long

    @Query("SELECT * FROM approval_requests WHERE approverId = :approverId AND status = 'PENDING'")
    fun getPendingApprovals(approverId: Long): Flow<List<ApprovalRequest>>

    @Query("SELECT * FROM approval_requests WHERE expenseId = :expenseId")
    suspend fun getApprovalsByExpense(expenseId: Long): List<ApprovalRequest>

    @Update
    suspend fun updateApprovalRequest(request: ApprovalRequest)
}

@Database(
    entities = [
        Company::class,
        User::class,
        Expense::class,
        ApprovalRule::class,
        ApprovalRequest::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun approvalRuleDao(): ApprovalRuleDao
    abstract fun approvalRequestDao(): ApprovalRequestDao
}

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String = value.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)

    @TypeConverter
    fun fromExpenseStatus(value: ExpenseStatus): String = value.name

    @TypeConverter
    fun toExpenseStatus(value: String): ExpenseStatus = ExpenseStatus.valueOf(value)

    @TypeConverter
    fun fromApprovalRuleType(value: ApprovalRuleType): String = value.name

    @TypeConverter
    fun toApprovalRuleType(value: String): ApprovalRuleType = ApprovalRuleType.valueOf(value)
}