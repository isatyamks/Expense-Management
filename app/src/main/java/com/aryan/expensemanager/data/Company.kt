package com.aryan.expensemanager.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val currency: String,
    val currencySymbol: String,
    val countryCode: String
)

enum class UserRole {
    ADMIN, MANAGER, EMPLOYEE
}

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole,
    val managerId: Long? = null
)

enum class ExpenseCategory {
    FOOD, TRAVEL, ACCOMMODATION, SUPPLIES, OTHER
}

enum class ExpenseStatus {
    PENDING, APPROVED, REJECTED
}

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val employeeId: Long,
    val amount: Double,
    val currency: String,
    val amountInCompanyCurrency: Double,
    val category: ExpenseCategory,
    val description: String,
    val date: Long,
    val status: ExpenseStatus = ExpenseStatus.PENDING,
    val currentApproverStep: Int = 0,
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "approval_rules")
data class ApprovalRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val name: String,
    val isManagerApprover: Boolean = false,
    val approverSequence: String, // JSON array of approver IDs
    val ruleType: ApprovalRuleType,
    val percentageThreshold: Int? = null,
    val specificApproverId: Long? = null
)

enum class ApprovalRuleType {
    SEQUENTIAL, PERCENTAGE, SPECIFIC_APPROVER, HYBRID
}

@Entity(
    tableName = "approval_requests",
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["approverId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ApprovalRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseId: Long,
    val approverId: Long,
    val stepNumber: Int,
    val status: ExpenseStatus = ExpenseStatus.PENDING,
    val comments: String? = null,
    val actionDate: Long? = null
)