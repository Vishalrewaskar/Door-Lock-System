# 🔐 Door Lock System using IoT and Android

```{=html}
<p align="center">
```
`<img src="assets/app/splash.png" width="180" alt="Splash Screen">`{=html}
```{=html}
</p>
```
```{=html}
<p align="center">
```
An IoT-based smart door lock system that combines an Android
application, Firebase Realtime Database, Bluetooth communication, and an
Arduino Uno to securely lock and unlock a door.
```{=html}
</p>
```

------------------------------------------------------------------------

## 📌 Overview

This project demonstrates a smart access control solution where an
Android application communicates with an Arduino-powered door lock
through an HC-05 Bluetooth module. Firebase is used for user and lock
management, while the app provides a modern interface for operating
locks and viewing activity logs.

> **AI-Assisted Development**\
> This project was built with the assistance of **Claude** and
> **ChatGPT** for development, debugging, and documentation.

------------------------------------------------------------------------

## ✨ Features

-   Secure lock & unlock control
-   Android application interface
-   Firebase Realtime Database integration
-   Bluetooth communication (HC-05)
-   Lock activity logs
-   User management
-   Navigation drawer
-   Real-time status updates
-   Arduino-based hardware prototype

------------------------------------------------------------------------

## 📱 Application Screenshots

> Create an `assets/app/` folder and place the screenshots using the
> following names.

  Splash                       Home
  ---------------------------- --------------------------
  ![](assets/app/splash.png)   ![](assets/app/home.png)

  Lock Control                       Logs
  ---------------------------------- --------------------------
  ![](assets/app/lock-control.png)   ![](assets/app/logs.png)

  Users                       Navigation
  --------------------------- --------------------------------
  ![](assets/app/users.png)   ![](assets/app/navigation.png)

------------------------------------------------------------------------

## 🔧 Hardware Prototype

Place the hardware images inside:

``` text
assets/hardware/
```

Example:

``` text
assets/
├── app/
└── hardware/
    ├── prototype.jpg
    └── wiring.jpg
```

------------------------------------------------------------------------

## 🛠️ Tech Stack

  Category         Technology
  ---------------- ----------------------------
  Language         Java
  IDE              Android Studio
  Database         Firebase Realtime Database
  Authentication   Firebase Authentication
  Hardware         Arduino Uno
  Communication    HC-05 Bluetooth
  Actuator         Servo Motor

------------------------------------------------------------------------

## 📦 Hardware Components

-   Arduino Uno
-   HC-05 Bluetooth Module
-   SG90 Servo Motor
-   Jumper Wires
-   USB Cable

------------------------------------------------------------------------

## 📂 Project Structure

``` text
Door-Lock-System/
├── Android Application
├── Arduino Code
├── Firebase Configuration
├── Gradle Files
└── Documentation
```

------------------------------------------------------------------------

## 🚀 Getting Started

### Prerequisites

-   Android Studio
-   Arduino IDE
-   Firebase Project
-   Android Device with Bluetooth

### Installation

1.  Clone the repository

``` bash
git clone https://github.com/Vishalrewaskar/Door-Lock-System.git
```

2.  Open the Android project in Android Studio.

3.  Configure Firebase.

4.  Upload the Arduino sketch to the Arduino Uno.

5.  Pair the Android device with the HC-05 module.

6.  Build and run the application.

------------------------------------------------------------------------

## ⚙️ System Workflow

``` text
Android App
      │
      ▼
Firebase Realtime Database
      │
      ▼
Bluetooth (HC-05)
      │
      ▼
Arduino Uno
      │
      ▼
Servo Motor
      │
      ▼
Door Lock / Unlock
```

------------------------------------------------------------------------

## 🔮 Future Improvements

-   Wi-Fi based control
-   RFID authentication
-   Fingerprint integration
-   Push notifications
-   Role-based access control
-   Offline synchronization

------------------------------------------------------------------------

## 🤝 Contributing

Contributions, issues, and suggestions are welcome.

------------------------------------------------------------------------

## 👨‍💻 Author

**Vishal Rewaskar**

GitHub: https://github.com/Vishalrewaskar

Repository:

https://github.com/Vishalrewaskar/Door-Lock-System

------------------------------------------------------------------------

## ⭐ Support

If you found this project useful, consider giving it a ⭐ on GitHub.
