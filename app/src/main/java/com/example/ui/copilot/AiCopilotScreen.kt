package com.example.ui.copilot

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.domain.copilot.CopilotMessage
import com.example.domain.copilot.CopilotToolCall
import com.example.ui.components.AetherCard
import com.example.ui.components.CurrencyText
import com.example.ui.components.SoundWaveVisualizer
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
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
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiCopilotScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.copilotMessages.collectAsStateWithLifecycle()
    val isListening by viewModel.isListeningVoice.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.isImeVisible

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording { text ->
                inputText = text
            }
        } else {
            val sample = "Gasté \$350 en OXXO de café y botana"
            inputText = sample
            viewModel.sendCopilotMessage(sample)
        }
    }

    LaunchedEffect(messages.size, isImeVisible) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(RoyalIndigo, VioletAccent))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MetallicSilverLight,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AETHER COPILOT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = MetallicSilverLight
                )
                Text(
                    text = "Voz & Chat Financiero Inteligente",
                    style = MaterialTheme.typography.labelSmall,
                    color = VioletAccent
                )
            }
        }

        // Voice suggestion chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Gasté \$450 en Walmart de despensa",
                "Me depositaron \$16,500 de nómina",
                "Pagué \$620 de recibo CFE",
                "Agrega recurrente Netflix \$299 día 15",
                "¿Cuánto he gastado este mes?",
                "Dame un resumen de mis finanzas"
            )
            items(suggestions) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalDark)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.sendCopilotMessage(prompt) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MetallicSilverLight
                    )
                }
            }
        }

        // Chat Messages Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onConfirmTool = { tool -> viewModel.confirmCopilotToolCall(msg.id, tool) },
                    onDiscardTool = { viewModel.discardCopilotToolCall(msg.id) }
                )
            }
        }

        // Sound Wave Visualizer when voice recording
        AnimatedVisibility(visible = isListening) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, VioletAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎙️ Escuchando comando de voz...",
                    style = MaterialTheme.typography.titleSmall,
                    color = VioletAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                SoundWaveVisualizer(isRecording = true)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.stopVoiceRecording() },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyExpense),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Detener y Procesar", fontSize = 12.sp)
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = if (isImeVisible) 12.dp else 90.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text("Describe o dicta un gasto/ingreso...", color = MetallicSilverMuted, fontSize = 13.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("copilot_input_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = CharcoalDark,
                    focusedBorderColor = RoyalIndigoLight,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = MetallicSilverLight,
                    unfocusedTextColor = MetallicSilverLight
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendCopilotMessage(inputText)
                            inputText = ""
                        }
                    }
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic button (Speech recognition with runtime permission)
            IconButton(
                onClick = {
                    if (isListening) {
                        viewModel.stopVoiceRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isListening) RubyExpense else CharcoalSlate)
                    .testTag("copilot_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Grabar voz",
                    tint = MetallicSilverLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send button
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendCopilotMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) RoyalIndigoLight else CharcoalDark)
                    .testTag("copilot_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (inputText.isNotBlank()) MetallicSilverLight else MetallicSilverMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: CopilotMessage,
    onConfirmTool: (CopilotToolCall) -> Unit,
    onDiscardTool: () -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.82f else 0.95f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) RoyalIndigoLight else DarkSurfaceElevated
                )
                .border(
                    1.dp,
                    if (isUser) RoyalIndigo else CharcoalBorder,
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MetallicSilverLight,
                    lineHeight = 20.sp
                )

                // Render Actionable Tool Calling Preview Card (Section 4.3 spec)
                if (message.toolCall != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ToolPreviewActionCard(
                        tool = message.toolCall,
                        isConfirmed = message.isToolConfirmed,
                        isDiscarded = message.isToolDiscarded,
                        onConfirm = { onConfirmTool(message.toolCall) },
                        onDiscard = onDiscardTool
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolPreviewActionCard(
    tool: CopilotToolCall,
    isConfirmed: Boolean,
    isDiscarded: Boolean,
    onConfirm: () -> Unit,
    onDiscard: () -> Unit
) {
    val isIncome = tool.parsedType == "income"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalDark)
            .border(
                1.dp,
                if (isConfirmed) EmeraldIncome.copy(alpha = 0.6f) else if (isDiscarded) RubyExpense.copy(alpha = 0.5f) else VioletAccent.copy(alpha = 0.6f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isIncome) EmeraldIncome.copy(alpha = 0.2f) else RubyExpense.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isIncome) "ABONO PROPUESTO" else "CARGO PROPUESTO",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) EmeraldIncome else RubyExpense
                    )
                }

                if (tool.isRecurring) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VioletAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RECURRENTE",
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tool.merchant,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MetallicSilverLight
            )

            Spacer(modifier = Modifier.height(2.dp))

            CurrencyText(
                amount = tool.amount,
                type = tool.parsedType,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Categoría: ${tool.categoryName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicSilverMuted
                )
                Text(
                    text = " • Cuenta: ${tool.accountName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicSilverMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: [Confirmar Transacción] / [Descartar]
            if (isConfirmed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldIncome, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Transacción Asentada con Éxito", style = MaterialTheme.typography.labelSmall, color = EmeraldIncome, fontWeight = FontWeight.Bold)
                }
            } else if (isDiscarded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = RubyExpense, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Transacción Descartada", style = MaterialTheme.typography.labelSmall, color = RubyExpense)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("tool_discard_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyExpense),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Descartar", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("tool_confirm_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldIncome),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirmar Transacción", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
