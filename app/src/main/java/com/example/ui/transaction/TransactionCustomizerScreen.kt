package com.example.ui.transaction

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.AetherCard
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSlate
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

@Composable
fun TransactionCustomizerScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val editingTx by viewModel.editingTransaction.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var selectedType by remember(editingTx) { mutableStateOf(editingTx?.type ?: "expense") }
    var rawAmountString by remember(editingTx) {
        mutableStateOf(if (editingTx != null) "%.2f".format(editingTx!!.amount).replace(",", ".") else "")
    }
    var merchantText by remember(editingTx) { mutableStateOf(editingTx?.merchant ?: "") }
    var notesText by remember(editingTx) { mutableStateOf(editingTx?.notes ?: "") }
    var selectedAccountId by remember(editingTx, accounts) {
        mutableStateOf(editingTx?.accountId ?: accounts.firstOrNull()?.id ?: "")
    }
    var selectedCategoryId by remember(editingTx, categories) {
        mutableStateOf(
            editingTx?.categoryId ?: categories.firstOrNull { it.kind == selectedType }?.id ?: ""
        )
    }

    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceFrequency by remember { mutableStateOf("monthly") }
    var requireBiometrics by remember(editingTx) { mutableStateOf(editingTx?.requiresBiometric ?: false) }

    val filteredCategories = categories.filter { it.kind == selectedType }

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
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MetallicSilverLight
                        )
                    }
                    Text(
                        text = if (editingTx != null) "Editar Movimiento" else "Nuevo Movimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                }

                if (editingTx != null) {
                    IconButton(
                        onClick = {
                            viewModel.deleteTransaction(editingTx!!)
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = RubyExpense
                        )
                    }
                }
            }
        }

        // Segmented Type Selector (Gasto vs Ingreso)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalDark)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                // Expense Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == "expense") RubyExpense else Color.Transparent)
                        .clickable {
                            selectedType = "expense"
                            selectedCategoryId = categories.firstOrNull { it.kind == "expense" }?.id ?: ""
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CARGO (GASTO)",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selectedType == "expense") MetallicSilverLight else MetallicSilverMuted
                    )
                }

                // Income Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == "income") EmeraldIncome else Color.Transparent)
                        .clickable {
                            selectedType = "income"
                            selectedCategoryId = categories.firstOrNull { it.kind == "income" }?.id ?: ""
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABONO (INGRESO)",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selectedType == "income") MetallicSilverLight else MetallicSilverMuted
                    )
                }
            }
        }

        // Amount Display & Keypad Input Box
        item {
            AetherCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MONTO EN MXN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MetallicSilverMuted,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val displayAmount = if (rawAmountString.isEmpty()) "0.00" else rawAmountString
                    Text(
                        text = "$ $displayAmount",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        color = if (selectedType == "income") EmeraldIncome else RubyExpense
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Embedded Tabular Numeric Pad per frontend spec
                    AetherNumPad(
                        onNumberClick = { digit ->
                            if (digit == "." && rawAmountString.contains(".")) return@AetherNumPad
                            if (rawAmountString.length < 9) {
                                rawAmountString += digit
                            }
                        },
                        onBackspaceClick = {
                            if (rawAmountString.isNotEmpty()) {
                                rawAmountString = rawAmountString.dropLast(1)
                            }
                        },
                        onClearClick = { rawAmountString = "" }
                    )
                }
            }
        }

        // Account Selector
        item {
            Column {
                Text(
                    text = "Cuenta de Cargo / Destino",
                    style = MaterialTheme.typography.titleSmall,
                    color = MetallicSilverLight,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) RoyalIndigo else CharcoalDark)
                                .border(1.dp, if (isSelected) MetallicSilverLight else CharcoalBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "${acc.name} (*${acc.lastFourDigits})",
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MetallicSilverLight
                            )
                        }
                    }
                }
            }
        }

        // Category Picker
        item {
            Column {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    color = MetallicSilverLight,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredCategories) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            RoyalIndigoLight
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.3f) else CharcoalDark)
                                .border(1.dp, if (isSelected) catColor else CharcoalBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getCategoryIcon(cat.icon),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MetallicSilverLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Merchant / Payee input
        item {
            OutlinedTextField(
                value = merchantText,
                onValueChange = { merchantText = it },
                label = { Text("Comercio, Beneficiario o Entidad") },
                placeholder = { Text("Ej. Walmart, CFE, Nómina, Uber...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("merchant_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = CharcoalDark,
                    focusedBorderColor = RoyalIndigoLight,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = MetallicSilverLight,
                    unfocusedTextColor = MetallicSilverLight,
                    focusedLabelColor = RoyalIndigoLight,
                    unfocusedLabelColor = MetallicSilverMuted
                )
            )
        }

        // Notes field
        item {
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notas o Descripción Opcional") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = CharcoalDark,
                    focusedBorderColor = RoyalIndigoLight,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = MetallicSilverLight,
                    unfocusedTextColor = MetallicSilverLight,
                    focusedLabelColor = RoyalIndigoLight,
                    unfocusedLabelColor = MetallicSilverMuted
                )
            )
        }

        // Recurrence Rules Picker Toggle (Section 5 Spec)
        item {
            AetherCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Transacción Recurrente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MetallicSilverLight)
                                Text(text = "Programar repetición periódica automática", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                            }
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MetallicSilverLight,
                                checkedTrackColor = VioletAccent
                            )
                        )
                    }

                    AnimatedVisibility(visible = isRecurring) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(text = "Frecuencia de repetición:", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("semanal" to "Semanal", "quincenal" to "Quincenal", "monthly" to "Mensual", "yearly" to "Anual").forEach { (key, label) ->
                                    val isSel = recurrenceFrequency == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) VioletAccent else CharcoalDark)
                                            .clickable { recurrenceFrequency = key }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MetallicSilverLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Biometrics Security Toggle
        item {
            AetherCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldIncome, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Protección Biométrica Requerida", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MetallicSilverLight)
                            Text(text = "Solicitar huella/rostro para modificar", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                        }
                    }
                    Switch(
                        checked = requireBiometrics,
                        onCheckedChange = { requireBiometrics = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MetallicSilverLight,
                            checkedTrackColor = EmeraldIncome
                        )
                    )
                }
            }
        }

        // Commit CTA Button
        item {
            val amountVal = rawAmountString.toDoubleOrNull() ?: 0.0
            val canSave = amountVal > 0 && merchantText.isNotBlank() && selectedAccountId.isNotBlank()

            Button(
                onClick = {
                    if (canSave) {
                        viewModel.saveTransaction(
                            id = editingTx?.id,
                            accountId = selectedAccountId,
                            categoryId = selectedCategoryId,
                            type = selectedType,
                            amount = amountVal,
                            merchant = merchantText.trim(),
                            occurredAt = editingTx?.occurredAt ?: System.currentTimeMillis(),
                            notes = notesText.trim(),
                            source = editingTx?.source ?: "manual",
                            isRecurring = isRecurring,
                            recurrenceFreq = recurrenceFrequency
                        )
                    }
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "income") EmeraldIncome else RoyalIndigoLight
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingTx != null) "Guardar Cambios" else "Asentar Movimiento",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AetherNumPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val padRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        padRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalDark)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                when (key) {
                                    "DEL" -> onBackspaceClick()
                                    else -> onNumberClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Borrar",
                                tint = MetallicSilverMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                fontFamily = JetBrainsMono,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MetallicSilverLight
                            )
                        }
                    }
                }
            }
        }
    }
}
