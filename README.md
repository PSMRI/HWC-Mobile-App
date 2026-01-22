# HWC-Mobile-App

[![DeepWiki](https://img.shields.io/badge/DeepWiki-PSMRI%2FHWC--Mobile--App-blue.svg?...)](https://deepwiki.com/PSMRI/HWC-Mobile-App)

## Overview

The **HWC Mobile App** is designed for healthcare programs to facilitate collaboration among health workers with different roles such as **Registrar, Nurse, Pharmacist, Lab Technician, and Doctor**.

The application eliminates pen-and-paper workflows by enabling digital patient data entry with improved accuracy, traceability, and operational efficiency.

## Functional Description

- **Patient Registration**
  Registrars or Nurses can register patients in the system (including Esanjivani-linked registrations where applicable).

- **Patient Updates**
  **Doctors** update clinical details
- **Lab Technicians** update laboratory results
- **Pharmacists** manage medication and dispensing details

---

## Features

- **Role-Based Authentication**: Secure login for Registrar, Nurse, Doctor, Lab Technician, and Pharmacist roles.
- **Real-Time Patient Data**: Access to the latest patient information across roles.
- **User-Friendly Interface**: Intuitive UI for fast navigation in clinical environments.
- **Offline Support**: Core workflows function without internet connectivity, with sync when online.
- **Multilingual Support**: Currently supports **English** and **Kannada**.

---

## Technologies & Tools Used

**IDE**: Android Studio **Otter Feature Drop (2025.2.3) or later**

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Local Storage**: Room Database
- **Networking**: Retrofit
- **Background Tasks**: WorkManager
- **Authentication**: Firebase Auth
- **Machine Learning**: TensorFlow Lite (FaceNet)
- **Version Control**: Git & GitHub

---

## Build & Environment Requirements

To successfully build and run this project, ensure the following versions:

**Android Studio**: Otter Feature Drop (2025.2.3) or later

- **Android Gradle Plugin (AGP)**: 9.0.0
- **Gradle**: 9.3.0
- **Kotlin**: 2.3.0
- **Compile SDK**: 35
- **Target SDK**: 35
- **Min SDK**: 24
- **Java (JDK)**: 17

> ⚠️ **Important Setup Notes**

<!-- > -->

> - Use **JDK 17 only**. Java 21 is **not supported** and will cause Gradle build failures.
> - This project uses **Kotlin 2.3.0**. Do **not** override Kotlin versions manually unless you fully understand AGP compatibility.
> - Add a valid `google-services.json` file inside the `app/` directory before running the project.
> - Jetpack Compose compiler is bundled with Kotlin 2.x. Refer to the official Compose–Kotlin compatibility map if upgrading.

> ⚠️ **Note**: Kotlin and Jetpack Compose versions must remain compatible. Refer to the official Compose–Kotlin compatibility map if upgrading.

---

## Installation & Setup

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Android SDK Platform 35

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/PSMRI/HWC-Mobile-App
   ```

2. **Open Android Studio**

   - Select **Open an existing Android Studio project**
   - Choose the cloned root directory

3. **Add Firebase configuration**

   - app/google-services.json

4. **Sync Gradle**

   - Allow Android Studio to download all dependencies

5. **Run the app**

   - Use an emulator or a physical Android device (Android 7.0+ recommended)

---

## Contributing

Contributions are welcome and appreciated.

1. Fork the repository
2. Create a new branch

   ```bash
   git checkout -b feature/your-feature
   ```

3. Make your changes
4. Commit your changes

   ```bash
   git commit -m "Add your feature"
   ```

5. Push to your branch

   ```bash
   git push origin feature/your-feature
   ```

6. Create a Pull Request

---

## Filing Issues

If you encounter bugs or have feature requests, please file them in the **main AMRIT repository**:

👉 [https://github.com/PSMRI/AMRIT/issues](https://github.com/PSMRI/AMRIT/issues)

Centralizing feedback helps streamline triage and resolution.

---

## Join Our Community

Join our community for discussions, support, and updates:

👉 **Discord**: [https://discord.gg/FVQWsf5ENS](https://discord.gg/FVQWsf5ENS)

---

## Credits & Open-Source Acknowledgements

This project uses third-party open-source models and libraries. Full acknowledgements are listed below in compliance with respective licenses.

---

### 1. FaceNet (facenet.tflite)

- **Original Implementation**
  Repository: [https://github.com/davidsandberg/facenet](https://github.com/davidsandberg/facenet)
  License: MIT License
  Architecture: Inception-ResNet-v1 trained on CASIA-WebFace

- **Android TFLite Source Used**
  Repository: [https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android](https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android)

- The `facenet.tflite` model located at:

```text
app/src/main/assets/facenet.tflite
```

was sourced directly from the above repository.

- **Model Conversion Reference**
  Script: [https://github.com/davidsandberg/facenet/blob/master/src/train_model/tflite_convert.py](https://github.com/davidsandberg/facenet/blob/master/src/train_model/tflite_convert.py)

---

### 2. MediaPipe Face Detection (BlazeFace – Short Range)

- Replaced Google ML Kit with MediaPipe Face Detection

- Model Used: **BlazeFace Short-Range (`face_detection_short_range.tflite`)**

- Official Source:
  [https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector#models](https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector#models)

- MediaPipe Repository:
  [https://github.com/google/mediapipe](https://github.com/google/mediapipe)

- License: **Apache 2.0**

---

## License

This project follows the licensing terms of its included third-party dependencies. Refer to individual libraries for detailed license information.
