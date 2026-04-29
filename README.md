# ISKOLARPH 🎓

<div align="center">

![ISKOLARPH Logo](logo_iskolar_ph.svg)

[![GitHub stars](https://img.shields.io/github/stars/Enichh/ISKOLARPH?style=for-the-badge&logo=github)](https://github.com/Enichh/ISKOLARPH/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Enichh/ISKOLARPH?style=for-the-badge&logo=github)](https://github.com/Enichh/ISKOLARPH/network)
[![GitHub issues](https://img.shields.io/github/issues/Enichh/ISKOLARPH?style=for-the-badge&logo=github)](https://github.com/Enichh/ISKOLARPH/issues)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green?style=for-the-badge&logo=android)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen?style=for-the-badge&logo=android)](https://android-arsenal.com/api?level=24)

**A comprehensive Android scholarship finder app designed specifically for Filipino students**

[📱 Download APK](https://github.com/Enichh/ISKOLARPH/releases/latest) • [📖 User Manual](docs/USER_MANUAL.md) • [🚀 Quick Start](#quick-start)

</div>

---

## 📋 Table of Contents

- [✨ Features](#-features)
- [📸 Screenshots](#-screenshots)
- [🛠️ Tech Stack](#-tech-stack)
- [🚀 Quick Start](#-quick-start)
- [⚙️ Installation](#️-installation)
- [📖 Usage](#-usage)
- [🏗️ Architecture](#️-architecture)
- [📁 Project Structure](#-project-structure)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [👥 Contributors](#-contributors)

---

## ✨ Features

### 🔐 Authentication & Security
- **Secure User Registration** - Firebase-based authentication with email verification
- **Password Reset** - Secure password recovery via email
- **Session Management** - Persistent login with secure token handling
- **Fresh Install Protection** - Automatic cleanup of stale auth sessions

### 🎓 Scholarship Management
- **Smart Matching** - AI-powered scholarship matching based on GWA and location
- **Comprehensive Catalog** - Browse available scholarships with detailed information
- **Eligibility Filtering** - Automatic filtering based on student qualifications
- **Saved Scholarships** - Bookmark and manage favorite scholarship opportunities
- **Application Tracking** - Monitor scholarship application status

### 🤖 AI Assistant
- **24/7 Chatbot Support** - Built-in AI assistant powered by LongCat API
- **Educational Guidance** - Scholarship advice, application tips, study strategies
- **Personalized Recommendations** - Tailored suggestions based on student profile
- **Multi-language Support** - English and Filipino language assistance

### 📱 User Experience
- **Material Design 3** - Modern, intuitive interface with dynamic theming
- **Bottom Navigation** - Easy access to Home, Catalog, Saved, and Profile
- **Offline Capability** - Local data storage with Room database
- **Real-time Updates** - LiveData for reactive UI updates
- **Location Services** - GPS-based scholarship recommendations
- **Responsive Design** - Optimized for various screen sizes and devices

---

## 📸 Screenshots

> *Screenshots will be added here - contribute by adding your own!*

| Login | Dashboard | Catalog | Scholarship Detail |
|-------|-----------|---------|-------------------|
| ![Login](screenshots/login.png) | ![Dashboard](screenshots/dashboard.png) | ![Catalog](screenshots/catalog.png) | ![Detail](screenshots/detail.png) |

---

## 🛠️ Tech Stack

### Core Technologies
| Category | Technology | Version |
|----------|------------|---------|
| **Language** | Java | 17 |
| **Platform** | Android SDK | API 24-36 |
| **Architecture** | MVVM + Repository Pattern | - |
| **Build System** | Gradle (Kotlin DSL) | 9.2.0 |

### Android Components
| Component | Library | Version |
|-----------|---------|---------|
| **UI Framework** | Material Design 3 | 1.13.0 |
| **AppCompat** | AndroidX AppCompat | 1.7.1 |
| **Activity** | AndroidX Activity | 1.13.0 |
| **ConstraintLayout** | AndroidX ConstraintLayout | 2.2.1 |
| **RecyclerView** | AndroidX RecyclerView | 1.3.2 |

### Data & Networking
| Component | Library | Version |
|-----------|---------|---------|
| **Local Database** | Room | 2.8.4 |
| **Networking** | Retrofit | 2.11.0 |
| **HTTP Client** | OkHttp | 4.12.0 |
| **JSON Parsing** | Gson | 2.10.1 |
| **Image Loading** | Glide | 4.16.0 |

### Backend Services
| Service | Technology |
|---------|------------|
| **Authentication** | Firebase Auth |
| **Cloud Database** | Firebase Firestore |
| **AI Integration** | LongCat API |
| **Email Service** | Firebase Email Verification |
| **Alternative Auth** | Supabase |

### Testing
| Type | Framework | Version |
|------|-----------|---------|
| **Unit Testing** | JUnit | 4.13.2 |
| **Mocking** | Mockito | 5.7.0 |
| **UI Testing** | Espresso | 3.7.0 |
| **Android Testing** | Robolectric | 4.11.1 |

---

## 🚀 Quick Start

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK API 24-36
- Firebase account (for authentication)

### Run the App

```bash
# Clone the repository
git clone https://github.com/Enichh/ISKOLARPH.git
cd ISKOLARPH

# Build debug APK
./gradlew assembleDebug

# Or open in Android Studio and click Run (▶️)
```

The APK will be generated at:
```
app/build/outputs/apk/debug/ISKOLARPH-v1.0-debug.apk
```

---

## ⚙️ Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Enichh/ISKOLARPH.git
cd ISKOLARPH
```

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing
3. Add Android app with package name: `com.example.iskolarphh`
4. Download `google-services.json` and place in `app/` directory

### 3. Configure API Keys (Optional)

Create `local.properties` in project root:

```properties
# LongCat API (for AI chatbot)
LONGCAT_API_KEY=your_api_key_here
LONGCAT_API_BASE_URL=https://api.example.com

# Supabase (alternative auth)
SUPABASE_URL=your_supabase_url
SUPABASE_PUBLISHABLE_KEY=your_publishable_key

# Release Keystore (for signing)
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

### 4. Build & Run

```bash
# Build debug version
./gradlew assembleDebug

# Build release version (requires keystore)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

---

## 📖 Usage

### For Students

#### 1. Sign Up
1. Launch the app
2. Tap **"Sign Up"**
3. Enter email and create a password (min 6 characters)
4. Provide personal information:
   - First name, Last name, Middle initial
   - GWA (Grade Weighted Average)
   - Location (for personalized recommendations)
5. Submit registration

#### 2. Email Verification
1. Check your email for verification link
2. Click the link to verify
3. Return to app and proceed to dashboard

#### 3. Browse Scholarships
1. Navigate to **Catalog** tab
2. Use search bar to find specific scholarships
3. Apply filters:
   - **Location Filter**: Luzon, Visayas, Mindanao, National
   - **GPA Filter**: Show only scholarships you qualify for
4. Tap any scholarship to view details
5. Tap **Save** button (bookmark icon) to save for later

#### 4. Use AI Assistant
1. Tap the **floating chat button** (bottom-right)
2. Ask questions about:
   - Scholarship eligibility
   - Application tips
   - Essay writing guidance
   - Study strategies
3. Chat history is maintained for the current session

#### 5. Manage Saved Scholarships
1. Navigate to **Saved** tab
2. View all bookmarked scholarships
3. Tap to view details or remove from saved

#### 6. Profile Management
1. Navigate to **Profile** tab
2. View account information
3. Update GWA or location
4. Access privacy settings

---

## 🏗️ Architecture

ISKOLARPH follows **MVVM (Model-View-ViewModel)** architecture with Repository pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                        PRESENTATION                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Activity   │  │   Fragment   │  │    Dialog    │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
├─────────┼─────────────────┼─────────────────┼─────────────┤
│         │                 │                 │             │
│    ┌────▼─────────────────▼─────────────────▼────┐        │
│    │              ViewModel Layer               │        │
│    │  (LoginViewModel, CatalogViewModel, ...)  │        │
│    └────┬───────────────────────────────┬───────┘        │
├─────────┼───────────────────────────────┼─────────────────┤
│         │                               │                 │
│  ┌──────▼──────┐              ┌─────────▼────────┐      │
│  │ Repository  │              │    Repository    │      │
│  │  (Student)  │              │  (Scholarship)   │      │
│  └──────┬──────┘              └─────────┬────────┘      │
├─────────┼───────────────────────────────┼─────────────────┤
│         │                               │                 │
│  ┌──────▼──────┐              ┌─────────▼────────┐      │
│  │     DAO     │              │       DAO        │      │
│  │  (Room DB)  │              │     (Room DB)    │      │
│  └─────────────┘              └──────────────────┘      │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │           API Service (Retrofit)                  │  │
│  │  ┌──────────────┐  ┌──────────────────────────┐   │  │
│  │  │ LongCat API  │  │   Firebase/Supabase      │   │  │
│  │  └──────────────┘  └──────────────────────────┘   │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Principles

| Principle | Implementation |
|-----------|-----------------|
| **Single Responsibility** | Each class has one purpose (Activity = UI, ViewModel = Logic, Repository = Data) |
| **Open/Closed** | Extensible through interfaces (Callback classes, Service abstractions) |
| **Dependency Inversion** | ViewModels depend on Repository abstractions, not concrete implementations |
| **Interface Segregation** | Small, focused interfaces (InsertCallback, DeleteCallback, etc.) |

---

## 📁 Project Structure

```
app/src/main/java/com/example/iskolarphh/
├── MainActivity.java              # Entry point with bottom navigation
├── LoginActivity.java             # Firebase authentication
├── SignupActivity.java            # User registration
├── EmailVerificationActivity.java # Email verification flow
├── PasswordResetActivity.java     # Password recovery
│
├── adapter/                       # RecyclerView adapters
│   └── ScholarshipAdapter.java    # Scholarship list adapter
│
├── api/                           # API layer
│   ├── LongcatApiService.java     # AI chatbot API interface
│   ├── RetrofitClient.java        # Retrofit configuration
│   └── ApiClientConfig.java       # API client settings
│
├── callback/                      # Async operation callbacks
│   ├── DeleteCallback.java
│   ├── InsertCallback.java
│   ├── LocationCallback.java
│   ├── StudentCallback.java
│   └── UpdateCallback.java
│
├── database/                      # Local data layer
│   ├── AppDatabase.java           # Room database singleton
│   ├── dao/                       # Data Access Objects
│   │   ├── ScholarshipDao.java
│   │   ├── StudentDao.java
│   │   └── PrivacyConsentDao.java
│   └── entity/                    # Data models
│       ├── Scholarship.java
│       ├── Student.java
│       └── PrivacyConsent.java
│
├── di/                            # Dependency Injection
│   ├── DependencyFactory.java     # Manual DI factory
│   └── ViewModelFactory.java      # ViewModel provider
│
├── model/                         # API models
│   ├── ChatMessage.java
│   ├── LongcatRequest.java
│   └── LongcatResponse.java
│
├── repository/                    # Data access layer
│   ├── ScholarshipRepository.java
│   ├── StudentRepository.java
│   └── PrivacyConsentRepository.java
│
├── service/                       # Business logic services
│   ├── GeocoderService.java       # Location geocoding
│   ├── LocationFlowManager.java   # Permission handling
│   ├── LocationManager.java       # GPS location
│   ├── ScholarshipFilterService.java
│   ├── ScholarshipDatabaseSeeder.java
│   └── SupabaseVerificationService.java
│
├── ui/                            # UI components
│   ├── CatalogFragment.java       # Scholarship catalog
│   ├── DashboardFragment.java     # Home dashboard
│   ├── SavedFragment.java         # Saved scholarships
│   ├── ProfileFragment.java       # User profile
│   ├── ChatbotDialog.java         # AI assistant dialog
│   ├── LocationPermissionDialog.java
│   ├── LogoutConfirmationDialog.java
│   ├── DialogManager.java         # Dialog utilities
│   ├── MessageAdapter.java        # Chat messages
│   └── ScholarshipDetailActivity.java
│
├── util/                          # Utility classes
│   ├── LocationConstants.java
│   ├── LocationPreferences.java
│   └── PerformanceMonitor.java
│
├── utils/                         # Additional utilities
│   ├── NetworkUtils.java
│   └── SearchDebounceHelper.java
│
└── viewmodel/                     # ViewModels
    ├── CatalogViewModel.java
    ├── DashboardViewModel.java
    ├── LoginViewModel.java
    ├── SignupViewModel.java
    ├── ProfileViewModel.java
    ├── LocationViewModel.java
    └── StudentViewModel.java
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'feat: add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting, etc.)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Build process or auxiliary tool changes

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Contributors

Thanks to these amazing people who made this project possible:

| Name | Role |
|------|------|
| **James Viray** | Lead Developer |
| **Jason Villareal** | Backend Developer |
| **Krizia Mae Oliva** | UI/UX Designer |
| **Jayron Mina** | QA Engineer |
| **Enoch Astor** | Project Manager |
| **Andre Victorino** | Documentation |

---

## 📞 Contact & Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/Enichh/ISKOLARPH/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/Enichh/ISKOLARPH/discussions)
- 📧 **Email**: Contact contributors via GitHub

---

<div align="center">

Built with ❤️ for Filipino students

**[⬆ Back to Top](#iskolarph-)**

</div>
