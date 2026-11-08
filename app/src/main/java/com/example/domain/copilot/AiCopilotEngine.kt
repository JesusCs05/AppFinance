package com.example.domain.copilot

import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import java.util.Locale
import java.util.UUID

data class CopilotToolCall(
    val toolName: String, // "create_transaction", "show_summary", "budget_status"
    val parsedType: String, // "income" or "expense"
    val amount: Double,
    val merchant: String,
    val categoryId: String,
    val categoryName: String,
    val accountId: String,
    val accountName: String,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: String? = null,
    val rawExplanation: String
)

data class CopilotMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "copilot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCall: CopilotToolCall? = null,
    val isToolConfirmed: Boolean = false,
    val isToolDiscarded: Boolean = false
)

object AiCopilotEngine {

    fun processPrompt(
        prompt: String,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        recentTransactions: List<TransactionEntity>
    ): CopilotMessage {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        // 1. Check for analysis/summary queries
        if (lower.contains("resumen") || lower.contains("balance") || lower.contains("cómo voy") || lower.contains("analisis") || lower.contains("estado")) {
            val totalIncome = recentTransactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = recentTransactions.filter { it.type == "expense" }.sumOf { it.amount }
            val net = totalIncome - totalExpense
            val netStr = if (net >= 0) "+$${"%,.2f".format(net)}" else "-$${"%,.2f".format(-net)}"

            val responseText = """
                📊 **Resumen Financiero AetherPay:**
                
                • **Ingresos Totales:** $${"%,.2f".format(totalIncome)} MXN
                • **Gastos Totales:** $${"%,.2f".format(totalExpense)} MXN
                • **Flujo Neto:** $netStr MXN
                
                ${if (net >= 0) "✅ Tienes un balance superavitario saludable este periodo." else "⚠️ Tus egresos superan los ingresos. Te sugiero reducir gastos no esenciales."}
            """.trimIndent()

            return CopilotMessage(
                sender = "copilot",
                text = responseText
            )
        }

        // 2. Check for category spending query (e.g., "¿Cuánto he gastado en alimentos?")
        val matchedCatForQuery = categories.find { lower.contains(it.name.lowercase(Locale.ROOT)) || lower.contains(it.name.split(" ").first().lowercase(Locale.ROOT)) }
        if ((lower.contains("cuanto he gastado") || lower.contains("cuánto gasté") || lower.contains("gasto en")) && matchedCatForQuery != null) {
            val spent = recentTransactions.filter { it.categoryId == matchedCatForQuery.id && it.type == "expense" }.sumOf { it.amount }
            val limitStr = if (matchedCatForQuery.monthlyLimit > 0) " / Límite: $${"%,.2f".format(matchedCatForQuery.monthlyLimit)}" else ""
            val pctStr = if (matchedCatForQuery.monthlyLimit > 0) " (${(spent / matchedCatForQuery.monthlyLimit * 100).toInt()}% consumido)" else ""

            return CopilotMessage(
                sender = "copilot",
                text = "💡 Has gastado **$${"%,.2f".format(spent)} MXN** en **${matchedCatForQuery.name}**$limitStr$pctStr."
            )
        }

        // 3. Transaction Creation Tool schema matching (e.g. "Gasté 450 en Walmart", "Pagué Uber de 180", "Me depositaron nómina de 15,000")
        val amount = extractAmount(lower)
        if (amount != null && amount > 0) {
            val isIncome = lower.contains("deposito") || lower.contains("ingreso") || lower.contains("nómina") ||
                    lower.contains("nomina") || lower.contains("gané") || lower.contains("cobré") || lower.contains("abono")

            val type = if (isIncome) "income" else "expense"
            val merchant = extractMerchant(prompt, isIncome)
            val category = resolveCategory(lower, merchant, isIncome, categories)
            val account = resolveAccount(lower, accounts)

            val isRecurring = lower.contains("recurrente") || lower.contains("cada mes") || lower.contains("mensual") || lower.contains("quincenal")
            val recurrenceFreq = when {
                lower.contains("quincenal") -> "biweekly"
                lower.contains("semanal") -> "weekly"
                lower.contains("anual") -> "yearly"
                isRecurring -> "monthly"
                else -> null
            }

            val toolCall = CopilotToolCall(
                toolName = "create_transaction",
                parsedType = type,
                amount = amount,
                merchant = merchant,
                categoryId = category.id,
                categoryName = category.name,
                accountId = account.id,
                accountName = account.name,
                isRecurring = isRecurring,
                recurrenceFrequency = recurrenceFreq,
                rawExplanation = if (type == "income") "Abono de $${"%,.2f".format(amount)} por $merchant" else "Cargo de $${"%,.2f".format(amount)} en $merchant"
            )

            val responseText = if (isIncome) {
                "He estructurado el ingreso detectado en tu cuenta ${account.name}. Revisa los detalles abajo y pulsa **Confirmar**:"
            } else {
                "Identifiqué un gasto de $${"%,.2f".format(amount)} en $merchant para la categoría **${category.name}**. ¿Deseas asentarlo en tu balance?"
            }

            return CopilotMessage(
                sender = "copilot",
                text = responseText,
                toolCall = toolCall
            )
        }

        // 4. Fallback conversational response
        return CopilotMessage(
            sender = "copilot",
            text = "Comprendo. Puedes dictarme o escribirme cualquier movimiento financiero como:\n\n" +
                    "• *'Gasté $450 en Walmart de despensa'*\n" +
                    "• *'Me depositaron mi quincena de $16,500'*\n" +
                    "• *'Pagué recibo de CFE $620 desde BBVA'*\n" +
                    "• *'¿Cuánto he gastado en Alimentos?'*\n" +
                    "• *'Dame un resumen de mis finanzas'*"
        )
    }

