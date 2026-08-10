# 🐱 Totoro

[![Download Totoro](https://img.shields.io/badge/Download-Totoro%20APK-4CAF50?style=for-the-badge&logo=android&logoColor=white)](https://totoro-blush.vercel.app/#download)
[![Website](https://img.shields.io/badge/Visit-Website-00bcd4?style=for-the-badge&logo=google-chrome&logoColor=white)](https://totoro-blush.vercel.app/)

Totoro is a free, high-performance Android manga and manhwa reading application. It is built to offer a seamless, instant-reading experience without the hassle of configuring external repositories or hunting down broken extensions.

> [!NOTE]  
> **Totoro is a modern, feature-rich fork of [Kotatsu](https://github.com/koitharu/kotatsu).** While keeping the solid foundation of the original project, we have introduced game-changing improvements, cutting-edge new features, and robust backend optimizations.

---

## 🚀 Key Improvements & New Features

Compared to the upstream project, Totoro introduces massive enhancements:

### 🤖 1. AI Pick (Powered by Groq)
Search for your next read using natural language. Want *"an action manhwa with system leveling and romance"*? Simply type it. 
* **Natural Language Processing**: Uses advanced Groq API inference (using `llama-3.3-70b-versatile`) to extract structured filters (genres, demographics, chapters) from user prompts.
* **Smart Caching**: Built-in in-memory cache and local history database (`Room`) to keep search experiences fast and cost-efficient.
* **Graceful Fallbacks**: Intelligent keyword matching automatically kicks in if the user is offline or has reached their rate limits.

### 🔍 2. Optimized Search & Deduplication
Searching over **1,200+ built-in sources** simultaneously can lead to cluttered duplicate results. Totoro resolves this with a custom deduplication engine that groups similar titles dynamically, offering a clean, unified list of recommendations.

### 🔔 3. Firebase Push Notifications
Never miss an update on your favorite titles. Integrated with **Firebase Cloud Messaging (FCM)** to deliver instant push notifications whenever new chapters are released.

### 🔒 4. Secure API Credentials
All sensitive API keys and certificates (e.g. Groq credentials) are securely externalized to `local.properties` and injected dynamically through Gradle `BuildConfig`, preventing credential leaks.

---

## 📱 Getting Started & Configuration

If you are building the project from source, you must configure your local environment keys.

### 1. Configure API Keys
Add your Groq API Key to your root `local.properties` file:
```properties
GROQ_API_KEY=gsk_your_groq_api_key_here
```
During build, Gradle will automatically inject this key into `BuildConfig.GROQ_API_KEY` for use by the application.

### 2. Gradle Build
Open the project in Android Studio (using Arctic Fox or newer), sync Gradle, and run:
```bash
./gradlew assembleDebug
```

---

## 🌍 Website & Downloads

* **Official Website**: [https://totoro-blush.vercel.app/](https://totoro-blush.vercel.app/)
* **Download APK**: [Download Totoro](https://totoro-blush.vercel.app/#download)

---

## 🤝 Acknowledgements & License

Totoro is proudly built as a fork of [Kotatsu](https://github.com/koitharu/kotatsu). We express our gratitude to the original developers of Kotatsu and the community for providing a solid baseline. Totoro is released under the same terms of licensing as the upstream project.
