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
                    "aetherpay_clean.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                INSTANCE?.let { seedDatabase(it) }
                            } catch (e: Throwable) {
                                android.util.Log.e("AppDatabase", "Error seeding initial categories", e)
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
            val categoryDao = database.categoryDao()

            // Preload only default standard categories with 0 initial spend/limit
            // Starting with ZERO accounts, ZERO transactions, ZERO drafts
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
                monthlyLimit = 0.0
            )
            val catServicios = CategoryEntity(
                id = "cat_servicios",
                name = "Servicios y Hogar",
                kind = "expense",
                icon = "home",
                colorHex = "#3B82F6",
                monthlyLimit = 0.0
            )
            val catTransporte = CategoryEntity(
                id = "cat_transporte",
                name = "Transporte y Gasolina",
                kind = "expense",
                icon = "directions_car",
                colorHex = "#8B5CF6",
                monthlyLimit = 0.0
            )
            val catEntretenimiento = CategoryEntity(
                id = "cat_entretenimiento",
                name = "Entretenimiento y Salidas",
                kind = "expense",
                icon = "sports_esports",
                colorHex = "#EC4899",
                monthlyLimit = 0.0
            )
            val catSalud = CategoryEntity(
                id = "cat_salud",
                name = "Salud y Farmacia",
                kind = "expense",
                icon = "local_hospital",
                colorHex = "#EF4444",
                monthlyLimit = 0.0
            )
            categoryDao.insertCategories(
                listOf(catNomina, catInversion, catAlimentos, catServicios, catTransporte, catEntretenimiento, catSalud)
            )
        }
    }
}
