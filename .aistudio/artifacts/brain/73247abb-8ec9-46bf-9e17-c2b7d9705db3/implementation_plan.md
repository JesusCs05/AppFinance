# Plan de Implementación: Calendario Futuro, Selector de Fechas y Gestión de Tarjetas y Presupuestos

Plan para enriquecer el calendario financiero con registro de movimientos a futuro, selector de fecha interactivo en la creación/edición de transacciones, administración completa de cuentas/tarjetas (edición y eliminación mediante menú contextual de 3 puntos) y gestión avanzada de presupuestos por categoría.

## User Review & Critical Decisions

> [!IMPORTANT]
> Decisiones confirmadas a partir de las respuestas del usuario en Fase 1:
> - **Visualización de movimientos a futuro**: Se marcarán como **movimientos programados** con indicador visual destacado (píldora violeta/ámbar con icono de reloj/calendario) y alerta de fecha/vencimiento tanto en el Calendario como en el Dashboard.
> - **Gestión de cuentas y tarjetas**: Cada tarjeta en el carrusel de cuentas contará con un **menú de 3 puntos (`MoreVert`)** accesible que desplegará las opciones de **Editar Cuenta** y **Eliminar Cuenta** (con confirmación de seguridad).
> - **Presupuestos por categoría**: Se añadirá la capacidad de editar montos directamente por teclado o deslizador, y el botón para **Eliminar/Restablecer Presupuesto** (fijando límite en $0.00 / sin límite activo).

---

## 1. Overview & Core Concept

- **Qué resuelve**: Actualmente, la creación de transacciones asume siempre el momento actual (`System.currentTimeMillis()`) y el calendario sólo muestra lo ocurrido en el pasado. El usuario necesita planificar gastos/ingresos futuros (pagos de servicios, vencimientos de tarjetas, cobros programados) con un selector de fecha accesible, poder añadir un movimiento directamente en el día seleccionado del calendario, y tener control total sobre sus tarjetas/cuentas y presupuestos para modificarlos o borrarlos según cambie su realidad financiera.
- **Público objetivo**: Usuarios que administran finanzas personales activas en México/LATAM y desean anticipar sus compromisos financieros en lugar de sólo registrarlos después de que ocurrieron.
- **Valor clave**: Transformar AetherPay de una libreta de registro histórico a un **planificador financiero interactivo y proactivo**.

---

## 2. User Experience & Visual Design

### 2.1 Flujo del Calendario y Acciones a Futuro
1. **Navegación y Selección de Fechas**:
   - El usuario navega entre meses y pulsa cualquier día (pasado, presente o futuro).
   - En la tarjeta del día seleccionado se visualizan los movimientos agrupados:
     - Movimientos ya ejecutados (verde para ingresos, rojo para gastos).
     - **Compromisos y Gastos Futuros Programados**: Mostrados con una tarjeta distintiva de borde acentuado en violeta/ámbar y etiqueta `"PROGRAMADO"`.
2. **Botón "+ Añadir en esta fecha"**:
   - En la tarjeta del día seleccionado se añade el botón de acción rápida `+ Añadir en este día`, el cual abre la pantalla de transacción preseleccionando la fecha seleccionada.
3. **Indicador de puntos en la cuadrícula mensual**:
   - Días con movimientos futuros mostrarán un punto ámbar/violeta distintivo para identificar visualmente compromisos pendientes en el mes.

### 2.2 Selector de Fecha en Formulario de Transacción (`TransactionCustomizerScreen`)
- **Tarjeta de Fecha Interactiva**:
  - Ubicada prominentemente en el formulario debajo del tipo de movimiento y monto.
  - Muestra la fecha seleccionada en formato amigable: *"Hoy, 8 de noviembre"*, *"Mañana, 9 de noviembre"* o *"Jueves, 15 de noviembre"*.
  - Al pulsar la tarjeta, se despliega el **`DatePickerDialog` de Material 3**.
  - Si la fecha seleccionada es posterior al día de hoy, se activa automáticamente la insignia visual: **"Acción Programada a Futuro"** con tono violeta y texto aclaratorio.

