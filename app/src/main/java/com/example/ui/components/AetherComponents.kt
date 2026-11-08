package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MetallicSilverLight
import com.example.ui.theme.MetallicSilverMuted
import com.example.ui.theme.RoyalIndigo
import com.example.ui.theme.RoyalIndigoLight
import com.example.ui.theme.RubyExpense
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AetherCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurfaceElevated,
    borderColor: Color = CharcoalBorder,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable { onClick() }
    } else {
        modifier.clip(RoundedCornerShape(cornerRadius))
    }

    Box(
        modifier = clickableModifier
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun AetherButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    backgroundColor: Color = RoyalIndigoLight,
    contentColor: Color = MetallicSilverLight,
    testTag: String = "aether_button"
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(CharcoalBorder, MetallicSilverMuted))
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    type: String? = null, // "income", "expense", null
    currency: String = "MXN",
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    isPrivacyHidden: Boolean = false
) {
    if (isPrivacyHidden) {
        Text(
            text = "••••••",
            fontFamily = JetBrainsMono,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = MetallicSilverLight,
            modifier = modifier
        )
        return
    }

    val color = when (type) {
        "income" -> EmeraldIncome
        "expense" -> RubyExpense
        else -> MetallicSilverLight
    }

    val sign = when (type) {
        "income" -> "+"
        "expense" -> "-"
        else -> ""
    }

    val formatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
        maximumFractionDigits = 2
    }.format(Math.abs(amount))

    Text(
        text = "$sign$formatted",
        fontFamily = JetBrainsMono,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@Composable
fun SoundWaveVisualizer(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val anim4 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )
    val anim5 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar5"
    )

    val heights = if (isRecording) {
        listOf(anim1, anim2, anim3, anim4, anim5, anim2, anim3)
    } else {
        listOf(0.2f, 0.3f, 0.4f, 0.3f, 0.2f, 0.3f, 0.2f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { factor ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(5.dp)
                    .height(36.dp * factor)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                RoyalIndigoLight,
                                if (isRecording) EmeraldIncome else CharcoalSlate
                            )
                        )
                    )
            )
        }
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase(Locale.ROOT)) {
        "shopping_cart" -> Icons.Default.ShoppingCart
        "shopping_bag" -> Icons.Default.ShoppingBag
        "payments" -> Icons.Default.Payments
        "trending_up" -> Icons.Default.TrendingUp
        "home" -> Icons.Default.Home
        "directions_car" -> Icons.Default.DirectionsCar
        "sports_esports" -> Icons.Default.SportsEsports
        "local_hospital" -> Icons.Default.LocalHospital
        "credit_card" -> Icons.Default.CreditCard
        "account_balance" -> Icons.Default.AccountBalance
        "wallet" -> Icons.Default.Wallet
        else -> Icons.Default.Receipt
    }
}
