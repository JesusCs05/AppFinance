package com.example.ui.notifications

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BankNotificationDraftEntity
import com.example.ui.components.AetherButton
import com.example.ui.components.AetherCard
import com.example.ui.components.CurrencyText
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MetallicSilverLight
import com.example.ui.theme.MetallicSilverMuted
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
fun BankNotificationInboxScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val allDrafts by viewModel.allBankDrafts.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("pending") } // "pending", "confirmed", "all"

    val displayedDrafts = when (selectedFilter) {
        "pending" -> allDrafts.filter { it.status == "pending" }
        "confirmed" -> allDrafts.filter { it.status == "confirmed" }
        else -> allDrafts
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MetallicSilverLight
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Buzón de Notificaciones Bancarias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                    Text(
                        text = "Listener & Deduplicación SHA-256",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAlert
                    )
                }
            }
        }

        // Test Simulation Bar
        item {
            AetherCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Sensors, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulador de Push Bancario",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MetallicSilverLight
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genera un payload simulando la llegada de una notificación del banco:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MetallicSilverMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SimulateButton("BBVA", Color(0xFF004481)) { viewModel.simulateBankNotification("bbva") }
                        SimulateButton("Nu", Color(0xFF820AD1)) { viewModel.simulateBankNotification("nu") }
                        SimulateButton("Banorte", Color(0xFFB91C1C)) { viewModel.simulateBankNotification("banorte") }
                        SimulateButton("Santander", Color(0xFFDC2626)) { viewModel.simulateBankNotification("santander") }
                    }
                }
            }
        }

        // Filter tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterTabItem(
                    label = "Pendientes (${allDrafts.count { it.status == "pending" }})",
                    isSelected = selectedFilter == "pending",
                    onClick = { selectedFilter = "pending" }
                )
                FilterTabItem(
                    label = "Confirmadas (${allDrafts.count { it.status == "confirmed" }})",
                    isSelected = selectedFilter == "confirmed",
                    onClick = { selectedFilter = "confirmed" }
                )
                FilterTabItem(
                    label = "Todas (${allDrafts.size})",
                    isSelected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" }
                )
            }
        }

        // Drafts list
        if (displayedDrafts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay notificaciones en este filtro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetallicSilverMuted
                    )
                }
            }
        } else {
            items(displayedDrafts) { draft ->
                BankDraftCard(
                    draft = draft,
                    onConfirm = { viewModel.commitBankDraft(draft) },
                    onDismiss = { viewModel.dismissBankDraft(draft.id) }
                )
            }
        }
    }
}

@Composable
private fun SimulateButton(name: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalDark)
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = "+ $name",
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MetallicSilverLight
        )
    }
}

@Composable
private fun FilterTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) RoyalIndigo else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MetallicSilverLight else MetallicSilverMuted
        )
    }
}

@Composable
private fun BankDraftCard(
    draft: BankNotificationDraftEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "MX")) }
    val isPending = draft.status == "pending"
    val isConfirmed = draft.status == "confirmed"

    val statusColor = when (draft.status) {
        "pending" -> AmberAlert
        "confirmed" -> EmeraldIncome
        else -> RubyExpense
    }

    AetherCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isPending) AmberAlert.copy(alpha = 0.4f) else CharcoalBorder
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = draft.bankName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = draft.status.uppercase(Locale.ROOT),
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = draft.detectedMerchant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                    Text(
                        text = "Tarjeta terminación *${draft.accountLastFour}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MetallicSilverMuted
                    )
                }

                CurrencyText(
                    amount = draft.detectedAmount,
                    type = draft.detectedType,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Raw message box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalDark)
                    .padding(8.dp)
            ) {
                Text(
                    text = draft.rawMessage,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MetallicSilverMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SHA-256 deduplication hash display
            Text(
                text = "SHA-256: ${draft.dedupHash.take(24)}...",
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                color = MetallicSilverMuted
            )

            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyExpense),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Descartar", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldIncome),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirmar a Saldo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
