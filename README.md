# ISKOLARPH

![GitHub stars](https://img.shields.io/github/stars/Enichh/ISKOLARPH?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/Enichh/ISKOLARPH?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues/Enichh/ISKOLARPH?style=for-the-badge)
![GitHub license](https://img.shields.io/github/license/Enichh/ISKOLARPH?style=for-the-badge)

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
- **Material Design 3**: Modern, intuitive UI with dark mode support
- **Offline Data Storage**: Room Database for local data persistence
- **Real-time Updates**: LiveData for reactive UI updates

## Tech Stack

- **Language**: Java 17
- **Platform**: Android (minSdk: 24, targetSdk: 36)
- **Architecture**: MVVM pattern with Repository pattern
- **Database**: Room (SQLite) with LiveData
- **Authentication**: Firebase Auth with Email Verification
- **UI**: Material Design 3 with Bottom Navigation
- **Build System**: Gradle with Kotlin DSL
- **Firebase**: BOM 32.7.0, Firebase Auth

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

### 3. Firebase Setup

1. Create a Firebase project at [https://console.firebase.google.com/](https://console.firebase.google.com/)
2. Add an Android app with package name: `com.example.iskolarphh`
3. Download `google-services.json` and place it in the `app/` directory
4. Enable Firebase Authentication in the Firebase Console
5. Enable Email/Password sign-in method

### 4. Build and Run

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
    ├── DashboardFragment.java     # Home dashboard
    └── ProfileFragment.java       # User profile
```

## Contributing

We welcome contributions! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Contributors

### Main Authors
- **James Viray**
- **Jason Villareal**
- **Krizia Mae Oliva**

### Contributors
- **Jayron Mina**
- **Enoch Astor**
- **Andre Victorino**

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Firebase](https://firebase.google.com/) for authentication services
- [Android Developers](https://developer.android.com/) for comprehensive documentation
- [Material Design](https://m3.material.io/) for design guidelines
- [Room Persistence Library](https://developer.android.com/training/data-storage/room) for database solution

## Contact

For questions or support, please open an issue on GitHub or contact the development team.

---

Built with ❤️ for Filipino students
