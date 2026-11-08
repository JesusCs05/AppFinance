package com.example.ui.notifications

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BankNotificationDraftEntity
import com.example.ui.components.AetherCard
import com.example.ui.components.CurrencyText
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.DarkBackground
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankNotificationInboxScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val allDrafts by viewModel.allBankDrafts.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("pending") } // "pending", "confirmed", "all"

    // Real-time notification listener permission check
    var isListenerEnabled by remember {
        mutableStateOf(isNotificationServiceEnabled(context))
    }

    // Refresh permission state whenever returning to this screen/app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isListenerEnabled = isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    modifier = Modifier.testTag("back_from_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MetallicSilverLight
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Buzón de Notificaciones Bancarias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                    Text(
                        text = "Captura en Tiempo Real & Deduplicación SHA-256",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isListenerEnabled) EmeraldIncome else AmberAlert
                    )
                }

                IconButton(
                    onClick = { isListenerEnabled = isNotificationServiceEnabled(context) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar estado",
                        tint = MetallicSilverMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Real Listener System Status Card
        item {
            AetherCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isListenerEnabled) EmeraldIncome.copy(alpha = 0.4f) else AmberAlert.copy(alpha = 0.5f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isListenerEnabled) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (isListenerEnabled) EmeraldIncome else AmberAlert,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isListenerEnabled) "Captura en Vivo Activa" else "Acceso a Notificaciones Requerido",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MetallicSilverLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isListenerEnabled) EmeraldIncome.copy(alpha = 0.15f) else AmberAlert.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isListenerEnabled) "EN SERVICIO" else "INACTIVO",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isListenerEnabled) EmeraldIncome else AmberAlert
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isListenerEnabled) {
                            "AetherPay escucha pasivamente en segundo plano los pagos de tus bancos. Al recibir una alerta bancaria, se parseará y agregará como borrador pendiente aquí sin necesidad de simulación."
                        } else {
                            "Para capturar automáticamente transacciones reales de tus bancos, debes permitir el acceso de lectura de notificaciones en los Ajustes del Sistema Android."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MetallicSilverMuted,
                        lineHeight = 18.sp
                    )

                    if (!isListenerEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                openNotificationSettings(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("enable_notification_access_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigoLight)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Activar en Ajustes de Android",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Monitored Banks Information Bar
        item {
            Column {
                Text(
                    text = "Bancos Monitoreados Activamente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicSilverMuted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BankMonitorBadge("BBVA México", Color(0xFF004481))
                    BankMonitorBadge("Nu Tarjeta", Color(0xFF820AD1))
                    BankMonitorBadge("Banorte Móvil", Color(0xFFB91C1C))
                    BankMonitorBadge("Santander", Color(0xFFDC2626))
                    BankMonitorBadge("Citibanamex", Color(0xFF002D72))
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
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CharcoalSlate,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isListenerEnabled) {
                                "Sin notificaciones bancarias pendientes\nLas compras reales aparecerán automáticamente."
                            } else {
                                "Activa el acceso a notificaciones arriba para comenzar a capturar gastos en vivo."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MetallicSilverMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
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
private fun BankMonitorBadge(name: String, brandColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalDark)
            .border(1.dp, brandColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(EmeraldIncome)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
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
                        text = "Tarjeta terminación *${draft.accountLastFour} • ${dateFormat.format(Date(draft.occurredAt))}",
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

            // Raw notification payload
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

private fun isNotificationServiceEnabled(context: Context): Boolean {
    return try {
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    } catch (e: Exception) {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        flat != null && flat.contains(context.packageName)
    }
}

private fun openNotificationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
