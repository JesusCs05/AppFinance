package com.example.domain.recurring

import com.example.data.local.AppDatabase
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

object RecurringPaymentScheduler {

    data class RecurringProcessResult(
        val processedCount: Int,
        val totalAmount: Double,
        val messages: List<String>
    )

    suspend fun evaluateAndProcessDueRules(
        database: AppDatabase,
        repository: FinanceRepository
    ): RecurringProcessResult = withContext(Dispatchers.IO) {
        val recurringDao = database.recurringRuleDao()
        val allRules = recurringDao.getAllRecurringRules().first()
        val activeRules = allRules.filter { it.isActive && it.autoCommit }

        val now = System.currentTimeMillis()
        var count = 0
        var total = 0.0
        val logs = mutableListOf<String>()

        for (rule in activeRules) {
            if (isRuleDue(rule, now)) {
                // Generate transaction
                val tx = TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    accountId = rule.accountId,
                    categoryId = rule.categoryId,
                    recurringRuleId = rule.id,
                    type = "expense",
                    amount = rule.amount,
                    occurredAt = now,
                    merchant = rule.merchant,
                    notes = "Cargo periódico automático (${rule.frequency.uppercase()})",
                    source = "recurring"
                )
                repository.insertTransaction(tx)

                // Advance nextRunDate
                val nextDate = computeNextRunDate(rule.frequency, rule.nextRunDate)
                recurringDao.updateRecurringRule(rule.copy(nextRunDate = nextDate))

                count++
                total += rule.amount
                logs.add("${rule.merchant}: $${"%,.2f".format(rule.amount)} MXN")
            }
        }

        RecurringProcessResult(
            processedCount = count,
            totalAmount = total,
            messages = logs
        )
    }

    private fun isRuleDue(rule: RecurringRuleEntity, currentTimestamp: Long): Boolean {
        // If nextRunDate has passed or is today
        if (rule.nextRunDate <= currentTimestamp) return true

        val calNow = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        val todayDay = calNow.get(Calendar.DAY_OF_MONTH)
        return (rule.frequency == "monthly" && todayDay == rule.dayOfMonth)
    }

    private fun computeNextRunDate(frequency: String, lastTimestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }
        when (frequency.lowercase()) {
            "daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "biweekly" -> cal.add(Calendar.WEEK_OF_YEAR, 2)
            "monthly" -> cal.add(Calendar.MONTH, 1)
            "yearly" -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }
}
