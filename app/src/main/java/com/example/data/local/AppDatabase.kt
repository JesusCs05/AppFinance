package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BankNotificationDraftEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        RecurringRuleEntity::class,
        BankNotificationDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun bankNotificationDao(): BankNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aetherpay.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                INSTANCE?.let { seedDatabase(it) }
                            } catch (e: Throwable) {
                                android.util.Log.e("AppDatabase", "Error seeding database", e)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            val accountDao = database.accountDao()
            val categoryDao = database.categoryDao()
            val transactionDao = database.transactionDao()
            val recurringDao = database.recurringRuleDao()
            val bankDao = database.bankNotificationDao()

            // Seed Accounts
            val bbva = AccountEntity(
                id = "acc_bbva",
                name = "BBVA Débito",
                type = "debit",
                lastFourDigits = "4821",
                balance = 28450.50,
                colorHex = "#004481"
            )
            val nu = AccountEntity(
                id = "acc_nu",
                name = "Nu Tarjeta de Crédito",
                type = "credit",
                lastFourDigits = "9312",
                balance = -3420.00,
                colorHex = "#820AD1"
            )
            val santander = AccountEntity(
                id = "acc_santander",
                name = "Santander Inversión",
                type = "investment",
                lastFourDigits = "1105",
                balance = 145000.00,
                colorHex = "#EC0000"
            )
            val cash = AccountEntity(
                id = "acc_cash",
                name = "Efectivo",
                type = "cash",
                lastFourDigits = "CASH",
                balance = 2150.00,
                colorHex = "#10B981"
            )
            accountDao.insertAccounts(listOf(bbva, nu, santander, cash))

            // Seed Categories
            val catNomina = CategoryEntity(
                id = "cat_nomina",
                name = "Nómina y Salario",
                kind = "income",
                icon = "payments",
                colorHex = "#10B981",
                monthlyLimit = 0.0
            )
            val catInversion = CategoryEntity(
                id = "cat_inversion",
                name = "Rendimientos / Inversión",
                kind = "income",
                icon = "trending_up",
                colorHex = "#06B6D4",
                monthlyLimit = 0.0
            )
            val catAlimentos = CategoryEntity(
                id = "cat_alimentos",
                name = "Alimentos y Supermercado",
                kind = "expense",
                icon = "shopping_cart",
                colorHex = "#F59E0B",
                monthlyLimit = 8000.0
            )
            val catServicios = CategoryEntity(
                id = "cat_servicios",
                name = "Servicios y Hogar",
                kind = "expense",
                icon = "home",
                colorHex = "#3B82F6",
                monthlyLimit = 4500.0
            )
            val catTransporte = CategoryEntity(
                id = "cat_transporte",
                name = "Transporte y Gasolina",
                kind = "expense",
                icon = "directions_car",
                colorHex = "#8B5CF6",
                monthlyLimit = 3000.0
            )
            val catEntretenimiento = CategoryEntity(
                id = "cat_entretenimiento",
                name = "Entretenimiento y Salidas",
                kind = "expense",
                icon = "sports_esports",
                colorHex = "#EC4899",
                monthlyLimit = 2500.0
            )
            val catSalud = CategoryEntity(
                id = "cat_salud",
                name = "Salud y Farmacia",
                kind = "expense",
                icon = "local_hospital",
                colorHex = "#EF4444",
                monthlyLimit = 2000.0
            )
            categoryDao.insertCategories(
                listOf(catNomina, catInversion, catAlimentos, catServicios, catTransporte, catEntretenimiento, catSalud)
            )

            // Seed Seed Transactions
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            val txList = mutableListOf<TransactionEntity>()

            // Current day
            txList.add(
                TransactionEntity(
                    id = "tx_1",
                    accountId = "acc_bbva",
                    categoryId = "cat_alimentos",
                    type = "expense",
                    amount = 450.50,
                    occurredAt = now - 3600_000 * 2,
                    merchant = "Walmart Supercenter",
                    notes = "Despensa semanal de frutas y verduras",
                    source = "bank_notif",
                    idempotencyKey = "tx_walmart_seed"
                )
            )
            txList.add(
                TransactionEntity(
                    id = "tx_2",
                    accountId = "acc_nu",
                    categoryId = "cat_transporte",
                    type = "expense",
                    amount = 185.00,
                    occurredAt = now - 3600_000 * 5,
                    merchant = "Uber Technologies",
                    notes = "Viaje a oficina",
                    source = "manual",
                    idempotencyKey = "tx_uber_seed"
                )
            )

            // 1 day ago
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -1)
            txList.add(
                TransactionEntity(
                    id = "tx_3",
                    accountId = "acc_bbva",
                    categoryId = "cat_entretenimiento",
                    type = "expense",
                    amount = 280.00,
                    occurredAt = cal.timeInMillis,
                    merchant = "Cinépolis VIP",
                    notes = "Boletos y palomitas",
                    source = "ai_voice",
                    idempotencyKey = "tx_cine_seed"
                )
            )

            // 3 days ago (Income)
            cal.add(Calendar.DAY_OF_YEAR, -2)
            txList.add(
                TransactionEntity(
                    id = "tx_4",
                    accountId = "acc_bbva",
                    categoryId = "cat_nomina",
                    type = "income",
                    amount = 26500.00,
                    occurredAt = cal.timeInMillis,
                    merchant = "Empresa Global Tech SA",
                    notes = "Pago de nómina quincenal",
                    source = "bank_notif",
                    idempotencyKey = "tx_nomina_seed"
                )
            )

            // 5 days ago
            cal.add(Calendar.DAY_OF_YEAR, -2)
            txList.add(
                TransactionEntity(
                    id = "tx_5",
                    accountId = "acc_santander",
                    categoryId = "cat_inversion",
                    type = "income",
                    amount = 1420.80,
                    occurredAt = cal.timeInMillis,
                    merchant = "CETES Directo / Inversión",
                    notes = "Intereses devengados mensuales",
                    source = "recurring",
                    idempotencyKey = "tx_cetes_seed"
                )
            )

            // 6 days ago
            cal.add(Calendar.DAY_OF_YEAR, -1)
            txList.add(
                TransactionEntity(
                    id = "tx_6",
                    accountId = "acc_bbva",
                    categoryId = "cat_servicios",
                    type = "expense",
                    amount = 750.00,
                    occurredAt = cal.timeInMillis,
                    merchant = "CFE Suministrador",
                    notes = "Recibo bimestral luz",
                    source = "recurring",
                    idempotencyKey = "tx_cfe_seed"
                )
            )

            transactionDao.insertTransactions(txList)

            // Seed Recurring Rules
            recurringDao.insertRecurringRule(
                RecurringRuleEntity(
                    id = "rec_netflix",
                    accountId = "acc_nu",
                    categoryId = "cat_entretenimiento",
                    merchant = "Netflix Premium",
                    amount = 299.00,
                    frequency = "monthly",
                    dayOfMonth = 15,
                    autoCommit = true,
                    isActive = true
                )
            )
            recurringDao.insertRecurringRule(
                RecurringRuleEntity(
                    id = "rec_internet",
                    accountId = "acc_bbva",
                    categoryId = "cat_servicios",
                    merchant = "Totalplay Fibra Óptica",
                    amount = 689.00,
                    frequency = "monthly",
                    dayOfMonth = 5,
                    autoCommit = true,
                    isActive = true
                )
            )

            // Seed Bank Notification Drafts for the live listener inbox
            val sampleDraft = BankNotificationDraftEntity(
                id = "draft_bbva_1",
                bankPackage = "com.bbva.bbvamx",
                bankName = "BBVA México",
                detectedType = "expense",
                detectedAmount = 890.00,
                detectedMerchant = "Costco Wholesale",
                suggestedCategoryId = "cat_alimentos",
                dedupHash = "hash_bbva_costco_${System.currentTimeMillis() / 180000}",
                status = "pending",
                occurredAt = now - 1800_000,
                rawMessage = "BBVA: Cargo de \$890.00 en COSTCO WHOLESALE con tu tarjeta débito terminación *4821 el 08/Nov.",
                accountLastFour = "4821"
            )
            bankDao.insertDraft(sampleDraft)
        }
    }
}
