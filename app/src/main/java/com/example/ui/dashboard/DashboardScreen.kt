package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountEntity
import com.example.data.model.BankNotificationDraftEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AetherButton
import com.example.ui.components.AetherCard
import com.example.ui.components.CurrencyText
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MetallicSilverLight
import com.example.ui.theme.MetallicSilverMuted
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalIndigo
import com.example.ui.theme.RoyalIndigoLight
import com.example.ui.theme.RubyExpense
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val pendingDrafts by viewModel.pendingBankDrafts.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()
    val isBiometricProtected by viewModel.isBiometricProtected.collectAsStateWithLifecycle()

    val totalNetWorth = accounts.sumOf { it.balance }
    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(RoyalIndigo, VioletAccent)
                                )
                            )
                            .border(1.dp, MetallicSilverMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Æ",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MetallicSilverLight
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AETHERPAY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = MetallicSilverLight
                        )
                        Text(
                            text = "Local-First Core & AI Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicSilverMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.togglePrivacyMode() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Privacidad",
                            tint = if (isPrivacyMode) AmberAlert else MetallicSilverMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleBiometricProtection() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isBiometricProtected) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = "Biometría",
                            tint = if (isBiometricProtected) EmeraldIncome else MetallicSilverMuted
                        )
                    }
                }
            }
        }

        // Hero Balance Card
        item {
            HeroBalanceCard(
                netWorth = totalNetWorth,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                isPrivacyHidden = isPrivacyMode,
                onAddTransaction = { viewModel.openNewTransaction() },
                onOpenCopilot = { viewModel.navigateTo(AppScreen.COPILOT) }
            )
        }

        // Pending Bank Notification Alert Banner (if any)
        if (pendingDrafts.isNotEmpty()) {
            item {
                PendingBankBanner(
                    drafts = pendingDrafts,
                    onConfirm = { draft -> viewModel.commitBankDraft(draft) },
                    onDismiss = { draft -> viewModel.dismissBankDraft(draft.id) },
                    onViewAll = { viewModel.navigateTo(AppScreen.NOTIFICATION_INBOX) }
                )
            }
        }

        // Quick Bank Simulation & Action Trigger Bar
        item {
            BankSimulationTriggerRow(
                onSimulateBank = { bank -> viewModel.simulateBankNotification(bank) },
                onOpenInbox = { viewModel.navigateTo(AppScreen.NOTIFICATION_INBOX) }
            )
        }

        // Accounts Carousel
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Cuentas & Tarjetas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MetallicSilverLight
                    )
                    Text(
                        text = "${accounts.size} activas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MetallicSilverMuted
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts) { account ->
                        AccountPillCard(
                            account = account,
                            isPrivacyHidden = isPrivacyMode
                        )
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Movimientos Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MetallicSilverLight
                )
                Text(
                    text = "Ver calendario",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalIndigoLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.CALENDAR) }
                )
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            item {
                AetherCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin movimientos registrados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MetallicSilverMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AetherButton(
                            text = "Crear primer movimiento",
                            onClick = { viewModel.openNewTransaction() },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            }
        } else {
            items(transactions.take(15)) { tx ->
                val category = categories.find { it.id == tx.categoryId }
                val account = accounts.find { it.id == tx.accountId }
                TransactionItemRow(
                    transaction = tx,
                    category = category,
                    account = account,
                    isPrivacyHidden = isPrivacyMode,
                    onClick = { viewModel.openEditTransaction(tx) }
                )
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    netWorth: Double,
    totalIncome: Double,
    totalExpense: Double,
    isPrivacyHidden: Boolean,
    onAddTransaction: () -> Unit,
    onOpenCopilot: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        CharcoalCard,
                        DarkSurfaceElevated,
                        Color(0xFF200F38)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(CharcoalBorder, RoyalIndigoLight, CharcoalBorder)),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BALANCE TOTAL CONSOLIDADO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = MetallicSilverMuted,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyalIndigo.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "MXN Local",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MetallicSilverLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CurrencyText(
                amount = netWorth,
                fontSize = 34.sp,
                isPrivacyHidden = isPrivacyHidden,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Income / Expense rapid pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalDark)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "INGRESOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicSilverMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CurrencyText(
                            amount = totalIncome,
                            type = "income",
                            fontSize = 16.sp,
                            isPrivacyHidden = isPrivacyHidden
                        )
                    }
                }

                // Expenses
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalDark)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "GASTOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicSilverMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CurrencyText(
                            amount = totalExpense,
                            type = "expense",
                            fontSize = 16.sp,
                            isPrivacyHidden = isPrivacyHidden
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CTA action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddTransaction,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("new_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigoLight)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenCopilot,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("copilot_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalSlate,
                        contentColor = MetallicSilverLight
                    )
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiloto IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PendingBankBanner(
    drafts: List<BankNotificationDraftEntity>,
    onConfirm: (BankNotificationDraftEntity) -> Unit,
    onDismiss: (BankNotificationDraftEntity) -> Unit,
    onViewAll: () -> Unit
) {
    val topDraft = drafts.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2C1E08),
                        Color(0xFF3B2609)
                    )
                )
            )
            .border(1.dp, AmberAlert.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = AmberAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alerta Bancaria Detectada (${drafts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberAlert
                    )
                }

                Text(
                    text = topDraft.bankName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicSilverLight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${topDraft.detectedMerchant} • \$${"%,.2f".format(topDraft.detectedAmount)} MXN (*${topDraft.accountLastFour})",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MetallicSilverLight
            )

            Text(
                text = topDraft.rawMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MetallicSilverMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver todas (${drafts.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicSilverMuted,
                    modifier = Modifier
                        .clickable { onViewAll() }
                        .padding(end = 12.dp)
                )

                // Discard
                OutlinedButton(
                    onClick = { onDismiss(topDraft) },
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("dismiss_bank_draft"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyExpense),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Descartar", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Confirm
                Button(
                    onClick = { onConfirm(topDraft) },
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("confirm_bank_draft"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldIncome),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirmar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BankSimulationTriggerRow(
    onSimulateBank: (String) -> Unit,
    onOpenInbox: () -> Unit
) {
    AetherCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = VioletAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pipeline de Notificaciones Bancarias",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                }
                Text(
                    text = "Buzón",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoyalIndigoLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenInbox() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Simula o escucha notificaciones en tiempo real de bancos autorizados con deduplicación SHA-256:",
                style = MaterialTheme.typography.bodySmall,
                color = MetallicSilverMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BankQuickButton("BBVA", Color(0xFF004481)) { onSimulateBank("bbva") }
                BankQuickButton("Nu", Color(0xFF820AD1)) { onSimulateBank("nu") }
                BankQuickButton("Banorte", Color(0xFFB91C1C)) { onSimulateBank("banorte") }
                BankQuickButton("Santander", Color(0xFFDC2626)) { onSimulateBank("santander") }
            }
        }
    }
}

@Composable
private fun BankQuickButton(
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalDark)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+ $label",
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MetallicSilverLight
            )
        }
    }
}

