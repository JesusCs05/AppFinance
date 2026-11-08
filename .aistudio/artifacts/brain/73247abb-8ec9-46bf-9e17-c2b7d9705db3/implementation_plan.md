# Plan de Implementación: Captura Real de Notificaciones Bancarias de Android

Sustituir el simulador manual de notificaciones bancarias por la captura 100% en vivo del sistema operativo Android (`NotificationListenerService`), añadiendo soporte completo para los 5 bancos principales de México (**BBVA, Nu, Banorte, Santander y Citibanamex**) y un panel de estado de permisos del sistema.

---

## Decisiones Críticas y Preferencias del Usuario

> [!IMPORTANT]
> Confirmadas por el usuario en Fase 1:
> - **Enfoque**: Eliminar por completo los botones de simulación y basarse únicamente en la captura real del sistema Android.
> - **Entidades Bancarias Soportadas**: Los 5 bancos principales de México: BBVA México, Nu Tarjeta/Cuenta, Banorte Móvil, Santander México y Citibanamex Móvil.

---

## 1. Visión General del Módulo

El usuario ya no verá botones de prueba (`+ BBVA`, `+ Nu`, etc.). En su lugar, AetherPay se integrará con el sistema de notificaciones de Android para detectar automáticamente cargos, transferencias, compras y depósitos reales en segundo plano en el momento exacto en que la app del banco emite una alerta.

```
┌────────────────────────────────────────────────────────────────────────┐
│                   Sistema Operativo Android (OS)                       │
│  [App BBVA / Nu / Banorte / Santander / Citibanamex emite alerta push] │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ StatusBarNotification
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│              BankNotificationListenerService (Background)              │
│  1. Filtra por paquete permitido de banco mexicano                     │
│  2. Parsea monto, tipo (gasto/ingreso), comercio y últimos 4 dígitos   │
│  3. Deduplica mediante Hash SHA-256 (previene dobles cargos)           │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ Guarda borrador 'pending'
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│             Buzón de Notificaciones Reales (UI en Compose)             │
│  - Indicador de estado del servicio (Activo / Requiere Permiso)        │
│  - Botón directo para conceder acceso en Ajustes de Android            │
│  - Lista de transacciones detectadas en tiempo real para aprobar/descartar
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Experiencia de Usuario y Diseño Visual

### Flujo de Usuario
1. **Verificación de Permiso de Android**:
   - Al entrar al buzón, la app verifica mediante `NotificationManagerCompat.getEnabledListenerPackages` si AetherPay tiene concedido el permiso especial de Acceso a Notificaciones.
   - Si no está activo, se muestra una tarjeta destacada con acento ámbar/violeta y el botón **"Activar Detección Automática"**, que redirige directamente a `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
   - Si está activo, muestra un badge verde con pulso: **"Detección en tiempo real activa (5 bancos protegidos)"**.
2. **Recepción Pasiva**:
   - Cuando el usuario realiza una compra en un comercio y su banco envía una notificación push, el servicio en segundo plano la procesa y la añade como borrador `pending`.
3. **Buzón Interactivo**:
   - Tarjetas elegantes con el logotipo/color oficial del banco, monto detectado, comercio identificado y categoría sugerida automáticamente.
   - Acciones de 1 toque: **Confirmar Asiento** (lo añade al saldo de la cuenta correspondiente) o **Descartar**.

---

## 3. Arquitectura y Componentes Clave

### A. Parser de Bancos Mexicanos (`BankNotificationParser.kt`)
- Incorporar firmas de paquete y patrones Regex de **Citibanamex** (`com.banamex.bnmchequeras`, `com.citibanamex.movil`, `citibanamex`, `banamex`).
- Refinar expresiones regulares de extracción para manejar formatos:
  - *"Citibanamex: Cargo por $250.00 en UBER..."*
  - *"BBVA: Retiro de $1,000.00 en Cajero..."*
  - *"Nu: Compra de $150.00 aprobada en..."*
  - *"Banorte: Cargo de $420.00..."*
  - *"Santander: Depósito recibido por..."*

### B. Servicio de Escucha (`BankNotificationListenerService.kt`)
- Optimizar la reactividad e inserción segura en Room (`BankNotificationDraftEntity`).
- Registro con `NotificationListenerService` en `AndroidManifest.xml`.

### C. Rediseño de Pantalla (`BankNotificationInboxScreen.kt`)
- **Eliminación Total**: Suprimir la barra `Simulador de Push Bancario` y los botones `SimulateButton`.
- **Nuevo Banner de Estado del Sistema**: Indicador visual dinámico con botón para abrir los Ajustes del Sistema Android si el servicio aún no está autorizado.
- **Insignias de Cobertura Bancaria**: Chips visuales indicando los 5 bancos monitoreados activamente.
- **Gestión de Borradores**: Pestañas de filtrado (Pendientes, Confirmadas, Todas) con vaciado de estados y feedback visual inmediato.

---

## 4. Plan de Verificación

1. **Compilación Limpia**: Ejecutar `compile_applet` para asegurar cero errores de tipos o referencias.
2. **Pruebas Automatizadas**: Correr `gradle :app:testDebugUnitTest` para certificar que el parsing de los 5 bancos, la deduplicación SHA-256 y la persistencia en Room funcionan sin fallos.
3. **Validación de UI**: Verificar que la pantalla cargue sin los botones de simulación y responda con los colores de Material Design 3.
