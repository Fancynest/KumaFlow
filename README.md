<div align="center">
  <h1>🐻 KumaFlow</h1>
  <p><b>A beautifully crafted, privacy-first Android personal finance manager.</b></p>

  [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue?logo=kotlin)](#)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Ready-green?logo=android)](#)
  [![Room](https://img.shields.io/badge/Room-Database-orange?logo=sqlite)](#)
  [![Offline First](https://img.shields.io/badge/Privacy-100%25_Offline-purple)](#)
</div>

<br/>

**KumaFlow** is a lightweight, privacy-focused Android application designed for managing personal cash flow. Built entirely offline with no external servers, it is tailored for users who value data security and a stunning, distraction-free aesthetic. 

---

## ✨ Features

- 🔐 **Fort Knox Security**: Keep your data safe with **Biometric Authentication** (Fingerprint/Face ID) and a 6-digit PIN.
- 🎨 **Dynamic Aesthetic**: Enjoy a sleek Dark Mode or turn on **AMOLED Fusion** for pure black backgrounds. Features custom themes and dynamic colors.
- 📊 **Smart Reports & Wrapped**: Visualize your spending habits with interactive charts. Check out your personalized **"KumaFlow Wrapped"** at the end of every month.
- 🧮 **Split Wallet Logic**: Advanced transaction handling that calculates and allocates split transaction values across different wallets effortlessly.
- 🕵️ **100% Privacy-First**: No servers. No telemetry. Your data is stored locally via Room Database and never leaves your device.
- 💾 **Export Your Data**: Easily export your financial records to PDF or CSV.
- 🪶 **Extremely Lightweight**: Highly optimized APK size (~17MB) with custom background Services & Alarm Managers that bypass aggressive OS battery optimizations.

---

## 🛠️ Architecture & Tech Stack

KumaFlow recently underwent a major architectural refactor to adhere to **industry-standard clean architecture** principles. 

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (100% Declarative UI)
- **Local Storage:** Room Database (with robust DAO and Migration support)
- **Background Tasks:** Kotlin Coroutines & Flow
- **Structure:** Modularized screens (`LockScreen`, `HomeScreen`, `ReportScreen`, `SettingsScreen`) for extreme maintainability and clean code practices.

---

## 🚀 Getting Started

Want to run KumaFlow or explore the code? Here is how to get started:

### Prerequisites
- **Android Studio** (Koala or newer recommended)
- **Min SDK:** 27 (Android 8.1 Oreo)
- **Target SDK:** 34

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Fancynest/KumaFlow.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and ensure all dependencies are downloaded.
4. Run the app on your emulator or physical device.

---

## 👨‍💻 Author

Crafted with passion by **Gabriel** 🐻.

## 📄 License

This project is intended for educational and portfolio purposes. Feel free to clone, learn from the architecture, and explore the power of Jetpack Compose!
