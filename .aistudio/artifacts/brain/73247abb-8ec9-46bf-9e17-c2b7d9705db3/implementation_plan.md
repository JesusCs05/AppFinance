# Plan de Implementación: Desacoplamiento de Firebase, Inicio en Cero Datos y Detección de Mercado Pago

Eliminar toda la infraestructura de Firebase y Google Services para convertir a AetherPay en una aplicación financiera 100% privada, local y offline (Room SQLite), iniciando con cero datos y soporte nativo para notificaciones de Mercado Pago.

## Decisiones Críticas y Revisión del Usuario

> [!IMPORTANT]
> A partir de tus respuestas en la fase de aclaración, se confirman las siguientes decisiones de diseño:
> - **Copiloto Financiero**: Operará con un potente motor analítico local sin conexión, con opción en la interfaz para ingresar una clave de API de Gemini propia si se desea análisis generativo avanzado.
> - **Inicio sin datos**: La base de datos comenzará completamente vacía (0 cuentas, 0 transacciones, 0 borradores) y la pantalla principal mostrará un estado vacío refinado con llamada a la acción para crear la primera cuenta.
> - **Mercado Pago**: Detección integral para transferencias enviadas/recibidas, compras en línea y pagos con tarjeta física/virtual de Mercado Pago.

---

## 1. Visión General y Experiencia de Usuario

- **Privacidad Total y Rendimiento Instantáneo**: Al eliminar dependencias de red externas y servicios en segundo plano de Google Services, el APK compila más rápido, es más ligero y arranca instantáneamente sin riesgo de bloqueos por falta de credenciales en la nube.
- **Flujo de Primer Uso (Zero-State)**: El usuario abre la app y encuentra un dashboard oscuro con acabados metálicos y un estado vacío que lo invita a registrar su primera cuenta (Efectivo, Débito, Crédito, etc.).
- **Bandeja de Entrada Bancaria Ampliada**: Notificaciones en vivo de los principales bancos de México más **Mercado Pago**, con chips de filtro y conversión automática en borradores listos para asentar.

---

## 2. Experiencia Visual y Diseño

- **Estética Dark Obsidian & M3**:
  - Fondo `DarkBackground` (`#0D0F14`) con acentos `RoyalIndigo` (`#6366F1`) y `MercadoPagoBlue` (`#009EE3`).
  - Tarjetas con bordes ultra-finos (`CharcoalBorder`) y contraste elevado.
- **Estados Vacíos con Estilo**:
  - Ilustración/ícono central con mensaje inspirador: *"Tu lienzo financiero está listo. Crea tu primera cuenta para comenzar a registrar tus finanzas."*
  - Botón prominente de acción directa: `+ Crear primera cuenta`.
- **Configuración de IA en Copiloto**:
  - Tarjeta o diálogo de ajustes en la pantalla del Copiloto para introducir/guardar la clave de Gemini opcionalmente de forma segura en `EncryptedSharedPreferences` / `DataStore`.

---

## 3. Decisiones Técnicas y de Arquitectura

```
┌────────────────────────────────────────────────────────┐
│                   AetherPay UI Layer                   │
│   Dashboard  │  Transacciones  │  Copiloto  │  Inbox   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    FinanceViewModel                    │
│   (Estado de cuentas, transacciones, filtros, drafts)  │
└─────────────┬───────────────────────────┬──────────────┘
              │                           │
┌─────────────▼─────────────┐ ┌───────────▼──────────────┐
│     FinanceRepository     │ │   GeminiCopilotService   │
│   (Room DAO / SQLite)     │ │ Local Engine / Gemini API│
└─────────────┬─────────────┘ └──────────────────────────┘
              │
┌─────────────▼──────────────────────────────────────────┐
│                   AppDatabase (Room)                   │
│    accounts  │  transactions  │  categories  │ drafts  │
└────────────────────────────────────────────────────────┘
```

1. **Desinstalación de Firebase y Google Services**:
   - Eliminar de `build.gradle.kts` y `libs.versions.toml`: `google-services`, `firebase-bom`, `firebase-ai`, `firebase-firestore`, `firebase-auth`, `firebase-appcheck`, `credentials`, `googleid`.
   - Limpiar el workflow de CI `.github/workflows/build-apk.yml` de scripts de restauración de `google-services.json`.
   - Eliminar `FirestoreSyncService.kt`, `FirestoreSyncManager.kt`, `GoogleAuthHelper.kt` y `AppCheckUtil.kt`.
2. **Cero Datos Iniciales (Zero Seed)**:
   - Limpiar el `seedDatabase` de `AppDatabase.kt`: no insertar cuentas ni transacciones mock. Solo predefinir las categorías estándar del sistema con saldo 0 (Alimentos, Transporte, Hogar, Entretenimiento, Salud, Nómina, etc.) para que el usuario pueda categorizar de inmediato.
3. **Módulo de Mercado Pago (`BankNotificationParser.kt`)**:
   - Identificador de paquete: `com.mercadopago.wallet` y `com.mercadolibre`.
   - Expresiones regulares y reglas léxicas para:
     - `Transferiste \$([0-9.,]+) a (.*)` -> Gasto / Transferencia
     - `Te transfirieron \$([0-9.,]+) de (.*)` / `Recibiste \$([0-9.,]+)` -> Ingreso / Dinero recibido
     - `Pagaste \$([0-9.,]+) en (.*)` / `Pago aprobado de \$([0-9.,]+)` -> Gasto / Compra
     - `Compra con tu tarjeta Mercado Pago por \$([0-9.,]+) en (.*)` -> Gasto / Tarjeta MP
   - Mapeo automático de categorías y almacenamiento de borrador en Room.
4. **Copiloto Híbrido**:
   - `AiCopilotEngine` ejecuta el análisis financiero local con estadísticas instantáneas y sugerencias de ahorro.
   - Si el usuario suministra una API Key de Gemini en los ajustes del Copiloto, se realizan consultas directas HTTP a la API REST de Gemini (`generativelanguage.googleapis.com`) sin requerir Firebase.

---

## 4. Plan de Ejecución Paso a Paso

1. **Paso 1: Limpieza de Gradle y Dependencias**:
   - Remover plugins y dependencias de Firebase/Google Services en `app/build.gradle.kts`.
   - Simplificar `.github/workflows/build-apk.yml`.
2. **Paso 2: Eliminación de Servicios y Clases Firebase**:
   - Eliminar `FirestoreSyncService.kt`, `FirestoreSyncManager.kt`, `GoogleAuthHelper.kt` y `AppCheckUtil.kt`.
   - Ajustar `MainActivity.kt` para remover inicializaciones de Firebase/AppCheck.
3. **Paso 3: Depuración de Base de Datos y Cero Datos**:
   - Modificar `AppDatabase.kt` para eliminar la inserción de cuentas y transacciones demo.
   - Ajustar `DashboardScreen.kt` para mostrar el nuevo estado vacío con botón interactivo de creación.
4. **Paso 4: Implementación de Parser para Mercado Pago**:
   - Agregar patrones y constantes de Mercado Pago en `BankNotificationParser.kt`.
   - Agregar chip de filtro e icono distintivo de Mercado Pago en `BankNotificationInboxScreen.kt`.
5. **Paso 5: Copiloto sin Firebase y Clave Opcional**:
   - Adaptar `GeminiCopilotService.kt` y `AiCopilotScreen.kt` con selector/diálogo de API Key opcional y motor local predeterminado.
6. **Paso 6: Verificación y Compilación**:
   - Ejecutar `compile_applet` y pruebas unitarias de parseo de Mercado Pago con Robolectric.
