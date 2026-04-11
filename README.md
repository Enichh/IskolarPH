# ISKOLARPH

![GitHub stars](https://img.shields.io/github/stars/Enichh/ISKOLARPH?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/Enichh/ISKOLARPH?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues/Enichh/ISKOLARPH?style=for-the-badge)

## About

ISKOLARPH is an Android scholarship finder application designed specifically for Filipino students. The app helps students discover relevant scholarship opportunities by matching their academic performance (GWA) and location with available scholarship programs.

### Problem Statement

Many Filipino students struggle to find scholarships that match their qualifications and geographic location. The process of searching through various scholarship databases is time-consuming and often results in missed opportunities.

### Solution

ISKOLARPH simplifies this process by providing a mobile application that:

- Collects student's Grade Weighted Average (GWA) and location
- Matches students with scholarships they qualify for
- Provides a user-friendly interface to browse and apply for scholarships
- Ensures secure user authentication through Firebase

## Features

- **User Authentication**: Secure sign-up and login with Firebase Authentication
- **Email Verification**: Mandatory email verification to ensure account security
- **Profile Management**: Students can input their GWA and location for scholarship matching
- **Scholarship Catalog**: Browse available scholarships filtered by eligibility
- **AI Chatbot Assistant**: Built-in AI assistant powered by LongCat API for scholarship guidance, application tips, and study advice
- **Material Design 3**: Modern, intuitive UI with dark mode support
- **Offline Data Storage**: Room Database for local data persistence
- **Real-time Updates**: LiveData for reactive UI updates
- **Scholarship Details**: Detailed view of scholarship programs with eligibility and application information

## Tech Stack

- **Language**: Java 17
- **Platform**: Android (minSdk: 24, targetSdk: 36)
- **Architecture**: MVVM pattern with Repository pattern
- **Database**: Room (SQLite) with LiveData
- **Authentication**: Firebase Auth with Email Verification
- **UI**: Material Design 3 with Bottom Navigation
- **AI Integration**: Retrofit + LongCat API for chatbot functionality
- **Build System**: Gradle with Kotlin DSL
- **Firebase**: BOM 32.7.0, Firebase Auth
- **Networking**: Retrofit 2.9.0, OkHttp Logging Interceptor

## Prerequisites

Before running this project, ensure you have:

- Android Studio Hedgehog or later installed
- JDK 17 configured
- Android SDK API 24-36
- A Firebase account (for authentication setup)
- A physical Android device or emulator running Android 7.0 (API 24) or higher

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Enichh/ISKOLARPH.git
cd ISKOLARPH
```

### 2. Open in Android Studio

Open the project folder in Android Studio and wait for Gradle sync to complete.

### 3. Build and Run

```bash
# Build the project
./gradlew build

# Install on connected device/emulator
./gradlew installDebug
```

Or use Android Studio's Run button (▶️) to build and install the app.

## Usage

### Sign Up

1. Launch the app
2. Click "Sign Up"
3. Enter your email address and create a password
4. Provide your personal information (first name, last name, middle initial)
5. Input your GWA (Grade Weighted Average)
6. Specify your location
7. Submit the registration form

### Email Verification

1. After signing up, check your email for a verification link
2. Click the verification link to verify your email address
3. Return to the app and proceed to the dashboard
4. If you don't receive the email, use the "Resend" button (60-second cooldown)

### Browse Scholarships

1. Navigate to the Catalog tab using the bottom navigation
2. View scholarships matched to your GWA and location
3. Tap on any scholarship to view details
4. Follow the application link to apply

### AI Chatbot Assistant

1. Tap the floating chat button (or access via Dashboard)
2. Ask questions about scholarships, application tips, essay writing, or study advice
3. The AI assistant provides guidance on educational topics
4. Chat history is maintained for the current session
5. Maximum 20 messages per conversation; resets automatically

**Note:** The AI assistant focuses on educational and scholarship topics. It will not store personal information and will redirect off-topic questions appropriately.

### Profile Management

1. Navigate to the Profile tab
2. View your account information
3. Update your GWA or location if needed

## Project Structure

```
app/src/main/java/com/example/iskolarphh/
├── MainActivity.java              # Entry point with bottom navigation
├── SignupActivity.java            # Firebase registration with Room persistence
├── LoginActivity.java             # Firebase authentication
├── EmailVerificationActivity.java # Email verification screen
├── api/                           # API layer
│   ├── LongcatApiService.java     # AI chatbot API interface
│   └── RetrofitClient.java        # Retrofit configuration
├── model/                         # Data models for API
│   ├── ChatMessage.java           # Chat message model
│   ├── LongcatRequest.java        # AI API request model
│   └── LongcatResponse.java       # AI API response model
├── repository/                    # Data access layer
│   ├── StudentRepository.java     # Student data operations
│   └── ScholarshipRepository.java # Scholarship data operations
├── database/
│   ├── AppDatabase.java           # Room database singleton
│   ├── DatabaseHelper.java        # Database helper utilities
│   ├── entity/                    # Data models
│   │   ├── Student.java           # User entity
│   │   └── Scholarship.java       # Scholarship entity
│   └── dao/                       # Data Access Objects
│       ├── StudentDao.java
│       └── ScholarshipDao.java
└── ui/                            # Fragment-based UI components
    ├── CatalogFragment.java       # Scholarship listing
    ├── ChatbotDialog.java         # AI chatbot dialog
    ├── DashboardFragment.java     # Home dashboard
    ├── MessageAdapter.java        # Chat message adapter
    └── ProfileFragment.java       # User profile
```

## Contributors

- **James Viray**
- **Jason Villareal**
- **Krizia Mae Oliva**
- **Jayron Mina**
- **Enoch Astor**
- **Andre Victorino**

## Contact

For questions or support, please open an issue on GitHub or contact the development team.

---

Built with ❤️ for Filipino students
