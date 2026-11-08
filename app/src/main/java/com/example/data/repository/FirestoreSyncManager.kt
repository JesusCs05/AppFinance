package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Throwable) {
            Log.e("FirestoreSync", "Failed to get FirebaseFirestore instance", e)
            null
        }
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    suspend fun syncTransactionToCloud(userId: String, tx: TransactionEntity) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(userId).collection("transactions").document(tx.id)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "accountId" to tx.accountId,
                "categoryId" to tx.categoryId,
                "type" to tx.type,
                "amount" to tx.amount,
                "occurredAt" to tx.occurredAt,
                "merchant" to tx.merchant,
                "notes" to tx.notes,
                "source" to tx.source,
                "idempotencyKey" to tx.idempotencyKey,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
            Log.d("FirestoreSync", "Transaction synced to cloud: ${tx.id}")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing transaction to cloud", e)
        }
    }

    suspend fun deleteTransactionFromCloud(userId: String, transactionId: String) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        try {
            firestore.collection("users").document(userId).collection("transactions").document(transactionId).delete().await()
            Log.d("FirestoreSync", "Transaction deleted from cloud: $transactionId")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting transaction from cloud", e)
        }
    }

    suspend fun syncAccountToCloud(userId: String, account: AccountEntity) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(userId).collection("accounts").document(account.id)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "name" to account.name,
                "type" to account.type,
                "lastFourDigits" to account.lastFourDigits,
                "balance" to account.balance,
                "colorHex" to account.colorHex,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing account to cloud", e)
        }
    }

    suspend fun syncCategoryToCloud(userId: String, category: CategoryEntity) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(userId).collection("categories").document(category.id)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "name" to category.name,
                "kind" to category.kind,
                "icon" to category.icon,
                "colorHex" to category.colorHex,
                "monthlyLimit" to category.monthlyLimit,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing category to cloud", e)
        }
    }

    suspend fun fullSync(
        userId: String,
        localAccounts: List<AccountEntity>,
        localCategories: List<CategoryEntity>,
        localTransactions: List<TransactionEntity>
    ) = withContext(Dispatchers.IO) {
        if (db == null) return@withContext
        _isSyncing.value = true
        try {
            // Push accounts
            for (acc in localAccounts) {
                syncAccountToCloud(userId, acc)
            }
            // Push categories
            for (cat in localCategories) {
                syncCategoryToCloud(userId, cat)
            }
            // Push transactions
            for (tx in localTransactions) {
                syncTransactionToCloud(userId, tx)
            }
            _lastSyncTime.value = System.currentTimeMillis()
            Log.d("FirestoreSync", "Full cloud sync completed for user: $userId")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Full cloud sync error", e)
        } finally {
            _isSyncing.value = false
        }
    }
}
