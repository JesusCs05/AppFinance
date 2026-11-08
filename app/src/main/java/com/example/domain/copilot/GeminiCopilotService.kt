package com.example.domain.copilot

import android.content.Context
import android.util.Log
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

class GeminiCopilotService(private val context: Context) {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun generateFinancialAdvice(
        prompt: String,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>
    ): CopilotMessage = withContext(Dispatchers.IO) {
        try {
            // Compute financial context summary
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val net = totalIncome - totalExpense

            val systemContext = """
                Eres el Copiloto Financiero IA de AetherPay. Tu tono es ejecutivo, analítico, conciso y motivador.
                Información financiera actual del usuario:
                - Balance Neto Consolidado: ${"$%,.2f".format(net)} MXN
                - Ingresos Totales: ${"$%,.2f".format(totalIncome)} MXN
                - Gastos Totales: ${"$%,.2f".format(totalExpense)} MXN
                - Cuentas activas: ${accounts.joinToString { "${it.name}: $${it.balance}" }}
                - Categorías principales: ${categories.joinToString { it.name }}
                
                Pregunta del usuario: "$prompt"
                
                Instrucciones:
                1. Responde en español de forma directa, breve (1 a 3 párrafos como máximo o viñetas claras).
                2. Usa emojis relevantes (📊, 💡, 💰, ⚠️) con buen gusto y cifras con formato monetario (${'$'}X,XXX.XX MXN).
                3. Si el usuario describe un gasto o ingreso nuevo, explícale que puede asentarlo y añade un consejo de ahorro.
            """.trimIndent()

            val response = withTimeout(40_000L) {
                model.generateContent(systemContext)
            }

            val text = response.text
            if (!text.isNullOrBlank()) {
                // Check if this was a transaction creation intent
                val localParsed = AiCopilotEngine.processPrompt(prompt, accounts, categories, transactions)
                if (localParsed.toolCall != null) {
                    return@withContext CopilotMessage(
                        sender = "copilot",
                        text = text,
                        toolCall = localParsed.toolCall
                    )
                }

                return@withContext CopilotMessage(
                    sender = "copilot",
                    text = text
                )
            } else {
                return@withContext AiCopilotEngine.processPrompt(prompt, accounts, categories, transactions)
            }
        } catch (e: Exception) {
            Log.w("GeminiCopilot", "Fallback to local engine: ${e.message}")
            return@withContext AiCopilotEngine.processPrompt(prompt, accounts, categories, transactions)
        }
    }
}
