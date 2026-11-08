package com.example

import com.example.data.service.BankNotificationParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAllFiveMexicanBanksParsing() {
    val now = System.currentTimeMillis()

    // 1. BBVA
    val bbva = BankNotificationParser.parseNotification(
      packageName = "com.bbva.bbvamx",
      title = "BBVA México",
      text = "BBVA: Compra autorizada por $650.00 en WALMART SUPERCENTER con tarjeta *4821.",
      timestamp = now
    )
    assertNotNull(bbva)
    assertEquals("BBVA México", bbva?.bankName)
    assertEquals(650.0, bbva?.amount ?: 0.0, 0.001)
    assertEquals("Walmart Supercenter", bbva?.merchant)
    assertEquals("4821", bbva?.lastFour)
    assertEquals("expense", bbva?.type)

    // 2. Nu
    val nu = BankNotificationParser.parseNotification(
      packageName = "com.nu.production",
      title = "Nu",
      text = "Nu: Compra de $129.00 aprobada en SPOTIFY con terminación *9312.",
      timestamp = now
    )
    assertNotNull(nu)
    assertEquals("Nu Tarjeta", nu?.bankName)
    assertEquals(129.0, nu?.amount ?: 0.0, 0.001)
    assertEquals("Spotify", nu?.merchant)
    assertEquals("9312", nu?.lastFour)

    // 3. Banorte
    val banorte = BankNotificationParser.parseNotification(
      packageName = "mx.banorte",
      title = "Banorte Móvil",
      text = "Banorte: Cargo de $450.00 en OXXO con tarjeta *9876.",
      timestamp = now
    )
    assertNotNull(banorte)
    assertEquals("Banorte Móvil", banorte?.bankName)
    assertEquals(450.0, banorte?.amount ?: 0.0, 0.001)
    assertEquals("OXXO Tiendas", banorte?.merchant)
    assertEquals("9876", banorte?.lastFour)

    // 4. Santander
    val santander = BankNotificationParser.parseNotification(
      packageName = "com.santander.mx",
      title = "Santander",
      text = "Santander: Depósito recibido por $8500.00 de Nómina en cuenta *1105.",
      timestamp = now
    )
    assertNotNull(santander)
    assertEquals("Santander México", santander?.bankName)
    assertEquals(8500.0, santander?.amount ?: 0.0, 0.001)
    assertEquals("income", santander?.type)

    // 5. Citibanamex
    val banamex = BankNotificationParser.parseNotification(
      packageName = "com.citibanamex.movil",
      title = "Citibanamex",
      text = "Citibanamex: Compra por $280.00 en UBER con tu tarjeta de débito *3456.",
      timestamp = now
    )
    assertNotNull(banamex)
    assertEquals("Citibanamex Móvil", banamex?.bankName)
    assertEquals(280.0, banamex?.amount ?: 0.0, 0.001)
    assertEquals("Uber Technologies", banamex?.merchant)
    assertEquals("3456", banamex?.lastFour)
    assertEquals("cat_transporte", banamex?.suggestedCategoryId)
  }

  @Test
  fun testDeduplicationSha256() {
    val now = 1700000000000L
    val res1 = BankNotificationParser.parseNotification(
      packageName = "com.bbva.bbvamx",
      title = "BBVA",
      text = "Compra por $500.00 en AMAZON *1234",
      timestamp = now
    )
    val res2 = BankNotificationParser.parseNotification(
      packageName = "com.bbva.bbvamx",
      title = "BBVA",
      text = "Compra por $500.00 en AMAZON *1234",
      timestamp = now + 10_000L // 10s later in same window
    )
    assertNotNull(res1)
    assertNotNull(res2)
    assertEquals(res1?.dedupHash, res2?.dedupHash)
  }
}
