# MoneyFlow — Offline Personal & Business Finance Tracker

MoneyFlow is a feature-rich, privacy-focused, offline-first personal and business financial management Android application. Built natively with **Java 11**, **SQLite**, and **Material Design 3**, MoneyFlow empowers users to seamlessly track income, expenses, and account transfers across personal and business profiles with real-time SMS automation, visual analytics, data security, and flexible backup options.

---

## 🌟 Key Features

- 👤 **Dynamic Profiles & Entities**: Manage dynamic Owners, Financial Accounts, Expense/Income Categories, Income Sources, and Payment Methods.
- 💸 **Manual Transactions**: Comprehensive logging for **Income**, **Expense**, and **Transfer** transactions with date, category, owner, account, payment method, notes, and attachment support.
- 📱 **SMS Automation & Smart Parser**: Real-time background detection and parsing of bank and MFS SMS messages (bKash, Nagad, Rocket, Bank SMS) with push notifications and a dedicated SMS review queue.
- 📊 **One-Click Dashboard & Analytics**: Intuitive monthly and yearly financial dashboards powered by **MPAndroidChart** featuring interactive bar charts, pie breakdowns, and cash flow trends.
- 📊 **Excel & CSV Export**: Export detailed financial reports, transaction ledgers, and account statements directly to **Excel (.xlsx)** and **CSV** formats using **Apache POI**.
- 💾 **Local JSON Backup & Restore**: Safeguard financial records with local JSON export and restore capabilities with zero reliance on cloud servers.
- 🔐 **PIN & Biometric Security**: Secure sensitive financial data with device biometric authentication (Fingerprint / Face Unlock) and fallback PIN lock using AndroidX Biometric.
- 🔣 **Dynamic Currency Selector**: Support for global currencies (BDT ৳, USD $, EUR €, GBP £, INR ₹, etc.) and custom currency symbols.
- 🎨 **Material Design 3 UI**: Modern, elegant, dynamic light/dark themed Material 3 interface following native Android UX guidelines.

---

## 🛠️ Tech Stack

- **Programming Language**: Java 11
- **Database**: Native SQLite (`SQLiteOpenHelper`)
- **Architecture**: MVVM Architecture (ViewModel, LiveData, Lifecycle)
- **UI Components**: Material Design 3 (`com.google.android.material:material`), XML Layouts
- **Data Visualization**: MPAndroidChart (`v3.1.0`)
- **Document Processing**: Apache POI (`poi`, `poi-ooxml`)
- **Security & Auth**: AndroidX Biometric (`androidx.biometric:biometric`)
- **JSON Processing**: Gson

---

## 🚀 Installation & Setup Instructions

### Prerequisites
- **Android Studio** (Ladybug / Jellyfish or newer recommended)
- **JDK 11** configured as Gradle JDK
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 34/35

### Building from Source

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/mdabdullajobayer/PersonalFinanceExpenseTrackerAndroidApp.git
   cd PersonalFinanceExpenseTrackerAndroidApp
   ```

2. **Open in Android Studio**:
   - Select **File > Open** and select the project root directory.

3. **Gradle Sync**:
   - Allow Gradle to download dependencies and sync the project structure.

4. **Build & Run**:
   - Connect an Android device or launch an Android Virtual Device (AVD) running API 24+.
   - Click **Run > Run 'app'** or execute:
     ```bash
     ./gradlew assembleDebug
     ```

5. **Permissions Setup**:
   - To enable SMS parsing, grant `RECEIVE_SMS` and `READ_SMS` permissions when prompted in the app.

---

## 👨‍💻 Developer Profile

**MD. Abdullah Jovayer**  
*Software Developer*  
📍 Dhaka, Bangladesh  

- 📧 **Email**: [mdabdullajovayer@gmail.com](mailto:mdabdullajovayer@gmail.com)
- 🐙 **GitHub**: [mdabdullajobayer](https://github.com/mdabdullajobayer)
- 💼 **LinkedIn**: [mdabdullahjobayer](https://linkedin.com/in/mdabdullahjobayer)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
