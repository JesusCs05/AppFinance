package com.example.ui.calendar

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AetherCard
import com.example.ui.components.CurrencyText
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MetallicSilverLight
import com.example.ui.theme.MetallicSilverMuted
import com.example.ui.theme.RoyalIndigo
import com.example.ui.theme.RoyalIndigoLight
import com.example.ui.theme.RubyExpense
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarAnalyticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()

    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "MX")) }
    val dayDetailFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX")) }

    // Filter transactions for current month
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)

    val monthTransactions = transactions.filter { tx ->
        val cal = Calendar.getInstance().apply { timeInMillis = tx.occurredAt }
        cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
    }

    val selectedDayTransactions = monthTransactions.filter { tx ->
        val cal = Calendar.getInstance().apply { timeInMillis = tx.occurredAt }
        cal.get(Calendar.DAY_OF_MONTH) == selectedDay
    }

    val dayIncome = selectedDayTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val dayExpense = selectedDayTransactions.filter { it.type == "expense" }.sumOf { it.amount }

    val monthIncome = monthTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val monthExpense = monthTransactions.filter { it.type == "expense" }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CALENDARIO FINANCIERO",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        color = MetallicSilverMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthFormat.format(currentCalendar.time).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MetallicSilverLight
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val newCal = currentCalendar.clone() as Calendar
                            newCal.add(Calendar.MONTH, -1)
                            currentCalendar = newCal
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Mes anterior",
                            tint = MetallicSilverLight
                        )
                    }

                    IconButton(
                        onClick = {
                            val newCal = currentCalendar.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            currentCalendar = newCal
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mes siguiente",
                            tint = MetallicSilverLight
                        )
                    }
                }
            }
        }

        // Calendar Grid Card
        item {
            AetherCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurfaceElevated
            ) {
                Column {
                    // Day of week labels
                    val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { dayName ->
                            Text(
                                text = dayName,
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MetallicSilverMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid calculation
                    val calTemp = currentCalendar.clone() as Calendar
                    calTemp.set(Calendar.DAY_OF_MONTH, 1)
                    val firstDayOfWeek = calTemp.get(Calendar.DAY_OF_WEEK) - 1
                    val maxDaysInMonth = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH)

                    var dayCounter = 1
                    val rowsCount = ((firstDayOfWeek + maxDaysInMonth + 6) / 7)

                    for (row in 0 until rowsCount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                if (cellIndex < firstDayOfWeek || dayCounter > maxDaysInMonth) {
                                    Box(modifier = Modifier.size(40.dp))
                                } else {
                                    val currentDayVal = dayCounter
                                    val isSelected = currentDayVal == selectedDay

                                    // Find transactions on this day
                                    val dayTxs = monthTransactions.filter { tx ->
                                        val calTx = Calendar.getInstance().apply { timeInMillis = tx.occurredAt }
                                        calTx.get(Calendar.DAY_OF_MONTH) == currentDayVal
                                    }
                                    val now = System.currentTimeMillis() + 60_000L
                                    val hasIncome = dayTxs.any { it.type == "income" }
                                    val hasExpense = dayTxs.any { it.type == "expense" }
                                    val hasRecurring = dayTxs.any { it.source == "recurring" }
                                    val hasFuture = dayTxs.any { it.occurredAt > now }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) RoyalIndigo else if (dayTxs.isNotEmpty()) CharcoalDark else Color.Transparent
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) MetallicSilverLight else if (dayTxs.isNotEmpty()) CharcoalBorder else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedDay = currentDayVal },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$currentDayVal",
                                                fontFamily = JetBrainsMono,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MetallicSilverLight else if (dayTxs.isNotEmpty()) MetallicSilverLight else MetallicSilverMuted
                                            )

                                            // Multi-dot indicators: Green for Income, Red for Expense, Violet for Recurring/Future
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                if (hasIncome) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(EmeraldIncome))
                                                }
                                                if (hasExpense) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(RubyExpense))
                                                }
                                                if (hasRecurring || hasFuture) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(VioletAccent))
                                                }
                                            }
                                        }
                                    }
                                    dayCounter++
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DotLegendItem(color = EmeraldIncome, label = "Ingreso")
                        Spacer(modifier = Modifier.width(14.dp))
                        DotLegendItem(color = RubyExpense, label = "Gasto")
                        Spacer(modifier = Modifier.width(14.dp))
                        DotLegendItem(color = VioletAccent, label = "Programado")
                    }
                }
            }
        }

        // Selected Day Summary Card
        item {
            val selectedDateCal = currentCalendar.clone() as Calendar
            selectedDateCal.set(Calendar.DAY_OF_MONTH, selectedDay)

            AetherCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayDetailFormat.format(selectedDateCal.time).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MetallicSilverLight
                        )

                        Text(
                            text = "${selectedDayTransactions.size} registros",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicSilverMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Day income
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalDark)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Entradas", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                                CurrencyText(amount = dayIncome, type = "income", fontSize = 15.sp, isPrivacyHidden = isPrivacyMode)
                            }
                        }

                        // Day expense
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalDark)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Salidas", style = MaterialTheme.typography.labelSmall, color = MetallicSilverMuted)
                                CurrencyText(amount = dayExpense, type = "expense", fontSize = 15.sp, isPrivacyHidden = isPrivacyMode)
                            }
                        }
                    }

                    val hasFutureTx = selectedDayTransactions.any { it.occurredAt > System.currentTimeMillis() + 60_000L }
                    if (hasFutureTx) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VioletAccent.copy(alpha = 0.15f))
                                .border(1.dp, VioletAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = VioletAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Compromiso futuro programado para esta fecha",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MetallicSilverLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.openNewTransaction(selectedDateCal.timeInMillis)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigoLight)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Añadir movimiento en esta fecha",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Transactions of the selected day
        if (selectedDayTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin movimientos en este día seleccionado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetallicSilverMuted
                    )
                }
            }
        } else {
            items(selectedDayTransactions) { tx ->
                val category = categories.find { it.id == tx.categoryId }
                SelectedDayTxRow(
                    transaction = tx,
                    category = category,
                    isPrivacyHidden = isPrivacyMode,
                    onClick = { viewModel.openEditTransaction(tx) }
                )
            }
        }

        // Category Spend Breakdown Section (Section 5 Spec)
        item {
            Text(
                text = "Distribución de Gastos Mensuales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MetallicSilverLight,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            AetherCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val expenseCategories = categories.filter { it.kind == "expense" }
                    val totalMonthExpense = monthExpense.coerceAtLeast(1.0)

                    expenseCategories.forEach { cat ->
                        val catSpent = monthTransactions.filter { it.categoryId == cat.id && it.type == "expense" }.sumOf { it.amount }
                        if (catSpent > 0 || cat.monthlyLimit > 0) {
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (_: Exception) {
                                RoyalIndigoLight
                            }

                            val progress = (catSpent / totalMonthExpense).toFloat().coerceIn(0f, 1f)

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MetallicSilverLight
                                        )
                                    }

                                    CurrencyText(
                                        amount = catSpent,
                                        type = "expense",
                                        fontSize = 13.sp,
                                        isPrivacyHidden = isPrivacyMode
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = catColor,
                                    trackColor = CharcoalDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DotLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MetallicSilverMuted
        )
    }
}

@Composable
private fun SelectedDayTxRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    isPrivacyHidden: Boolean,
    onClick: () -> Unit = {}
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#4B0082"))
    } catch (_: Exception) {
        RoyalIndigoLight
    }
    val isFuture = transaction.occurredAt > (System.currentTimeMillis() + 60_000L)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(
                1.dp,
                if (isFuture) VioletAccent.copy(alpha = 0.5f) else CharcoalBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category?.icon ?: "receipt"),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.merchant,
                            style = MaterialTheme.typography.titleSmall,
                            color = MetallicSilverLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isFuture) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VioletAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PROGRAMADO",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VioletAccent
                                )
                            }
                        }
                    }
                    Text(
                        text = category?.name ?: "General",
                        style = MaterialTheme.typography.labelSmall,
                        color = MetallicSilverMuted
                    )
                }
            }

            CurrencyText(
                amount = transaction.amount,
                type = transaction.type,
                fontSize = 14.sp,
                isPrivacyHidden = isPrivacyHidden
            )
        }
    }
}
