package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.BankNotificationDraftEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :amountDelta WHERE id = :id")
    suspend fun adjustBalance(id: String, amountDelta: Double)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY kind ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind ORDER BY name ASC")
    fun getCategoriesByKind(kind: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE occurredAt >= :startTime AND occurredAt <= :endTime ORDER BY occurredAt DESC")
    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY occurredAt DESC")
    fun getTransactionsByAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY occurredAt DESC")
    fun getTransactionsByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)
}

@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules ORDER BY nextRunDate ASC")
    fun getAllRecurringRules(): Flow<List<RecurringRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRule(rule: RecurringRuleEntity)

    @Update
    suspend fun updateRecurringRule(rule: RecurringRuleEntity)

    @Delete
    suspend fun deleteRecurringRule(rule: RecurringRuleEntity)
}

@Dao
interface BankNotificationDao {
    @Query("SELECT * FROM bank_notification_drafts ORDER BY occurredAt DESC")
    fun getAllDrafts(): Flow<List<BankNotificationDraftEntity>>

    @Query("SELECT * FROM bank_notification_drafts WHERE status = 'pending' ORDER BY occurredAt DESC")
    fun getPendingDrafts(): Flow<List<BankNotificationDraftEntity>>

    @Query("SELECT * FROM bank_notification_drafts WHERE dedupHash = :hash LIMIT 1")
    suspend fun getDraftByHash(hash: String): BankNotificationDraftEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraft(draft: BankNotificationDraftEntity): Long

    @Update
    suspend fun updateDraft(draft: BankNotificationDraftEntity)

    @Query("UPDATE bank_notification_drafts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM bank_notification_drafts WHERE id = :id")
    suspend fun deleteDraftById(id: String)
}
