package com.example.data.service

import com.example.data.model.BankNotificationDraftEntity
import java.security.MessageDigest
import java.util.Locale
import java.util.regex.Pattern

object BankNotificationParser {

    // Supported Bank and Fintech package identifiers
    const val PKG_BBVA = "com.bbva.bbvamx"
    const val PKG_BANORTE = "mx.banorte"
    const val PKG_NU = "com.nu.production"
    const val PKG_SANTANDER = "com.santander.mx"
    const val PKG_BANAMEX = "com.citibanamex.movil"
    const val PKG_MERCADO_PAGO = "com.mercadopago.wallet"
    const val PKG_MERCADO_LIBRE = "com.mercadolibre"

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
                packageName.contains("santander", ignoreCase = true) ||
                packageName.contains("banamex", ignoreCase = true) ||
                packageName.contains("citibanamex", ignoreCase = true) ||
                packageName.contains("mercadopago", ignoreCase = true) ||
                packageName.contains("mercadolibre", ignoreCase = true)
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
            lower.contains("mercadopago") || lower.contains("mercado pago") || lower.contains("mercadolibre") -> PKG_MERCADO_PAGO to "Mercado Pago"
            lower.contains("bbva") -> PKG_BBVA to "BBVA México"
            lower.contains("banorte") -> PKG_BANORTE to "Banorte Móvil"
            lower.contains("nu") || lower.contains("nubank") -> PKG_NU to "Nu Tarjeta"
            lower.contains("santander") -> PKG_SANTANDER to "Santander México"
            lower.contains("banamex") || lower.contains("citibanamex") -> PKG_BANAMEX to "Citibanamex Móvil"
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
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
        return when {
            lower.contains("deposito") || lower.contains("abono") || lower.contains("recibiste") ||
            lower.contains("te transfirieron") || lower.contains("te enviaron") ||
            lower.contains("dinero recibido") || lower.contains("ingreso de dinero") ||
            lower.contains("transferencia recibida") || lower.contains("nomina") ||
            lower.contains("ingreso") -> "income"
            else -> "expense"
        }
    }

    private fun extractMerchant(content: String, bankName: String): String {
        val lower = content.lowercase(Locale.ROOT)
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")

        // Mercado Pago specific patterns
        if (bankName.contains("Mercado Pago", ignoreCase = true)) {
            val transferToRegex = Regex("""(?:transferiste|enviaste)\b.*?a\s+([A-Za-z\s]{3,24})""", RegexOption.IGNORE_CASE)
            val toMatch = transferToRegex.find(content)
            if (toMatch != null) {
                val name = toMatch.groupValues[1].trim().trimEnd('.', ',', '!')
                return "Transferencia a ${name.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar(Char::uppercase) }}"
            }

            val transferFromRegex = Regex("""(?:te transfirieron|recibiste|te enviaron|ingreso)\b.*?de\s+([A-Za-z\s]{3,24})""", RegexOption.IGNORE_CASE)
            val fromMatch = transferFromRegex.find(content)
            if (fromMatch != null) {
                val name = fromMatch.groupValues[1].trim().trimEnd('.', ',', '!')
                return "Transferencia de ${name.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar(Char::uppercase) }}"
            }

            if (lower.contains("mercado libre")) return "Mercado Libre"
        }

        // Check canonical merchants first
        when {
            lower.contains("walmart") -> return "Walmart Supercenter"
            lower.contains("oxxo") -> return "OXXO Tiendas"
            lower.contains("uber") -> return "Uber Technologies"
            lower.contains("amazon") -> return "Amazon México"
            lower.contains("liverpool") -> return "Liverpool"
            lower.contains("starbucks") -> return "Starbucks Coffee"
            lower.contains("netflix") -> return "Netflix"
            lower.contains("spotify") -> return "Spotify"
            lower.contains("costco") -> return "Costco Wholesale"
            lower.contains("cfe") -> return "CFE Electricidad"
            lower.contains("farmacia") -> return "Farmacias del Ahorro"
            lower.contains("cajero") || lower.contains("retiro") -> return "Retiro ATM"
            lower.contains("mercado pago") -> return "Mercado Pago"
        }

        // Patterns like "en OXXO", "en WALMART", "compra en LIVERPOOL", "en AMAZON con tarjeta"
        val regexEn = Regex("""(?:en|de|para|comercio:?)\s+([A-Za-z0-9\s\.\-_&]{3,28}?)(?=\s+(?:con|el|por|al|tarjeta|desde|\.|\$|\*|$))""", RegexOption.IGNORE_CASE)
        val match = regexEn.find(content)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
            if (candidate.length in 3..25 && !candidate.equals(bankName, ignoreCase = true)) {
                return candidate.split(" ").joinToString(" ") { word ->
                    word.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
                }
            }
        }

        return if (bankName.contains("Mercado Pago", ignoreCase = true)) "Mercado Pago" else "Comercio Detectado"
    }

    private fun extractLastFour(content: String): String {
        // Matches *1234 or tarjeta 1234 or terminacion 1234
        val regex = Regex("""(?:\*|terminaci[oó]n|cuenta|tarjeta)?\s*\*?(\d{4})\b""", RegexOption.IGNORE_CASE)
        val match = regex.find(content)
        return match?.groupValues?.get(1) ?: "MP"
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
            lower.contains("liverpool") || lower.contains("playstation") ||
            lower.contains("mercado libre") -> "cat_entretenimiento"

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
