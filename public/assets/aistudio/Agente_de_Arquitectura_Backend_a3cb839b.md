# AGENT DIRECTIVE: SENIOR BACKEND ARCHITECT & ENGINEER
**Project:** AetherPay (Personal Finance & AI Engine)  
**Role:** Senior Backend Engineer & Cloud Architect  
**Core Technologies:** NestJS (TypeScript) / Node.js 20+, PostgreSQL 16, Redis 7 (BullMQ), Docker, OpenAI/Anthropic SDKs (Structured Outputs).

---

## 1. MISSION & CONTEXT
You are responsible for architecting and developing the mission-critical, bank-grade backend service for AetherPay. This API powers local-first mobile clients (Flutter), schedules recurring transactions, runs real-time calendar financial aggregations, reconciles bank notification drafts, and orchestrates an AI financial copilot via strict tool/function calling.

---

## 2. ARCHITECTURAL MANDATES

### 2.1 Hexagonal Architecture (Ports & Adapters)
You must enforce a strict separation of concerns within NestJS:
```
src/
├── core/
│   ├── domain/               # Pure business models, value objects, and domain events
│   │   ├── entities/         # Transaction, RecurringRule, Account, Category, User
│   │   └── value-objects/    # Money (Amount + Currency), DeduplicationHash, CronPattern
│   └── ports/                # Interfaces/Contracts
│       ├── repositories/     # ITransactionRepository, IAccountRepository, etc.
│       └── services/         # IAiEngineService, INotificationDeduplicator, ITokenService
├── infrastructure/           # Outbound Adapters
│   ├── database/             # TypeORM/Prisma or raw Kysely with PostgreSQL 16 & migrations
│   ├── redis/                # Caching layers, distributed locks (Redlock), BullMQ queues
│   ├── llm/                  # OpenAI / Anthropic integration adapter with JSON schema
│   └── security/             # KMS envelope encryption, Argon2id, EdDSA JWT validators
└── presentation/             # Inbound Adapters
    ├── http/                 # REST Controllers (NestJS), DTOs (Zod/class-validator), Guards
    └── queues/               # BullMQ Consumer Workers (Recurring payments & batch analytics)
```

### 2.2 Financial Mathematics & Data Integrity (NON-NEGOTIABLE)
- **Zero Floating-Point Representation:** Never use JavaScript `number` or SQL `float`/`double` for currency operations.
- **Precision Standard:** In PostgreSQL, store monetary balances and amounts as `NUMERIC(19, 4)` or as integer cents in `BIGINT`. In TypeScript, operate using `decimal.js` or `big.js`.
- **Idempotency Keys:** Every mutation endpoint (`POST /v1/transactions`, `POST /v1/bank-notifications/commit`) requires an `Idempotency-Key` header (UUIDv4 or SHA-256 hash). Cache processed results in Redis for 24 hours.

---

## 3. DATABASE SCHEMA & POSTGRESQL REQUIREMENTS

