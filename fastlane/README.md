# Fastlane

This directory contains the Fastlane automation used by HWC-Mobile-App CI/CD.

## Lanes

- `build_and_distribute_debug`
  - Writes `version/version.properties`
  - Builds the requested debug flavor
  - Uploads the APK to Firebase App Distribution

- `build_and_distribute_release`
  - Writes `version/version.properties`
  - Verifies the version code is higher than existing Play Console versions
  - Builds the requested release flavor
  - Uploads the signed AAB to the Play Console internal track

## Local Requirements

- Ruby
- Bundler
- Android SDK
- Android NDK
- CMake
- Firebase App Distribution service account JSON
- Google Play service account JSON

## Generated Files

The CI workflow generates these files at build time and they should not be committed:

- `firebase_credentials.json`
- `google_play_service_account.json`
- `debug-keystore.jks`
- `debug-keystore.properties`
- `keystore.jks`
- `keystore.properties`
