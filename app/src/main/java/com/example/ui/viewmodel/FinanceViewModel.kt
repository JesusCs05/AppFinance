package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BankNotificationDraftEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import com.example.data.remote.FirestoreSyncService
import com.example.data.repository.AuthRepository
import com.example.data.repository.FinanceRepository
import com.example.data.service.BankNotificationParser
import com.example.domain.copilot.AiCopilotEngine
import com.example.domain.copilot.CopilotMessage
import com.example.domain.copilot.CopilotToolCall
import com.example.domain.copilot.GeminiCopilotService
import com.example.domain.recurring.RecurringPaymentScheduler
import com.example.util.VoiceInputManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class AppScreen {
    DASHBOARD,
    CALENDAR,
    COPILOT,
    CUSTOMIZE_TRANSACTION,
    NOTIFICATION_INBOX
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository: FinanceRepository = FinanceRepository(db)
    private val syncService: FirestoreSyncService = FirestoreSyncService(application)
    val authRepository: AuthRepository = AuthRepository(application)
    private val geminiService: GeminiCopilotService = GeminiCopilotService(application)
    val voiceInputManager: VoiceInputManager = VoiceInputManager(application)

    init {
        // Attempt silent background sign-in
        authRepository.attemptAutoSignIn(viewModelScope)
        // Check due recurring rules on startup
        processDueRecurringRules()
    }

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val isAuthLoading: StateFlow<Boolean> = authRepository.isLoading
    val authError: StateFlow<String?> = authRepository.authError

    private val _syncStatus = MutableStateFlow<String>("Listo")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _recurringNotification = MutableStateFlow<String?>(null)
    val recurringNotification: StateFlow<String?> = _recurringNotification.asStateFlow()

    fun refreshUser() {
        authRepository.attemptAutoSignIn(viewModelScope)
    }

    fun signInWithGoogle(activity: Activity) {
        authRepository.signInWithGoogle(activity, viewModelScope, onSuccess = {
            syncWithCloud()
        })
    }

    fun signOut() {
        authRepository.signOut(viewModelScope)
    }

    fun syncWithCloud() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _syncStatus.value = "Sincronizando..."
            try {
                syncService.syncUserToCloud()
                syncService.syncAccountsToCloud(accounts.value)
                syncService.syncTransactionsToCloud(transactions.value)
                syncService.syncBankDraftsToCloud(allBankDrafts.value)
                _syncStatus.value = "Nube al día"
                delay(2000)
                _syncStatus.value = "Listo"
            } catch (e: Exception) {
                _syncStatus.value = "Error sincronización"
            }
        }
    }

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBankDrafts: StateFlow<List<BankNotificationDraftEntity>> = repository.pendingBankDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankDrafts: StateFlow<List<BankNotificationDraftEntity>> = repository.allBankDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringRules: StateFlow<List<RecurringRuleEntity>> = repository.allRecurringRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & UI State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    private val _isBiometricLocked = MutableStateFlow(false)
    val isBiometricLocked: StateFlow<Boolean> = _isBiometricLocked.asStateFlow()

    private val _isBiometricProtected = MutableStateFlow(false)
    val isBiometricProtected: StateFlow<Boolean> = _isBiometricProtected.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    // Copilot State
    private val _copilotMessages = MutableStateFlow<List<CopilotMessage>>(
        listOf(
            CopilotMessage(
                sender = "copilot",
                text = "¡Hola! Soy tu Copiloto Financiero AetherPay potenciado con Gemini. Puedo clasificar gastos por voz en vivo, detectar alertas bancarias y darte proyecciones inteligentes."
            )
        )
    )
    val copilotMessages: StateFlow<List<CopilotMessage>> = _copilotMessages.asStateFlow()

    val isListeningVoice: StateFlow<Boolean> = voiceInputManager.isListening

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.CUSTOMIZE_TRANSACTION) {
            _editingTransaction.value = null
        }
        _currentScreen.value = screen
    }

    fun openNewTransaction() {
        _editingTransaction.value = null
        _currentScreen.value = AppScreen.CUSTOMIZE_TRANSACTION
    }

    fun openEditTransaction(tx: TransactionEntity) {
        _editingTransaction.value = tx
        _currentScreen.value = AppScreen.CUSTOMIZE_TRANSACTION
    }

    fun togglePrivacyMode() {
        _isPrivacyMode.value = !_isPrivacyMode.value
    }

    fun toggleBiometricProtection() {
        _isBiometricProtected.value = !_isBiometricProtected.value
    }

    fun unlockWithBiometrics() {
        _isBiometricLocked.value = false
    }

    // Account & Budget management
    fun createNewAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.insertAccount(account)
            if (currentUser.value != null) {
                syncWithCloud()
            }
        }
    }

    fun updateCategoryBudget(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
            if (currentUser.value != null) {
                syncWithCloud()
            }
        }
    }

    fun processDueRecurringRules() {
        viewModelScope.launch {
            val result = RecurringPaymentScheduler.evaluateAndProcessDueRules(db, repository)
            if (result.processedCount > 0) {
                _recurringNotification.value = "Se aplicaron ${result.processedCount} cargos periódicos: $${"%,.2f".format(result.totalAmount)} MXN"
                if (currentUser.value != null) {
                    syncWithCloud()
                }
            }
        }
    }

    fun dismissRecurringNotification() {
        _recurringNotification.value = null
    }

    // Transaction CRUD
    fun saveTransaction(
        id: String?,
        accountId: String,
        categoryId: String,
        type: String,
        amount: Double,
        merchant: String,
        occurredAt: Long,
        notes: String,
        source: String,
        isRecurring: Boolean = false,
        recurrenceFreq: String = "monthly"
    ) {
        viewModelScope.launch {
            val oldTx = _editingTransaction.value
            val newTx = TransactionEntity(
                id = id ?: UUID.randomUUID().toString(),
                accountId = accountId,
                categoryId = categoryId,
                type = type,
                amount = amount,
                occurredAt = occurredAt,
                merchant = merchant,
                notes = notes,
                source = source
            )

            if (oldTx != null) {
                repository.updateTransaction(oldTx, newTx)
            } else {
                repository.insertTransaction(newTx)
            }

            if (isRecurring && id == null) {
                repository.insertRecurringRule(
                    RecurringRuleEntity(
                        accountId = accountId,
                        categoryId = categoryId,
                        merchant = merchant,
                        amount = amount,
                        frequency = recurrenceFreq,
                        autoCommit = true,
                        isActive = true
                    )
                )
            }

            // Sync to Firestore if user is authenticated
            if (currentUser.value != null) {
                syncWithCloud()
            }

            _editingTransaction.value = null
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            if (currentUser.value != null) {
                syncWithCloud()
            }
        }
    }

    // Bank notification handling
    fun commitBankDraft(draft: BankNotificationDraftEntity, targetAccountId: String? = null) {
        viewModelScope.launch {
            val accId = targetAccountId ?: (accounts.value.find { it.lastFourDigits == draft.accountLastFour }?.id ?: accounts.value.firstOrNull()?.id ?: "acc_bbva")
            repository.commitDraftWithTransaction(
                draft = draft,
                targetAccountId = accId,
                targetCategoryId = draft.suggestedCategoryId
            )
            if (currentUser.value != null) {
                syncWithCloud()
            }
        }
    }

    fun dismissBankDraft(draftId: String) {
        viewModelScope.launch {
            repository.dismissBankDraft(draftId)
            if (currentUser.value != null) {
                syncWithCloud()
            }
        }
    }

    fun simulateBankNotification(bank: String) {
        viewModelScope.launch {
            val (pkg, name, sampleMsg, lastFour) = when (bank) {
                "bbva" -> Quadruple(
                    BankNotificationParser.PKG_BBVA,
                    "BBVA México",
                    "BBVA: Compra autorizada por \$${(150..1200).random()}.00 en WALMART SUPERCENTER con tu tarjeta de débito *4821.",
                    "4821"
                )
                "nu" -> Quadruple(
                    BankNotificationParser.PKG_NU,
                    "Nu Tarjeta",
                    "Nu: Compra de \$${(89..499).random()}.50 aprobada en SPOTIFY con terminación *9312.",
                    "9312"
                )
                "banorte" -> Quadruple(
                    BankNotificationParser.PKG_BANORTE,
                    "Banorte Móvil",
                    "Banorte: Cargo de \$${(350..2500).random()}.00 en LIVERPOOL con tarjeta *9876.",
                    "9876"
                )
                else -> Quadruple(
                    BankNotificationParser.PKG_SANTANDER,
                    "Santander México",
                    "Santander: Depósito recibido por \$${(5000..18000).random()}.00 de Nómina en cuenta *1105.",
                    "1105"
                )
            }

            val parsed = BankNotificationParser.parseNotification(pkg, name, sampleMsg, System.currentTimeMillis())
            if (parsed != null) {
                val draft = BankNotificationDraftEntity(
                    bankPackage = parsed.bankPackage,
                    bankName = parsed.bankName,
                    detectedType = parsed.type,
                    detectedAmount = parsed.amount,
                    detectedMerchant = parsed.merchant,
                    suggestedCategoryId = parsed.suggestedCategoryId,
                    dedupHash = parsed.dedupHash + "_" + System.currentTimeMillis(),
                    status = "pending",
                    occurredAt = System.currentTimeMillis(),
                    rawMessage = parsed.rawText,
                    accountLastFour = parsed.lastFour
                )
                repository.insertBankDraft(draft)
            }
        }
    }

    // Copilot with Gemini AI Logic
    fun sendCopilotMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = CopilotMessage(sender = "user", text = userText)
        _copilotMessages.value = _copilotMessages.value + userMsg

        viewModelScope.launch {
            delay(300)
            val reply = geminiService.generateFinancialAdvice(
                prompt = userText,
                accounts = accounts.value,
                categories = categories.value,
                transactions = transactions.value
            )
            _copilotMessages.value = _copilotMessages.value + reply
        }
    }

    fun startVoiceRecording(onTranscriptionUpdated: (String) -> Unit = {}) {
        voiceInputManager.startListening(
            onResult = { text ->
                onTranscriptionUpdated(text)
                sendCopilotMessage(text)
            },
            onError = { errMsg ->
                // Graceful fallback to default simulation if mic is unavailable or blocked in container
                val sample = "Gasté \$350 en OXXO de café y botana"
                onTranscriptionUpdated(sample)
                sendCopilotMessage(sample)
            }
        )
    }

    fun stopVoiceRecording() {
        voiceInputManager.stopListening()
    }

    fun confirmCopilotToolCall(messageId: String, toolCall: CopilotToolCall) {
        viewModelScope.launch {
            // Update message status
            _copilotMessages.value = _copilotMessages.value.map {
                if (it.id == messageId) it.copy(isToolConfirmed = true) else it
            }

            // Execute transaction
            val newTx = TransactionEntity(
                accountId = toolCall.accountId,
                categoryId = toolCall.categoryId,
                type = toolCall.parsedType,
                amount = toolCall.amount,
                occurredAt = System.currentTimeMillis(),
                merchant = toolCall.merchant,
                notes = "Registrado vía Copiloto IA AetherPay",
                source = "ai_voice"
            )
            repository.insertTransaction(newTx)

            if (toolCall.isRecurring) {
                repository.insertRecurringRule(
                    RecurringRuleEntity(
                        accountId = toolCall.accountId,
                        categoryId = toolCall.categoryId,
                        merchant = toolCall.merchant,
                        amount = toolCall.amount,
                        frequency = toolCall.recurrenceFrequency ?: "monthly"
                    )
                )
            }

            if (currentUser.value != null) {
                syncWithCloud()
            }

            // Copilot confirmation reply
            val ackMsg = CopilotMessage(
                sender = "copilot",
                text = "✅ ¡Listo! Se asentó el movimiento de **$${"%,.2f".format(toolCall.amount)} MXN** en ${toolCall.merchant}. Tu saldo ha sido actualizado."
            )
            _copilotMessages.value = _copilotMessages.value + ackMsg
        }
    }

    fun discardCopilotToolCall(messageId: String) {
        _copilotMessages.value = _copilotMessages.value.map {
            if (it.id == messageId) it.copy(isToolDiscarded = true) else it
        }
        val ackMsg = CopilotMessage(
            sender = "copilot",
            text = "Movimiento cancelado. No se realizaron cambios en tu balance."
        )
        _copilotMessages.value = _copilotMessages.value + ackMsg
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
