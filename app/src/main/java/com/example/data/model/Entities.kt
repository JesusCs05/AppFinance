package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // cash, debit, credit, investment
    val lastFourDigits: String = "0000",
    val balance: Double = 0.0,
    val currency: String = "MXN",
    val colorHex: String = "#4B0082"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kind: String, // income, expense
    val icon: String, // e.g. "shopping_cart", "restaurant", "home", "payments", etc.
    val colorHex: String,
    val monthlyLimit: Double = 0.0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val categoryId: String,
    val recurringRuleId: String? = null,
    val type: String, // income, expense, transfer
    val amount: Double,
    val currency: String = "MXN",
    val occurredAt: Long = System.currentTimeMillis(),
    val merchant: String,
    val notes: String = "",
    val source: String = "manual", // manual, ai_voice, ai_text, bank_notif, recurring
    val idempotencyKey: String = UUID.randomUUID().toString(),
    val requiresBiometric: Boolean = false
)

@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val categoryId: String,
    val merchant: String,
    val amount: Double,
    val frequency: String, // daily, weekly, biweekly, monthly, yearly
    val dayOfMonth: Int = 1,
    val nextRunDate: Long = System.currentTimeMillis(),
    val autoCommit: Boolean = true,
    val isActive: Boolean = true
)

@Entity(tableName = "bank_notification_drafts")
data class BankNotificationDraftEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bankPackage: String,
    val bankName: String,
    val detectedType: String, // income, expense
    val detectedAmount: Double,
    val detectedMerchant: String,
    val suggestedCategoryId: String,
    val dedupHash: String,
    val status: String = "pending", // pending, confirmed, dismissed
    val occurredAt: Long = System.currentTimeMillis(),
    val rawMessage: String,
    val accountLastFour: String = ""
)