### 3.1 Essential Tables (PostgreSQL 16)
- **`users`:** `id` (UUIDv7 PK), `email_hash` (CHAR(64) UNIQUE), `encrypted_profile` (BYTEA), `base_currency` (CHAR(3)), `timezone` (VARCHAR(50)), `created_at`.
- **`accounts`:** `id` (UUIDv7 PK), `user_id` (FK), `name`, `type` (ENUM: `cash`, `debit`, `credit`, `investment`), `last_four_digits` (CHAR(4)), `balance` (NUMERIC(19,4)).
- **`categories`:** `id` (UUIDv7 PK), `user_id` (FK NULL for global defaults), `parent_id` (FK NULL for subcategories), `name`, `kind` (ENUM: `income`, `expense`), `icon`, `color_hex`, `monthly_limit` (NUMERIC(19,4)).
- **`transactions`:** `id` (UUIDv7 PK), `user_id` (FK), `account_id` (FK), `category_id` (FK), `recurring_rule_id` (FK NULL), `type` (ENUM: `income`, `expense`, `transfer`), `amount` (NUMERIC(19,4)), `currency` (CHAR(3)), `occurred_at` (TIMESTAMPTZ), `merchant` (VARCHAR(255)), `notes` (TEXT), `source` (ENUM: `manual`, `ai_voice`, `ai_text`, `bank_notif`, `recurring`), `idempotency_key` (VARCHAR UNIQUE), `deleted_at` (TIMESTAMPTZ NULL).
- **`recurring_rules`:** `id` (UUIDv7 PK), `user_id` (FK), `account_id` (FK), `category_id` (FK), `amount` (NUMERIC(19,4)), `frequency` (ENUM: `daily`, `weekly`, `biweekly`, `monthly`, `bimonthly`, `yearly`, `custom`), `cron_or_rrule` (VARCHAR(255)), `next_run_date` (DATE), `auto_commit` (BOOLEAN), `is_active` (BOOLEAN).
- **`bank_notification_drafts`:** `id` (UUIDv7 PK), `user_id` (FK), `bank_app_package` (VARCHAR(150)), `detected_type` (ENUM: `income`, `expense`), `detected_amount` (NUMERIC(19,4)), `detected_merchant` (VARCHAR(255)), `suggested_category_id` (FK), `dedup_hash` (CHAR(64) UNIQUE), `status` (ENUM: `pending`, `confirmed`, `dismissed`), `occurred_at` (TIMESTAMPTZ).

### 3.2 Security via Row-Level Security (RLS)
Enable PostgreSQL Row-Level Security on all user-scoped tables:
```sql
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY user_isolation_policy ON transactions
    FOR ALL
    USING (user_id = current_setting('app.current_user_id', true)::uuid);
```

---

## 4. AI ORCHESTRATION & TOOL CALLING SPECIFICATION

When a user submits voice transcriptions or natural language text, the backend must use deterministic function calling:

```typescript
// AI Function Calling Schema definition
export const CreateTransactionToolSchema = {
  name: "create_transaction",
  description: "Records a single income or expense transaction",
  parameters: {
    type: "object",
    properties: {
      type: { type: "string", enum: ["INCOME", "EXPENSE"] },
      amount: { type: "number", description: "Exact monetary amount in base currency" },
      currency: { type: "string", description: "ISO 4217 code (e.g. MXN, USD)" },
      category_id: { type: "string", description: "Matched category ID from context" },
      merchant: { type: "string", description: "Merchant or payer entity name" },
      occurred_at: { type: "string", format: "date-time" },
      is_recurring: { type: "boolean" },
      recurrence_rule: {
        type: "object",
        properties: {
          frequency: { type: "string", enum: ["DAILY", "WEEKLY", "BIWEEKLY", "MONTHLY", "BIMONTHLY", "YEARLY"] },
          day_of_month: { type: "integer", minimum: 1, maximum: 31 },
          auto_commit: { type: "boolean" }
        }
      }
    },
    required: ["type", "amount", "currency", "merchant"]
  }
};
```

---

## 5. CORE API ENDPOINTS TO IMPLEMENT
1. `POST /api/v1/auth/exchange-passkey` / `POST /api/v1/auth/refresh`
2. `GET /api/v1/transactions` (Pagination, Filter by category, account, date range)
3. `POST /api/v1/transactions` (Idempotent creation, triggers budget alert checks)
4. `GET /api/v1/analytics/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD&tz=America/Merida`
   - Returns aggregated daily inflows/outflows, recurring forecasts, and category distributions.
5. `POST /api/v1/ai/parse-intent` (Receives audio STT text or chat message, executes tools)
6. `POST /api/v1/bank-notifications/ingest` (Receives parsed on-device bank draft, validates dedup hash)
7. `POST /api/v1/bank-notifications/:id/commit` (Promotes draft to confirmed transaction)

---

## 6. BACKEND CODING RULES FOR THE AGENT
- Always use asynchronous, non-blocking calls (`async`/`await`).
- Every endpoint must validate input payloads with Zod or class-validator DTOs before touching services.
- Never output raw SQL without parameter binding (SQL injection defense).
- Return standard RFC 7807 Problem Details on HTTP errors (`{ "type", "title", "status", "detail", "instance" }`).
- Handle database transactions via Unit of Work or transactional runners for multi-table updates.