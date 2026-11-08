package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.BankNotificationDraftEntity
import com.example.data.model.TransactionEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreSyncService(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth = Firebase.auth

    fun getCurrentUser() = auth.currentUser

    suspend fun syncUserToCloud() {
        val user = auth.currentUser ?: return
        val userDoc = db.collection("users").document(user.uid)
        val payload = mapOf(
            "userId" to user.uid,
            "email" to (user.email ?: ""),
            "displayName" to (user.displayName ?: "Usuario AetherPay"),
            "baseCurrency" to "MXN",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userDoc.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing user profile", e)
        }
    }

    suspend fun syncAccountsToCloud(accounts: List<AccountEntity>) {
        val user = auth.currentUser ?: return
        val userAccountsRef = db.collection("users").document(user.uid).collection("accounts")
        for (acc in accounts) {
            val payload = mapOf(
                "userId" to user.uid,
                "name" to acc.name,
                "type" to acc.type,
                "lastFourDigits" to acc.lastFourDigits,
                "balance" to acc.balance,
                "currency" to acc.currency,
                "colorHex" to acc.colorHex,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                userAccountsRef.document(acc.id).set(payload, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Error syncing account ${acc.id}", e)
            }
        }
    }

    suspend fun syncTransactionsToCloud(transactions: List<TransactionEntity>) {
        val user = auth.currentUser ?: return
        val userTxRef = db.collection("users").document(user.uid).collection("transactions")
        for (tx in transactions) {
            val payload = mapOf(
                "userId" to user.uid,
                "accountId" to tx.accountId,
                "categoryId" to tx.categoryId,
                "type" to tx.type,
                "amount" to tx.amount,
                "currency" to tx.currency,
                "occurredAt" to tx.occurredAt,
                "merchant" to tx.merchant,
                "notes" to tx.notes,
                "source" to tx.source,
                "idempotencyKey" to tx.idempotencyKey,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                userTxRef.document(tx.id).set(payload, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Error syncing transaction ${tx.id}", e)
            }
        }
    }

    suspend fun syncBankDraftsToCloud(drafts: List<BankNotificationDraftEntity>) {
        val user = auth.currentUser ?: return
        val draftsRef = db.collection("users").document(user.uid).collection("bank_drafts")
        for (draft in drafts) {
            val payload = mapOf(
                "userId" to user.uid,
                "bankPackage" to draft.bankPackage,
                "bankName" to draft.bankName,
                "detectedType" to draft.detectedType,
                "detectedAmount" to draft.detectedAmount,
                "detectedMerchant" to draft.detectedMerchant,
                "suggestedCategoryId" to draft.suggestedCategoryId,
                "dedupHash" to draft.dedupHash,
                "status" to draft.status,
                "occurredAt" to draft.occurredAt,
                "rawMessage" to draft.rawMessage,
                "accountLastFour" to draft.accountLastFour
            )
            try {
                draftsRef.document(draft.id).set(payload, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Error syncing draft ${draft.id}", e)
            }
        }
    }
}