    private fun extractAmount(text: String): Double? {
        val regex = Regex("""(?:\$|\bpesos\b|\bmxn\b)?\s*([0-9]+(?:\.[0-9]{1,2})?)\s*(?:\$|\bpesos\b|\bmxn\b)?""")
        val matches = regex.findAll(text)
        for (m in matches) {
            val numStr = m.groupValues[1]
            val value = numStr.toDoubleOrNull()
            if (value != null && value > 0 && value < 10_000_000) {
                return value
            }
        }
        return null
    }

    private fun extractMerchant(text: String, isIncome: Boolean): String {
        val regexEn = Regex("""(?:en|de|para|a)\s+([A-Za-z0-9\s\.\-_&]{3,25}?)(?=\s+(?:de|por|con|desde|\$|\.|$))""", RegexOption.IGNORE_CASE)
        val match = regexEn.find(text)
        if (match != null) {
            val name = match.groupValues[1].trim()
            if (name.isNotEmpty() && !name.equals("pesos", ignoreCase = true) && !name.equals("mxn", ignoreCase = true)) {
                return name.replaceFirstChar { it.uppercase() }
            }
        }

        val lower = text.lowercase(Locale.ROOT)
        return when {
            isIncome -> "Nómina / Empresa"
            lower.contains("walmart") -> "Walmart"
            lower.contains("oxxo") -> "OXXO"
            lower.contains("uber") -> "Uber"
            lower.contains("starbucks") -> "Starbucks"
            lower.contains("netflix") -> "Netflix"
            lower.contains("cfe") -> "CFE"
            lower.contains("amazon") -> "Amazon"
            lower.contains("cine") -> "Cinépolis"
            else -> "Comercio"
        }
    }

    private fun resolveCategory(
        text: String,
        merchant: String,
        isIncome: Boolean,
        categories: List<CategoryEntity>
    ): CategoryEntity {
        if (isIncome) {
            return categories.find { it.kind == "income" }
                ?: CategoryEntity(id = "cat_nomina", name = "Nómina", kind = "income", icon = "payments", colorHex = "#10B981")
        }

        val lower = (text + " " + merchant).lowercase(Locale.ROOT)
        return categories.find { cat ->
            val catName = cat.name.lowercase(Locale.ROOT)
            when {
                catName.contains("alimento") && (lower.contains("super") || lower.contains("walmart") || lower.contains("oxxo") || lower.contains("comida") || lower.contains("restaurante") || lower.contains("despensa") || lower.contains("starbucks")) -> true
                catName.contains("transporte") && (lower.contains("uber") || lower.contains("gasolina") || lower.contains("didi") || lower.contains("camion") || lower.contains("pasaje")) -> true
                catName.contains("servicio") && (lower.contains("cfe") || lower.contains("luz") || lower.contains("agua") || lower.contains("internet") || lower.contains("totalplay") || lower.contains("renta")) -> true
                catName.contains("entretenimiento") && (lower.contains("netflix") || lower.contains("spotify") || lower.contains("cine") || lower.contains("juego") || lower.contains("fiesta")) -> true
                catName.contains("salud") && (lower.contains("farmacia") || lower.contains("doctor") || lower.contains("medicina") || lower.contains("consulta")) -> true
                else -> false
            }
        } ?: categories.firstOrNull { it.kind == "expense" }
        ?: CategoryEntity(id = "cat_alimentos", name = "Alimentos", kind = "expense", icon = "shopping_cart", colorHex = "#F59E0B")
    }

    private fun resolveAccount(text: String, accounts: List<AccountEntity>): AccountEntity {
        val lower = text.lowercase(Locale.ROOT)
        return accounts.find {
            lower.contains(it.name.lowercase(Locale.ROOT)) ||
            (it.type == "credit" && lower.contains("credito")) ||
            (it.type == "debit" && lower.contains("debito")) ||
            (it.type == "cash" && (lower.contains("efectivo") || lower.contains("cash")))
        } ?: accounts.firstOrNull()
        ?: AccountEntity(id = "acc_default", name = "Cuenta Principal", type = "debit")
    }
}
