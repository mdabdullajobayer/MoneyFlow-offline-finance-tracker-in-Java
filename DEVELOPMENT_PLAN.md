# MoneyFlow - Development Plan

MoneyFlow is a robust, offline-first personal and business finance tracker designed for Android, built with Java, XML layouts, Material Design 3, native SQLite (`SQLiteOpenHelper`), MPAndroidChart, Apache POI, and Biometric authentication.

---

## Phased Development Roadmap

### Phase 1: Foundation, Architecture & Database Design (Current Phase)
- [x] Project workspace inspection and dependency setup (`build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts` with JitPack).
- [x] Integration of core libraries: Material Design 3, MPAndroidChart, Apache POI, Biometric, Lifecycle/ViewModel, Navigation Component.
- [x] Creation of comprehensive documentation (`DEVELOPMENT_PLAN.md`, `DATABASE_SCHEMA.md`, `README.md`).
- [ ] Implementation of native SQLite database architecture (`SQLiteOpenHelper`) with 11 core tables and robust initial seeding (owners, accounts, transaction_types, categories, income_sources, payment_methods, etc.).
- [ ] Verification via successful Gradle build (`assembleDebug`).

### Phase 2: UI Foundation, Navigation & Material Design 3 Theming
- [ ] Setup XML layout infrastructure with Material Design 3 components (`Theme.MoneyFlow`, color tokens, typography, shapes).
- [ ] Navigation Component setup (`nav_graph.xml`) with bottom navigation / navigation drawer for Dashboard, Transactions, Budgets, Analytics, and Settings.
- [ ] Dual-profile switcher (Personal vs. Business mode).

### Phase 3: Core Transaction Management & SMS Parser Automation
- [ ] Transaction CRUD (Income, Expense, Transfer) supporting multiple accounts, categories, payment methods, and income sources.
- [ ] Budget tracking and alerts per category/owner.
- [ ] SMS Receiver & Parser for automated bank/MFS transaction logging (`sms_senders`, `sms_logs`).

### Phase 4: Advanced Analytics, Excel/CSV Export & Biometric Security
- [ ] Advanced financial charts and visual reports using MPAndroidChart (PieChart, BarChart, LineChart).
- [ ] Excel & CSV export/import functionality using Apache POI (`poi`, `poi-ooxml`).
- [ ] Biometric lock (`androidx.biometric.BiometricPrompt`) for app-level security.
- [ ] End-to-end testing, bug fixing, and polish.