@Composable
private fun AccountPillCard(
    account: AccountEntity,
    isPrivacyHidden: Boolean
) {
    val brandColor = try {
        Color(android.graphics.Color.parseColor(account.colorHex))
    } catch (_: Exception) {
        RoyalIndigo
    }

    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(brandColor)
                )
                Text(
                    text = "*${account.lastFourDigits}",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = MetallicSilverMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MetallicSilverLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            CurrencyText(
                amount = account.balance,
                fontSize = 15.sp,
                isPrivacyHidden = isPrivacyHidden
            )
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    account: AccountEntity?,
    isPrivacyHidden: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#4B0082"))
    } catch (_: Exception) {
        RoyalIndigoLight
    }

    val iconVector = getCategoryIcon(category?.icon ?: "receipt")
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "MX")) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category icon bubble
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MetallicSilverLight,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category?.name ?: "General",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicSilverMuted
                        )
                        if (account != null) {
                            Text(
                                text = " • ${account.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MetallicSilverMuted
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                CurrencyText(
                    amount = transaction.amount,
                    type = transaction.type,
                    fontSize = 15.sp,
                    isPrivacyHidden = isPrivacyHidden
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (transaction.source == "bank_notif") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AmberAlert.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "BANCO",
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberAlert
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (transaction.source == "ai_voice") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VioletAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "VOZ IA",
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = VioletAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = dateFormat.format(Date(transaction.occurredAt)),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MetallicSilverMuted
                    )
                }
            }
        }
    }
}
