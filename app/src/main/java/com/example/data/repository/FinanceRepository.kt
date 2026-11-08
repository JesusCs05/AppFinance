package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BankNotificationDraftEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FinanceRepository(private val database: AppDatabase) {

    private val accountDao = database.accountDao()
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val recurringRuleDao = database.recurringRuleDao()
    private val bankNotificationDao = database.bankNotificationDao()

    // Flows
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allRecurringRules: Flow<List<RecurringRuleEntity>> = recurringRuleDao.getAllRecurringRules()
    val pendingBankDrafts: Flow<List<BankNotificationDraftEntity>> = bankNotificationDao.getPendingDrafts()
    val allBankDrafts: Flow<List<BankNotificationDraftEntity>> = bankNotificationDao.getAllDrafts()

    fun getTransactionsInRange(start: Long, end: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsInRange(start, end)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
        // Update account balance
        val delta = if (transaction.type == "income") transaction.amount else -transaction.amount
        accountDao.adjustBalance(transaction.accountId, delta)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        // Reverse account balance
        val delta = if (transaction.type == "income") -transaction.amount else transaction.amount
        accountDao.adjustBalance(transaction.accountId, delta)
    }

    suspend fun updateTransaction(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        transactionDao.updateTransaction(newTransaction)
        // Reverse old
        val oldDelta = if (oldTransaction.type == "income") -oldTransaction.amount else oldTransaction.amount
        accountDao.adjustBalance(oldTransaction.accountId, oldDelta)
        // Apply new
        val newDelta = if (newTransaction.type == "income") newTransaction.amount else -newTransaction.amount
        accountDao.adjustBalance(newTransaction.accountId, newDelta)
    }

    // Bank notification ingestion & review
    suspend fun insertBankDraft(draft: BankNotificationDraftEntity): Boolean {
        val existing = bankNotificationDao.getDraftByHash(draft.dedupHash)
        if (existing != null) return false
        bankNotificationDao.insertDraft(draft)
        return true
    }

    suspend fun commitBankDraft(draftId: String, selectedAccountId: String, overrideCategoryId: String? = null) {
        val draftList = bankNotificationDao.getAllDrafts()
        // Find draft
        database.runInTransaction {
            // Room transaction
        }
        bankNotificationDao.updateStatus(draftId, "confirmed")
    }

    suspend fun commitDraftWithTransaction(draft: BankNotificationDraftEntity, targetAccountId: String, targetCategoryId: String) {
        // Mark confirmed
        bankNotificationDao.updateStatus(draft.id, "confirmed")

        // Create transaction
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            accountId = targetAccountId,
            categoryId = targetCategoryId,
            type = draft.detectedType,
            amount = draft.detectedAmount,
            occurredAt = draft.occurredAt,
            merchant = draft.detectedMerchant,
            notes = "Confirmado desde alerta bancaria (${draft.bankName})",
            source = "bank_notif",
            idempotencyKey = draft.dedupHash
        )
        insertTransaction(tx)
    }

    suspend fun dismissBankDraft(draftId: String) {
        bankNotificationDao.updateStatus(draftId, "dismissed")
    }

    // Account mutations
    suspend fun insertAccount(account: AccountEntity) = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    // Category mutations
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.insertCategory(category)

    // Recurring rules
    suspend fun insertRecurringRule(rule: RecurringRuleEntity) = recurringRuleDao.insertRecurringRule(rule)
    suspend fun deleteRecurringRule(rule: RecurringRuleEntity) = recurringRuleDao.deleteRecurringRule(rule)
}
