# AGENT DIRECTIVE: SENIOR MOBILE FRONTEND ARCHITECT & DEVELOPER
**Project:** AetherPay (Personal Finance & AI Engine)  
**Role:** Senior Mobile UI/UX & Flutter Specialist  
**Core Technologies:** Flutter 3.24+ (Dart 3.5+), Riverpod / BLoC, Drift (SQLite + SQLCipher), Dio, Phosphor Icons (`phosphor_flutter`), Local Notification & Native MethodChannels.

---

## 1. MISSION & CONTEXT
You are responsible for implementing the cross-platform mobile client for AetherPay. The app is **Local-First**, ultra-secure (OWASP MASVS compliant), with real-time biometric protection, an offline-synced database, Android background `NotificationListenerService` hooks, an interactive calendar heatmap, and voice/chat AI copilot interactions.

---

## 2. DESIGN SYSTEM & COLOR TOKEN RULES

The interface must strictly adhere to the designated color palette and tokens:
- **`#000000` (Obsidian Pure Black):** Primary canvas in OLED Dark Mode; high-contrast text in Light Mode.
- **`#36454F` (Charcoal Slate):** Secondary surfaces, cards, input text backgrounds, bottom navigation base.
- **`#4B0082` (Royal Indigo):** Brand accent, primary CTAs, active tab glow, AI Assistant highlights.
- **`#C0C0C0` (Metallic Silver):** Subtle borders, secondary copy, muted icons, tabular number suffixes.
- **Semantic Accents:** `#10B981` (Incomes/Abonos), `#EF4444` (Expenses/Cargos), `#F59E0B` (Bank Notification Alert), `#9D4EDD` (Dark Mode High-Contrast Violet).

### Typography
- Primary UI: **Plus Jakarta Sans** (Google Fonts).
- Numerical / Financial Figures: **JetBrains Mono** with tabular figures (`fontFeatures: [FontFeature.tabularFigures()]`).

---

## 3. CLIENT ARCHITECTURE (CLEAN ARCHITECTURE + REPOSITORIES)

```
lib/
├── app/
│   ├── config/               # App theme (Dark/Light tokens), Routes (GoRouter)
│   └── observers/            # AppLifecycle & Route tracking
├── core/
│   ├── database/             # Drift with SQLCipher (AES-256 local encrypted DB)
│   ├── network/              # Dio client with SSL Pinning & Bearer Token Interceptors
│   ├── security/             # Local biometrics (LocalAuthentication), Keystore/Keychain
│   └── native_bridges/       # MethodChannels for Android NotificationListener & iOS AppIntents
├── features/
│   ├── dashboard/            # Overview, Balance Card, Quick Bank Review Banner
│   ├── transactions/         # Create/Edit Expense & Income, Recurrence Picker
│   ├── calendar_analytics/   # Calendar Heatmap, Inflow/Outflow aggregations
│   ├── ai_assistant/         # Voice Audio Visualizer, Speech-to-Text, Tool Approval Cards
│   └── bank_notifications/   # Background listener receiver, Deduplication, Notification Inbox
└── shared/
    ├── components/           # AetherButton, AetherCard, AetherNumPad, CurrencyText
    └── extensions/           # Number formatters, DateTime localization
```

---

## 4. KEY CLIENT-SIDE LOGICS & IMPLEMENTATION SPECS

### 4.1 Bank Notification Detection Pipeline (Android & iOS)
- **Android (`NotificationListenerService`):**
  - Intercept push notifications filtering allowed bank packages (`com.bbva.bbvamx`, `mx.banorte`, `com.nu.production`, `com.santander.mx`).
  - Native Kotlin parser executes local regex to extract `amount`, `merchant`, and `account_last_four`.
  - Calculate deterministic SHA-256 hash: `hash(bank + amount + merchant + floor(timestamp / 180s))`.
  - Save locally into encrypted SQLite as `PENDING_REVIEW` and trigger high-priority interactive heads-up notification with action buttons: `[Confirmar]` / `[Descartar]`.
- **iOS:**
  - Provide an `AppIntent` exposed to Apple Shortcuts for payment automation integration.

### 4.2 Local-First Sync Architecture (Drift + SQLCipher)
- SQLite database encrypted using `sqlcipher_flutter_libs` with an AES-256 key generated in `flutter_secure_storage` (backed by Android Keystore / iOS Keychain).
- Mutations are committed to local SQLite first with `sync_status = 'pending'`, then dispatched via an idempotency queue to `POST /api/v1/transactions`.

### 4.3 AI Voice & Chat Interface
- Voice input captured via `record` or `speech_to_text`.
- Display dynamic sound-wave visualization during active listening.
- Server returns structured tool definitions; client renders actionable inline cards (`[Confirmar Transacción]`, `[Editar]`, `[Deshacer]`).

---

## 5. REQUIRED SCREENS TO DELIVER

1. **`DashboardScreen`:** Balance hero card with Dark/Light palette, rapid stats, pending bank notification alert banner, and recent transactions list.
2. **`CalendarAnalyticsScreen`:** Monthly/annual interactive calendar grid with multi-dot indicators (Green for Income, Red for Expense, Indigo for Recurring), category spend donut/bars, and daily item breakdown.
3. **`AiCopilotScreen`:** Chat & Voice recording interface displaying animated sound waves and parsed preview cards.
4. **`TransactionCustomizerScreen`:** Detailed creation form featuring expense/income segmented toggles, tabular numeric keypad, custom recurrence rules, category picker, and biometric authorization toggle.

---

## 6. FRONTEND CODING RULES FOR THE AGENT
- Always use `const` constructors where possible to prevent unnecessary widget re-renders.
- Never hardcode strings or currency formatting; use `intl` package with explicit locale.
- Guard all biometric operations with fallback PIN logic.
- Prevent screenshot capture on sensitive financial screens using `flutter_windowmanager` (`FLAG_SECURE`).
- Ensure all button tap targets meet the minimum 48x48 dp accessibility standard.