# HWC-Mobile-App

[![DeepWiki](https://img.shields.io/badge/DeepWiki-PSMRI%2FHWC--Mobile--App-blue.svg?...)](https://deepwiki.com/PSMRI/HWC-Mobile-App)

## Overview
The HWC Mobile App is designed for healthcare programs to facilitate collaboration among health workers with different roles such as Registrar, Nurse, Pharmacist, Lab Technician, and Doctor, etc.
This application aims to eliminate pen and paperwork for different roles, allowing them to enter patient data digitally with increased ease and accuracy.


## Functional Description

- **Patient Registration**: Nurses or Registrars can register patients (the same patient can be registered with Esanjivni).
- **Patient Updates**: Doctors, Lab Technicians, and Pharmacists can update the details for registered patients within the app.

## Features

- **User Authentication**: Secure login for different roles like Registrar, Nurse, Doctor, Lab Technician, Pharmacist .
- **Real-time Data**: Access to up-to-date information about patients.
- **User-Friendly Interface**: Intuitive design for easy navigation.
- **Offline Access**: Ability to use the app without an internet connection.
- **Multilingual Support**: Ability to use app in different languages like English, Kannada.

## Technologies & Tools Used

- **IDE**: Android Studio.
- **Database**: Room
- **Languages**: XML, Kotlin, SQL
- **Architecture & Architectural Components**: MVVM, Android Architectural Components
- **SDK**: Android SDK 23-34


## Installation

Make sure you have the following installed:

- [Android Studio](https://developer.android.com/studio)

To run this project, Follow these steps:

1. Clone the repository to your local machine,
   using: `git clone https://github.com/PSMRI/HWC-Mobile-App`.
2. Open Android Studio.
3. Click on 'Open an existing Android Studio project'.
4. Navigate to the directory where you cloned the project and select the root folder.
5. Wait for Android Studio to sync the project and download the dependencies.
6. Once the sync is done, you can run the project on an emulator or a physical device.


## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make your changes.
4. Commit your changes (`git commit -am 'Add some feature'`).
5. Push to the branch (`git push origin feature/your-feature`).
6. Create a new Pull Request.

## Filing Issues

If you encounter any issues, bugs, or have feature requests, please file them in the [main AMRIT repository](https://github.com/PSMRI/AMRIT/issues). Centralizing all feedback helps us streamline improvements and address concerns efficiently.  

## Join Our Community

We’d love to have you join our community discussions and get real-time support!  
Join our [Discord server](https://discord.gg/FVQWsf5ENS) to connect with contributors, ask questions, and stay updated.  


## Credits

This project uses third-party open-source models and resources. Full acknowledgements are provided below to comply with respective licenses and to support reproducibility.

---

### **1. FaceNet (facenet.tflite)**

* Original FaceNet implementation
  **Repository:** [https://github.com/davidsandberg/facenet](https://github.com/davidsandberg/facenet)
  **License:** MIT License
  **Architecture:** Inception-ResNet-v1 trained on CASIA-WebFace
* Android TFLite model source used in this project:
  **Repository:** [https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android](https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android)
* The Facenet `.tflite` in our repo (`app/src/main/assets/facenet.tflite`) was sourced directly from the above repository.
* If any developer wishes to convert a FaceNet `.pb` model to `.tflite`, refer to this conversion script:
  **File:** [`src/train_model/tflite_convert.py`](https://github.com/davidsandberg/facenet/blob/master/src/train_model/tflite_convert.py)

---

### **2. MediaPipe Face Detection (BlazeFace – Short Range)**

* Replaced Google ML Kit with MediaPipe implementation.
* Current face detection model used: **BlazeFace Short-Range (`face_detection_short_range.tflite`)**
* Downloaded from official source:
  [https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector#models](https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector#models)
* Face Detector is part of **Mediapipe Solutions**
  **Repository:** [https://github.com/google/mediapipe](https://github.com/google/mediapipe)
  **License:** Apache 2.0