package com.example.domain.copilot

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiCopilotService(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("aetherpay_ai_prefs", Context.MODE_PRIVATE)
    }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    fun getCustomApiKey(): String {
        val userKey = prefs.getString("gemini_api_key", "") ?: ""
        if (userKey.isNotBlank()) return userKey.trim()

        // Fallback to BuildConfig if present
        return try {
            val buildKey = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
            if (!buildKey.isNullOrBlank() && !buildKey.contains("MY_GEMINI_API_KEY")) buildKey else ""
        } catch (_: Throwable) {
            ""
        }
    }

    fun saveCustomApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun clearCustomApiKey() {
        prefs.edit().remove("gemini_api_key").apply()
    }

    fun isOnlineAiConfigured(): Boolean = getCustomApiKey().isNotBlank()

    suspend fun generateFinancialAdvice(
        prompt: String,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>
    ): CopilotMessage = withContext(Dispatchers.IO) {
        val apiKey = getCustomApiKey()

        // If no Gemini API key configured, use the built-in smart offline financial engine
        if (apiKey.isBlank()) {
            return@withContext AiCopilotEngine.processPrompt(prompt, accounts, categories, transactions)
        }

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

            val text = withTimeout(25_000L) {
                callGeminiRestApi(apiKey, systemContext)
            }

            if (!text.isNullOrBlank()) {
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
            Log.w("GeminiCopilot", "Fallback to local offline engine: ${e.message}")
            return@withContext AiCopilotEngine.processPrompt(prompt, accounts, categories, transactions)
        }
    }

    private fun callGeminiRestApi(apiKey: String, systemPrompt: String): String? {
        val payload = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", systemPrompt)
                }))
            }))
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = payload.toString().toRequestBody(mediaType)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GeminiCopilot", "Gemini API error code: ${response.code}")
                return null
            }
            val bodyString = response.body?.string() ?: return null
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text")
                }
            }
            return null
        }
    }
}
