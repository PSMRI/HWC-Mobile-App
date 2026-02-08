# HWC Mobile App

## Overview

The **HWC Mobile App** is designed for healthcare programs to facilitate collaboration among health workers with different roles such as **Registrar, Nurse, Pharmacist, Lab Technician, and Doctor**.

The application eliminates pen-and-paper workflows by enabling digital patient data entry with improved accuracy, traceability, and operational efficiency.

---

## Functional Description

- **Patient Registration**
  Registrars or Nurses can register patients in the system, including Esanjivani-linked registrations where applicable.

- **Patient Updates**

  - **Doctors** update clinical details
  - **Lab Technicians** update laboratory results
  - **Pharmacists** manage medication and dispensing details

---

## Features

- **Role-Based Authentication**: Secure login for Registrar, Nurse, Doctor, Lab Technician, and Pharmacist roles.
- **Real-Time Patient Data**: Access to the latest patient information across roles.
- **User-Friendly Interface**: Intuitive UI for fast navigation in clinical environments.
- **Offline Support**: Core workflows function without internet connectivity, with background sync when online.
- **Multilingual Support**: Currently supports **English** and **Kannada**.

---

## Technologies & Tools Used

- **IDE**: Android Studio (Otter Feature Drop 2025.2.3 or later)
- **Language**: Kotlin
- **UI Framework**: XML & Jetpack Compose
- **Architecture**: MVVM, Android Architecture Components
- **Local Storage**: Room Database
- **Networking**: Retrofit
- **Background Tasks**: WorkManager
- **Authentication**: Firebase Auth
- **Machine Learning**: TensorFlow Lite (FaceNet), MediaPipe
- **Version Control**: Git & GitHub

---

## Build & Environment Requirements

- **Compile SDK**: 35
- **Target SDK**: 35
- **Min SDK**: 24
- **Android Gradle Plugin (AGP)**: 9.0.0
- **Gradle**: 9.3.0
- **Kotlin**: 2.3.0
- **Java (JDK)**: 17

> ⚠️ **Important Setup Notes**
>
> - Use **JDK 17 only**. Java 21 is **not supported** and will cause Gradle build failures.
> - Ensure the Kotlin version remains compatible with Jetpack Compose.
> - Add a valid `google-services.json` file inside the `app/` directory before running the project.
> - Kotlin and Jetpack Compose versions must remain compatible. Refer to the official Compose–Kotlin compatibility map if upgrading.

---


## Installation & Setup

### Prerequisites

- Android Studio
- Android SDK Platform 35

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/PSMRI/HWC-Mobile-App
   ```

2. **Open the project**

   - Open Android Studio
   - Select **Open an existing Android Studio project**
   - Choose the cloned root directory

3. **Add Firebase configuration**

   - Place `google-services.json` inside the `app/` directory

4. **Sync Gradle**

   - Allow Android Studio to download dependencies and complete Gradle sync

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
   git commit -m "Describe your change"
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

### 1. FaceNet (facenet.tflite)

- **Original Repository**: [https://github.com/davidsandberg/facenet](https://github.com/davidsandberg/facenet)
- **License**: MIT License
- **Architecture**: Inception-ResNet-v1 trained on CASIA-WebFace
- **Android TFLite Source Used**: [https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android](https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android)
- The `facenet.tflite` model is located at:

  ```text
  app/src/main/assets/facenet.tflite
  ```

- **Model Conversion Reference**: [https://github.com/davidsandberg/facenet/blob/master/src/train_model/tflite_convert.py](https://github.com/davidsandberg/facenet/blob/master/src/train_model/tflite_convert.py)

---

### 2. MediaPipe Face Detection (BlazeFace – Short Range)

- Model Used: **BlazeFace Short-Range (`face_detection_short_range.tflite`)**
- Official Source: [https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector#models](https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector#models)
- Repository: [https://github.com/google/mediapipe](https://github.com/google/mediapipe)
- License: Apache 2.0

---

## License

This project follows the licensing terms of its included third-party dependencies. Refer to individual libraries for detailed license information.
