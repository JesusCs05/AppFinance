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
}
