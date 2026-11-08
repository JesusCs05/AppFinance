package com.example.data.service

import com.example.data.model.BankNotificationDraftEntity
import java.security.MessageDigest
import java.util.Locale
import java.util.regex.Pattern

object BankNotificationParser {

    // Supported Bank package identifiers
    const val PKG_BBVA = "com.bbva.bbvamx"
    const val PKG_BANORTE = "mx.banorte"
    const val PKG_NU = "com.nu.production"
    const val PKG_SANTANDER = "com.santander.mx"

    data class ParsedBankResult(
        val bankPackage: String,
        val bankName: String,
        val type: String, // income, expense
        val amount: Double,
        val merchant: String,
        val lastFour: String,
        val suggestedCategoryId: String,
        val dedupHash: String,
        val rawText: String
    )

    fun isAllowedPackage(packageName: String): Boolean {
        return packageName.contains("bbva", ignoreCase = true) ||
                packageName.contains("banorte", ignoreCase = true) ||
                packageName.contains("nu", ignoreCase = true) ||
                packageName.contains("santander", ignoreCase = true)
    }

    fun parseNotification(packageName: String, title: String, text: String, timestamp: Long): ParsedBankResult? {
        val fullContent = "$title $text".trim()
        if (fullContent.isEmpty()) return null

        val bankInfo = resolveBankInfo(packageName, fullContent)
        val amount = extractAmount(fullContent) ?: return null
        val type = extractType(fullContent)
        val merchant = extractMerchant(fullContent, bankInfo.second)
        val lastFour = extractLastFour(fullContent)
        val suggestedCategory = suggestCategory(merchant, type)

        // Compute deterministic SHA-256: hash(bank + amount + merchant + floor(timestamp / 180s))
        val bucketTimestamp = timestamp / 180_000L
        val rawHashPayload = "${bankInfo.first}:$amount:$merchant:$bucketTimestamp"
        val dedupHash = computeSha256(rawHashPayload)

        return ParsedBankResult(
            bankPackage = bankInfo.first,
            bankName = bankInfo.second,
            type = type,
            amount = amount,
            merchant = merchant,
            lastFour = lastFour,
            suggestedCategoryId = suggestedCategory,
            dedupHash = dedupHash,
            rawText = fullContent
        )
    }

    private fun resolveBankInfo(pkg: String, content: String): Pair<String, String> {
        val lower = (pkg + " " + content).lowercase(Locale.ROOT)
        return when {
            lower.contains("bbva") -> PKG_BBVA to "BBVA México"
            lower.contains("banorte") -> PKG_BANORTE to "Banorte Móvil"
            lower.contains("nu") || lower.contains("nubank") -> PKG_NU to "Nu Tarjeta"
            lower.contains("santander") -> PKG_SANTANDER to "Santander México"
            else -> "com.general.bank" to "Banco Notificación"
        }
    }

    private fun extractAmount(content: String): Double? {
        // Matches $1,234.56 or $ 450.00 or 1234.50 MXN
        val regex = Regex("""(?:\$|\bMXN\s*|\bUSD\s*)\s*([\d,]+(?:\.\d{1,2})?)|\b([\d,]+(?:\.\d{2}))\s*(?:pesos|mxn|usd)""", RegexOption.IGNORE_CASE)
        val match = regex.find(content)
        if (match != null) {
            val raw = match.groupValues[1].ifEmpty { match.groupValues[2] }
            val cleaned = raw.replace(",", "")
            return cleaned.toDoubleOrNull()
        }

        // Generic fallback for any price pattern
        val genericRegex = Regex("""(\d{1,3}(?:,\d{3})*(?:\.\d{2}))""")
        val genericMatch = genericRegex.find(content)
        return genericMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractType(content: String): String {
        val lower = content.lowercase(Locale.ROOT)
        return when {
            lower.contains("deposito") || lower.contains("abono") || lower.contains("recibiste") ||
            lower.contains("transferencia recibida") || lower.contains("nomina") -> "income"
            else -> "expense"
        }
    }

    private fun extractMerchant(content: String, bankName: String): String {
        // Patterns like "en OXXO", "en WALMART", "compra en LIVERPOOL", "en AMAZON con tarjeta"
        val regexEn = Regex("""(?:en|de|para|comercio:?)\s+([A-Za-z0-9\s\.\-_&]{3,28}?)(?=\s+(?:con|el|por|al|tarjeta|desde|\.|\$|\*|$))""", RegexOption.IGNORE_CASE)
        val match = regexEn.find(content)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
            if (candidate.length in 3..25 && !candidate.equals(bankName, ignoreCase = true)) {
                return candidate.replaceFirstChar { it.uppercase() }
            }
        }

        // Fallbacks based on common merchants
        val lower = content.lowercase(Locale.ROOT)
        return when {
            lower.contains("walmart") -> "Walmart Supercenter"
            lower.contains("oxxo") -> "OXXO Tiendas"
            lower.contains("uber") -> "Uber Technologies"
            lower.contains("amazon") -> "Amazon México"
            lower.contains("liverpool") -> "Liverpool"
            lower.contains("starbucks") -> "Starbucks Coffee"
            lower.contains("netflix") -> "Netflix"
            lower.contains("spotify") -> "Spotify"
            lower.contains("costco") -> "Costco Wholesale"
            lower.contains("cfe") -> "CFE Electricidad"
            lower.contains("farmacia") -> "Farmacias del Ahorro"
            lower.contains("cajero") || lower.contains("retiro") -> "Retiro ATM"
            else -> "Comercio Detectado"
        }
    }

    private fun extractLastFour(content: String): String {
        // Matches *1234 or tarjeta 1234 or terminacion 1234
        val regex = Regex("""(?:\*|terminaci[oó]n|cuenta|tarjeta)?\s*\*?(\d{4})\b""", RegexOption.IGNORE_CASE)
        val match = regex.find(content)
        return match?.groupValues?.get(1) ?: "4821"
    }

    private fun suggestCategory(merchant: String, type: String): String {
        if (type == "income") return "cat_nomina"
        val lower = merchant.lowercase(Locale.ROOT)
        return when {
            lower.contains("walmart") || lower.contains("oxxo") || lower.contains("costco") ||
            lower.contains("super") || lower.contains("soriana") || lower.contains("chedraui") ||
            lower.contains("starbucks") || lower.contains("restaurante") -> "cat_alimentos"

            lower.contains("uber") || lower.contains("gasolina") || lower.contains("didi") ||
            lower.contains("pemex") || lower.contains("shell") -> "cat_transporte"

            lower.contains("netflix") || lower.contains("spotify") || lower.contains("cine") ||
            lower.contains("liverpool") || lower.contains("playstation") -> "cat_entretenimiento"

            lower.contains("cfe") || lower.contains("totalplay") || lower.contains("telmex") ||
            lower.contains("renta") || lower.contains("luz") || lower.contains("agua") -> "cat_servicios"

            lower.contains("farmacia") || lower.contains("hospital") || lower.contains("doctor") ||
            lower.contains("salud") -> "cat_salud"

            else -> "cat_alimentos"
        }
    }

    private fun computeSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
