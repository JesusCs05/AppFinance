package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.calendar.CalendarAnalyticsScreen
import com.example.ui.copilot.AiCopilotScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.notifications.BankNotificationInboxScreen
import com.example.ui.theme.AetherPayTheme
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MetallicSilverLight
import com.example.ui.theme.MetallicSilverMuted
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalIndigo
import com.example.ui.theme.RoyalIndigoLight
import com.example.ui.theme.VioletAccent
import com.example.ui.transaction.TransactionCustomizerScreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isBiometricProtected by viewModel.isBiometricProtected.collectAsStateWithLifecycle()

            // Dynamic FLAG_SECURE window protection per architectural directive
            LaunchedEffect(isBiometricProtected) {
                if (isBiometricProtected) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            AetherPayTheme(darkTheme = true) {
                AetherPayApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AetherPayApp(viewModel: FinanceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val pendingDrafts by viewModel.pendingBankDrafts.collectAsStateWithLifecycle()

    // Handle system back navigation to Dashboard
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentScreen != AppScreen.CUSTOMIZE_TRANSACTION) {
                AetherBottomNav(
                    currentScreen = currentScreen,
                    pendingDraftsCount = pendingDrafts.size,
                    onSelectScreen = { viewModel.navigateTo(it) }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.CALENDAR) {
                FloatingActionButton(
                    onClick = { viewModel.openNewTransaction() },
                    containerColor = RoyalIndigoLight,
                    contentColor = MetallicSilverLight,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 72.dp)
                        .testTag("fab_new_transaction")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Movimiento", modifier = Modifier.size(26.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppScreen.CALENDAR -> CalendarAnalyticsScreen(viewModel = viewModel)
                    AppScreen.COPILOT -> AiCopilotScreen(viewModel = viewModel)
                    AppScreen.CUSTOMIZE_TRANSACTION -> TransactionCustomizerScreen(viewModel = viewModel)
                    AppScreen.NOTIFICATION_INBOX -> BankNotificationInboxScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AetherBottomNav(
    currentScreen: AppScreen,
    pendingDraftsCount: Int,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .border(width = 1.dp, color = CharcoalBorder)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = CharcoalCard,
        contentColor = MetallicSilverLight,
        tonalElevation = 8.dp
    ) {
        // Dashboard
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onSelectScreen(AppScreen.DASHBOARD) },
            icon = {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard")
            },
            label = { Text("Panel", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MetallicSilverLight,
                selectedTextColor = MetallicSilverLight,
                indicatorColor = RoyalIndigo,
                unselectedIconColor = MetallicSilverMuted,
                unselectedTextColor = MetallicSilverMuted
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        // Calendar
        NavigationBarItem(
            selected = currentScreen == AppScreen.CALENDAR,
            onClick = { onSelectScreen(AppScreen.CALENDAR) },
            icon = {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendario")
            },
            label = { Text("Calendario", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MetallicSilverLight,
                selectedTextColor = MetallicSilverLight,
                indicatorColor = RoyalIndigo,
                unselectedIconColor = MetallicSilverMuted,
                unselectedTextColor = MetallicSilverMuted
            ),
            modifier = Modifier.testTag("nav_calendar")
        )

        // AI Copilot
        NavigationBarItem(
            selected = currentScreen == AppScreen.COPILOT,
            onClick = { onSelectScreen(AppScreen.COPILOT) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Copiloto IA",
                    tint = if (currentScreen == AppScreen.COPILOT) VioletAccent else MetallicSilverMuted
                )
            },
            label = { Text("Copiloto IA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VioletAccent,
                selectedTextColor = VioletAccent,
                indicatorColor = RoyalIndigo,
                unselectedIconColor = MetallicSilverMuted,
                unselectedTextColor = MetallicSilverMuted
            ),
            modifier = Modifier.testTag("nav_copilot")
        )

        // Bank Notifications Inbox
        NavigationBarItem(
            selected = currentScreen == AppScreen.NOTIFICATION_INBOX,
            onClick = { onSelectScreen(AppScreen.NOTIFICATION_INBOX) },
            icon = {
                if (pendingDraftsCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = AmberAlert,
                                contentColor = ObsidianBlack
                            ) {
                                Text(
                                    text = "$pendingDraftsCount",
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alertas Bancarias")
                    }
                } else {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alertas Bancarias")
                }
            },
            label = { Text("Bancos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MetallicSilverLight,
                selectedTextColor = MetallicSilverLight,
                indicatorColor = RoyalIndigo,
                unselectedIconColor = MetallicSilverMuted,
                unselectedTextColor = MetallicSilverMuted
            ),
            modifier = Modifier.testTag("nav_notifications")
        )
    }
}
