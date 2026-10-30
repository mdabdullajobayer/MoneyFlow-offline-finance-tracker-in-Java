# MoneyFlow - Database Schema (`SQLite`)

MoneyFlow uses a normalized SQLite database managed via `SQLiteOpenHelper` to ensure reliable offline-first storage and high performance for both personal and business financial tracking.

---

## ERD & Tables Overview

```
+-------------+       +---------------+       +------------------+
|   owners    | 1---N |   accounts    | 1---N |   transactions   |
+-------------+       +---------------+       +------------------+
        |                     |                       |
        | 1                   | 1                     | N
        +----------+          +------------+          |
                   |                       |          |
                   v                       v          v
            +------------+          +------------+  +-------------------+
            | categories |          |payment_meth|  |transaction_types  |
            +------------+          +------------+  +-------------------+
```

---

## Table Definitions

### 1. `owners`
Tracks financial profiles (e.g., Personal vs. Business).
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL) - e.g., "Personal", "Business"
- `type` (TEXT NOT NULL) - "PERSONAL" or "BUSINESS"
- `is_default` (INTEGER DEFAULT 0)

### 2. `accounts`
Financial accounts (Cash, Bank accounts, Mobile Wallets like bKash/Nagad).
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL) - e.g., "Cash in Hand", "City Bank", "bKash"
- `type` (TEXT NOT NULL) - "CASH", "BANK", "MOBILE_WALLET", "CREDIT_CARD"
- `balance` (REAL DEFAULT 0.0)
- `currency` (TEXT DEFAULT "BDT")
- `icon` (TEXT)

### 3. `transaction_types`
Types of transactions.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL) - e.g., "IN", "OUT", "TRANSFER"

### 4. `categories`
Income and expense categories.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL) - e.g., "Food & Dining", "Salary", "Transport"
- `type` (TEXT NOT NULL) - "IN" or "OUT"
- `icon` (TEXT)
- `owner_id` (INTEGER, FOREIGN KEY REFERENCES owners(id))

### 5. `income_sources`
Specific sources of income.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL) - e.g., "Full-time Job", "Freelance", "Client A"
- `owner_id` (INTEGER, FOREIGN KEY REFERENCES owners(id))

### 6. `payment_methods`
Payment mechanisms used.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL) - e.g., "Cash", "Card", "Bank Transfer", "MFS"
- `icon` (TEXT)

### 7. `transactions`
Core ledger table storing all financial transactions.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `owner_id` (INTEGER, FOREIGN KEY REFERENCES owners(id))
- `account_id` (INTEGER, FOREIGN KEY REFERENCES accounts(id))
- `transaction_type_id` (INTEGER, FOREIGN KEY REFERENCES transaction_types(id))
- `category_id` (INTEGER, FOREIGN KEY REFERENCES categories(id))
- `income_source_id` (INTEGER, FOREIGN KEY REFERENCES income_sources(id) NULL)
- `payment_method_id` (INTEGER, FOREIGN KEY REFERENCES payment_methods(id))
- `amount` (REAL NOT NULL)
- `date` (TEXT NOT NULL) - ISO-8601 format (`YYYY-MM-DD HH:MM:SS`)
- `note` (TEXT)
- `payee_payer` (TEXT)
- `is_recurring` (INTEGER DEFAULT 0)
- `attachment_uri` (TEXT)

### 8. `sms_senders`
Trusted SMS senders for automated parsing.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `sender_name` (TEXT NOT NULL) - e.g., "BKash", "CityBank", "SSLWireless"
- `keyword_filter` (TEXT)
- `account_id` (INTEGER, FOREIGN KEY REFERENCES accounts(id))

### 9. `sms_logs`
Logs of incoming financial SMS messages.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `sender` (TEXT NOT NULL)
- `message_body` (TEXT NOT NULL)
- `received_date` (TEXT NOT NULL)
- `parsed_amount` (REAL)
- `status` (TEXT NOT NULL) - "PENDING", "PARSED", "IGNORED"
- `transaction_id` (INTEGER, FOREIGN KEY REFERENCES transactions(id) NULL)

### 10. `budgets`
Budget allocations per category and owner.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `owner_id` (INTEGER, FOREIGN KEY REFERENCES owners(id))
- `category_id` (INTEGER, FOREIGN KEY REFERENCES categories(id))
- `amount` (REAL NOT NULL)
- `start_date` (TEXT NOT NULL)
- `end_date` (TEXT NOT NULL)
- `period` (TEXT DEFAULT "MONTHLY")

### 11. `app_settings`
Application configuration and preferences.
- `id` (INTEGER, PRIMARY KEY AUTOINCREMENT)
- `key` (TEXT UNIQUE NOT NULL)
- `value` (TEXT NOT NULL)

---

## Seed Data

### Initial Owners
1. (1, "Personal", "PERSONAL", 1)
2. (2, "Business", "BUSINESS", 0)

### Initial Transaction Types
1. (1, "IN")
2. (2, "OUT")
3. (3, "TRANSFER")

### Initial Accounts
1. (1, "Cash in Hand", "CASH", 0.0, "BDT")
2. (2, "Prime Bank Account", "BANK", 0.0, "BDT")
3. (3, "bKash Personal", "MOBILE_WALLET", 0.0, "BDT")

### Initial Categories
- **Personal (OUT)**: Food & Dining, Groceries, Transportation, Utilities, Entertainment, Healthcare, Rent
- **Personal (IN)**: Salary, Investments, Gifts
- **Business (OUT)**: Inventory, Salaries, Office Rent, Utilities, Marketing
- **Business (IN)**: Client Sales, Services
