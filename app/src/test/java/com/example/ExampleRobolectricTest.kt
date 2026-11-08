package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.service.BankNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AetherPay", appName)
    }

    @Test
    fun `test bank notification parsing BBVA`() {
        val parsed = BankNotificationParser.parseNotification(
            packageName = BankNotificationParser.PKG_BBVA,
            title = "BBVA México",
            text = "Cargo de $450.50 en WALMART con tu tarjeta terminación *4821",
            timestamp = 1700000000000L
        )
        assertNotNull(parsed)
        assertEquals(450.50, parsed!!.amount, 0.01)
        assertEquals("expense", parsed.type)
        assertEquals("4821", parsed.lastFour)
        assertTrue(parsed.dedupHash.isNotEmpty())
    }

    @Test
    fun `test bank notification parsing Mercado Pago transfer sent`() {
        val parsed = BankNotificationParser.parseNotification(
            packageName = BankNotificationParser.PKG_MERCADO_PAGO,
            title = "Mercado Pago",
            text = "Transferiste $850.00 a Maria Gonzalez",
            timestamp = 1700000000000L
        )
        assertNotNull(parsed)
        assertEquals(850.00, parsed!!.amount, 0.01)
        assertEquals("expense", parsed.type)
        assertEquals("Mercado Pago", parsed.bankName)
        assertTrue(parsed.merchant.contains("Maria Gonzalez"))
    }

    @Test
    fun `test bank notification parsing Mercado Pago money received`() {
        val parsed = BankNotificationParser.parseNotification(
            packageName = BankNotificationParser.PKG_MERCADO_PAGO,
            title = "Mercado Pago",
            text = "Recibiste $1,200.00 de Carlos Sanchez",
            timestamp = 1700000000000L
        )
        assertNotNull(parsed)
        assertEquals(1200.00, parsed!!.amount, 0.01)
        assertEquals("income", parsed.type)
        assertEquals("Mercado Pago", parsed.bankName)
        assertTrue(parsed.merchant.contains("Carlos Sanchez"))
    }

    @Test
    fun `test bank notification parsing Mercado Libre purchase`() {
        val parsed = BankNotificationParser.parseNotification(
            packageName = BankNotificationParser.PKG_MERCADO_LIBRE,
            title = "Mercado Libre",
            text = "Pago de $499.00 en Mercado Libre con tarjeta",
            timestamp = 1700000000000L
        )
        assertNotNull(parsed)
        assertEquals(499.00, parsed!!.amount, 0.01)
        assertEquals("expense", parsed.type)
        assertEquals("Mercado Libre", parsed.merchant)
    }
}