### 2.3 Gestión de Cuentas y Tarjetas (`AccountPillCard`)
- En el carrusel de cuentas, cada tarjeta incluye un icono discreto de 3 puntos verticales (`MoreVert`) con tamaño táctil accesible (48dp).
- Al pulsar el menú se presentan:
  - **Editar Cuenta**: Abre el diálogo `EditAccountDialog` prellenado con nombre, tipo, últimos 4 dígitos, saldo y color.
  - **Eliminar Cuenta**: Despliega un diálogo de confirmación de advertencia antes de remover la cuenta.

### 2.4 Gestión de Presupuestos (`CategoryBudgetEditorDialog`)
- Cada categoría de gasto incluye:
  - Entrada de texto para escribir el límite mensual exacto y barra deslizante.
  - Botón `"Eliminar Presupuesto"` / `"Restablecer"` para dejar la categoría sin presupuesto activo.
  - Indicador de estado: *"Sin límite"* o *"Presupuesto: $X MXN"*.

---

## 3. Key Product Decisions & Trade-Offs

- **Tratamiento de Transacciones Futuras**:
  - *Decisión*: Almacenar las transacciones futuras en la tabla `transactions` con su marca de tiempo `occurredAt` en el futuro. En los cálculos de balance actual del Dashboard se calcula el balance consolidado actual y se muestra un bloque de `"Próximos Vencimientos y Compromisos"` para que el saldo actual no se confunda con desembolsos aún no efectuados.
  - *Razón*: Mantiene la integridad del modelo de datos sin requerir migraciones complejas ni tablas duplicadas, mientras permite que el Calendario y el Dashboard muestren la línea de tiempo completa.
- **Edición y Eliminación en Base de Datos**:
  - *Decisión*: `AccountDao` ya cuenta con `updateAccount` y `deleteAccount`. Se exponen directamente en `FinanceRepository` y `FinanceViewModel` para actualizar reactivamente los `StateFlow` recolectados en Compose.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI                             │
├──────────────────────────┬─────────────────────────┬───────────────────┤
│ CalendarAnalyticsScreen  │ TransactionCustomizer   │ DashboardScreen   │
│ - Días futuros/pasados   │ - DatePicker selector   │ - 3-dots Menu en  │
│ - Botón "+ Añadir fecha" │ - "Acción Programada"   │   AccountPillCard │
│ - Badges "PROGRAMADO"    │ - Default: Hoy / Pre-sel│ - Budget Dialog   │
└────────────┬─────────────┴────────────┬────────────┴─────────┬─────────┘
             │                          │                      │
             ▼                          ▼                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          FinanceViewModel                              │
│ - openNewTransaction(initialDate: Long?)                               │
│ - updateAccount(account: AccountEntity)                                │
│ - deleteAccount(account: AccountEntity)                                │
│ - updateCategoryBudget(category: CategoryEntity)                       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    Room Local Database (aetherpay_clean.db)            │
│ ┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────┐ │
│ │   TransactionEntity  │ │    AccountEntity     │ │  CategoryEntity  │ │
│ │ occurredAt (futuro)  │ │   CRUD completo      │ │ monthlyLimit: 0  │ │
│ └──────────────────────┘ └──────────────────────┘ └──────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### Componentes a Modificar:
1. **`TransactionCustomizerScreen.kt`**:
   - Integrar selector de fecha Material 3 con formato amigable.
   - Soportar `initialDate` pasado desde el ViewModel.
   - Guardar `occurredAt` con la fecha seleccionada por el usuario.
2. **`CalendarAnalyticsScreen.kt`**:
   - Añadir botón `+ Añadir en esta fecha`.
   - Distinguir transacciones futuras con distintivo `"PROGRAMADO"`.
   - Soporte de multi-puntos con color violeta para compromisos futuros.
3. **`AccountBudgetDialogs.kt`**:
   - Añadir `EditAccountDialog` reutilizable con datos existentes.
   - Enriquecer `CategoryBudgetEditorDialog` con eliminación/restablecimiento de presupuesto y campo numérico.
4. **`DashboardScreen.kt`**:
   - Actualizar `AccountPillCard` con menú de 3 puntos (`MoreVert`), `DropdownMenu`, acciones de editar y eliminar cuenta.
5. **`FinanceViewModel.kt`**:
   - Agregar `_preselectedDate: MutableStateFlow<Long?>` y firma `openNewTransaction(initialDate: Long? = null)`.
   - Añadir funciones `updateAccount(account: AccountEntity)` y `deleteAccount(account: AccountEntity)`.
