package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.ui.components.CurrencyText
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MetallicSilverLight
import com.example.ui.theme.MetallicSilverMuted
import com.example.ui.theme.RoyalIndigo
import com.example.ui.theme.RoyalIndigoLight
import com.example.ui.theme.RubyExpense
import com.example.ui.theme.VioletAccent
import java.util.UUID

@Composable
fun NewAccountDialog(
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("debit") }
    var lastFour by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4B0082") }

    val colors = listOf("#4B0082", "#004481", "#820AD1", "#EC0000", "#10B981", "#F59E0B", "#06B6D4")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CharcoalCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nueva Cuenta / Tarjeta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MetallicSilverMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la cuenta") },
                    placeholder = { Text("Ej. BBVA Nómina, Nu Crédito, Efectivo") },
                    modifier = Modifier.fillMaxWidth().testTag("account_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = CharcoalDark,
                        focusedBorderColor = RoyalIndigoLight,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = MetallicSilverLight,
                        unfocusedTextColor = MetallicSilverLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type selector
                Text(text = "Tipo de Cuenta:", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("debit" to "Débito", "credit" to "Crédito", "cash" to "Efectivo", "investment" to "Inversión").forEach { (typeKey, label) ->
                        val isSel = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) RoyalIndigo else CharcoalDark)
                                .border(1.dp, if (isSel) MetallicSilverLight else CharcoalBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MetallicSilverLight)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = lastFour,
                        onValueChange = { if (it.length <= 4) lastFour = it },
                        label = { Text("Últimos 4") },
                        placeholder = { Text("1234") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = CharcoalDark,
                            focusedBorderColor = RoyalIndigoLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = MetallicSilverLight,
                            unfocusedTextColor = MetallicSilverLight
                        )
                    )

                    OutlinedTextField(
                        value = initialBalanceText,
                        onValueChange = { initialBalanceText = it },
                        label = { Text("Saldo inicial") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = CharcoalDark,
                            focusedBorderColor = RoyalIndigoLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = MetallicSilverLight,
                            unfocusedTextColor = MetallicSilverLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color picker
                Text(text = "Color Distintivo:", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(colors) { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSel = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (isSel) MetallicSilverLight else Color.Transparent, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val bal = initialBalanceText.toDoubleOrNull() ?: 0.0
                        val newAcc = AccountEntity(
                            id = UUID.randomUUID().toString(),
                            name = name.trim().ifEmpty { "Cuenta Nueva" },
                            type = selectedType,
                            lastFourDigits = lastFour.trim().ifEmpty { "0000" },
                            balance = bal,
                            colorHex = selectedColor
                        )
                        onSave(newAcc)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigoLight)
                ) {
                    Text("Guardar Cuenta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditAccountDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var selectedType by remember { mutableStateOf(account.type) }
    var lastFour by remember { mutableStateOf(account.lastFourDigits) }
    var balanceText by remember { mutableStateOf(if (account.balance == 0.0) "" else "%.2f".format(account.balance).replace(",", ".")) }
    var selectedColor by remember { mutableStateOf(account.colorHex) }

    val colors = listOf("#4B0082", "#004481", "#820AD1", "#EC0000", "#10B981", "#F59E0B", "#06B6D4")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CharcoalCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Cuenta / Tarjeta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MetallicSilverMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la cuenta") },
                    placeholder = { Text("Ej. BBVA Nómina, Nu Crédito, Efectivo") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_account_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = CharcoalDark,
                        focusedBorderColor = RoyalIndigoLight,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = MetallicSilverLight,
                        unfocusedTextColor = MetallicSilverLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type selector
                Text(text = "Tipo de Cuenta:", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("debit" to "Débito", "credit" to "Crédito", "cash" to "Efectivo", "investment" to "Inversión").forEach { (typeKey, label) ->
                        val isSel = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) RoyalIndigo else CharcoalDark)
                                .border(1.dp, if (isSel) RoyalIndigoLight else CharcoalBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MetallicSilverLight else MetallicSilverMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = lastFour,
                        onValueChange = { if (it.length <= 4) lastFour = it },
                        label = { Text("Últimos 4 dígitos") },
                        placeholder = { Text("4821") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = CharcoalDark,
                            focusedBorderColor = RoyalIndigoLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = MetallicSilverLight,
                            unfocusedTextColor = MetallicSilverLight
                        )
                    )

                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Saldo actual") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = CharcoalDark,
                            focusedBorderColor = RoyalIndigoLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = MetallicSilverLight,
                            unfocusedTextColor = MetallicSilverLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Color Identificador:", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(colors) { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSel = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (isSel) MetallicSilverLight else Color.Transparent, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val bal = balanceText.toDoubleOrNull() ?: account.balance
                        val updated = account.copy(
                            name = name.trim().ifEmpty { account.name },
                            type = selectedType,
                            lastFourDigits = lastFour.trim().ifEmpty { account.lastFourDigits },
                            balance = bal,
                            colorHex = selectedColor
                        )
                        onSave(updated)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigoLight)
                ) {
                    Text("Actualizar Cuenta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetEditorDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveCategory: (CategoryEntity) -> Unit
) {
    val expenseCategories = categories.filter { it.kind == "expense" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CharcoalCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Presupuestos Mensuales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MetallicSilverMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenseCategories) { cat ->
                        var currentLimit by remember(cat) { mutableDoubleStateOf(cat.monthlyLimit) }
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            RoyalIndigoLight
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
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
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(catColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getCategoryIcon(cat.icon),
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = cat.name, style = MaterialTheme.typography.titleSmall, color = MetallicSilverLight)
                                    }

                                    if (currentLimit > 0) {
                                        Text(
                                            text = "$${"%,.0f".format(currentLimit)} MXN",
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldIncome
                                        )
                                    } else {
                                        Text(
                                            text = "Sin límite",
                                            fontFamily = JetBrainsMono,
                                            fontSize = 11.sp,
                                            color = MetallicSilverMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Slider(
                                    value = currentLimit.toFloat(),
                                    onValueChange = {
                                        currentLimit = it.toDouble()
                                        onSaveCategory(cat.copy(monthlyLimit = it.toDouble()))
                                    },
                                    valueRange = 0f..25000f,
                                    steps = 25,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MetallicSilverLight,
                                        activeTrackColor = catColor,
                                        inactiveTrackColor = CharcoalDark
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(1000.0 to "+1k", 2500.0 to "+2.5k", 5000.0 to "+5k").forEach { (amount, lbl) ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(CharcoalDark)
                                                    .clickable {
                                                        currentLimit = amount
                                                        onSaveCategory(cat.copy(monthlyLimit = amount))
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(text = lbl, fontSize = 9.sp, fontFamily = JetBrainsMono, color = MetallicSilverLight)
                                            }
                                        }
                                    }

                                    if (currentLimit > 0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    currentLimit = 0.0
                                                    onSaveCategory(cat.copy(monthlyLimit = 0.0))
                                                }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Quitar Presupuesto",
                                                tint = RubyExpense,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Quitar límite",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = RubyExpense,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigoLight)
                ) {
                    Text("Listo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
