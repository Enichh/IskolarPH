# ISKOLARPH

<div align="center">

![ISKOLARPH Logo](logo_iskolar_ph.svg)

</div>

A comprehensive Android scholarship finder app designed specifically for Filipino students.

<div align="center">

[User Manual](docs/USER_MANUAL.md) | [Download APK](https://github.com/Enichh/ISKOLARPH/releases/latest)

</div>

---

## Overview

ISKOLARPH helps Filipino students discover and apply for scholarships that match their academic profile and location. The app provides personalized scholarship recommendations, AI-powered assistance, and a comprehensive catalog of available opportunities.

---

## Features

### Authentication & Security

- Secure user registration with Firebase-based authentication and email verification
- Password reset functionality via email
- Persistent login with secure token handling
- Automatic cleanup of stale auth sessions on fresh installs

### Scholarship Management

- AI-powered scholarship matching based on GWA and location
- Comprehensive catalog with detailed scholarship information
- Automatic eligibility filtering based on student qualifications
- Bookmark and manage favorite scholarship opportunities
- Track scholarship application status

### AI Assistant

- 24/7 chatbot support powered by LongCat API
- Educational guidance for scholarship applications
- Personalized recommendations based on student profile
- English and Filipino language assistance

### User Experience

- Material Design 3 with modern, intuitive interface
- Bottom navigation for easy access to Home, Catalog, Saved, and Profile
- Offline capability with local Room database storage
- Real-time UI updates using LiveData
- GPS-based scholarship recommendations
- Responsive design optimized for various screen sizes

---

## Tech Stack

### Core Technologies

| Category     | Technology                | Version   |
| ------------ | ------------------------- | --------- |
| Language     | Java                      | 17        |
| Platform     | Android SDK               | API 24-36 |
| Architecture | MVVM + Repository Pattern | -         |
| Build System | Gradle (Kotlin DSL)       | 9.2.0     |

### Android Components

| Component        | Library                   | Version |
| ---------------- | ------------------------- | ------- |
| UI Framework     | Material Design 3         | 1.13.0  |
| AppCompat        | AndroidX AppCompat        | 1.7.1   |
| Activity         | AndroidX Activity         | 1.13.0  |
| ConstraintLayout | AndroidX ConstraintLayout | 2.2.1   |
| RecyclerView     | AndroidX RecyclerView     | 1.3.2   |

### Data & Networking

| Component      | Library  | Version |
| -------------- | -------- | ------- |
| Local Database | Room     | 2.8.4   |
| Networking     | Retrofit | 2.11.0  |
| HTTP Client    | OkHttp   | 4.12.0  |
| JSON Parsing   | Gson     | 2.10.1  |
| Image Loading  | Glide    | 4.16.0  |

### Backend Services

| Service          | Technology                  |
| ---------------- | --------------------------- |
| Authentication   | Firebase Auth               |
| Cloud Database   | Firebase Firestore          |
| AI Integration   | LongCat API                 |
| Email Service    | Firebase Email Verification |
| Alternative Auth | Supabase                    |

### Testing

| Type            | Framework   | Version |
| --------------- | ----------- | ------- |
| Unit Testing    | JUnit       | 4.13.2  |
| Mocking         | Mockito     | 5.7.0   |
| UI Testing      | Espresso    | 3.7.0   |
| Android Testing | Robolectric | 4.11.1  |

---

## Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK API 24-36
- Firebase account (for authentication)

### Download APK

The latest release APK is available on the [GitHub Releases page](https://github.com/Enichh/ISKOLARPH/releases/latest).

### Build from Source

```bash
# Clone the repository
git clone https://github.com/Enichh/ISKOLARPH.git
cd ISKOLARPH

# Build debug APK
./gradlew assembleDebug

# Or open in Android Studio and click Run
```

The APK will be generated at:

```
app/build/outputs/apk/debug/ISKOLARPH-v1.0-debug.apk
```

---

## Installation

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

## Usage

### Sign Up

1. Launch the app
2. Tap "Sign Up"
3. Enter email and create a password (min 6 characters)
4. Provide personal information:
   - First name, Last name, Middle initial
   - GWA (Grade Weighted Average)
   - Location (for personalized recommendations)
5. Submit registration

### Email Verification

1. Check your email for verification link
2. Click the link to verify
3. Return to app and proceed to dashboard

### Browse Scholarships

1. Navigate to "Catalog" tab
2. Use search bar to find specific scholarships
3. Apply filters:
   - Location Filter: Luzon, Visayas, Mindanao, National
   - GPA Filter: Show only scholarships you qualify for
4. Tap any scholarship to view details
5. Tap "Save" button (bookmark icon) to save for later

### Use AI Assistant

1. Tap the floating chat button (bottom-right)
2. Ask questions about:
   - Scholarship eligibility
   - Application tips
   - Essay writing guidance
   - Study strategies
3. Chat history is maintained for the current session

### Manage Saved Scholarships

1. Navigate to "Saved" tab
2. View all bookmarked scholarships
3. Tap to view details or remove from saved

### Profile Management

1. Navigate to "Profile" tab
2. View account information
3. Update GWA or location
4. Access privacy settings

---

## Project Structure

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

Built for Filipino students to discover educational opportunities.
