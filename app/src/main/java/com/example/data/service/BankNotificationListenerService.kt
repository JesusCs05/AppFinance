package com.example.data.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.BankNotificationDraftEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BankNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (!BankNotificationParser.isAllowedPackage(packageName)) {
            return
        }

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val timestamp = sbn.postTime

        val parsed = BankNotificationParser.parseNotification(packageName, title, text, timestamp) ?: return

        serviceScope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val bankDao = db.bankNotificationDao()

                // Deduplication check
                val existing = bankDao.getDraftByHash(parsed.dedupHash)
                if (existing == null) {
                    val draft = BankNotificationDraftEntity(
                        bankPackage = parsed.bankPackage,
                        bankName = parsed.bankName,
                        detectedType = parsed.type,
                        detectedAmount = parsed.amount,
                        detectedMerchant = parsed.merchant,
                        suggestedCategoryId = parsed.suggestedCategoryId,
                        dedupHash = parsed.dedupHash,
                        status = "pending",
                        occurredAt = timestamp,
                        rawMessage = parsed.rawText,
                        accountLastFour = parsed.lastFour
                    )
                    bankDao.insertDraft(draft)
                    Log.d("BankListener", "Ingested bank draft: ${parsed.merchant} - $${parsed.amount}")
                }
            } catch (e: Exception) {
                Log.e("BankListener", "Error ingesting notification", e)
            }
        }
    }
}
