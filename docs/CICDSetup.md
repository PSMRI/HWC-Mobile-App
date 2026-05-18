# HWC Mobile App CI/CD Setup

This repository uses GitHub Actions + Fastlane to build and distribute Android artifacts.

## Overview

- `hwc-android-build.yml` is the manual dispatch workflow.
- `build-distribute.yml` is the reusable build workflow.
- `fastlane/Fastfile` handles the actual build, Firebase distribution, and Play upload.
- `version/version.properties` provides the version name and version code used by Gradle.

## Workflow Flow

1. Select a project in GitHub Actions.
2. Resolve the HWC flavor and environment.
3. Checkout the requested branch.
4. Decode the required secrets.
5. Export native build variables.
6. Run Fastlane.
7. Upload artifacts and distribute the app.

## Project Map

| GitHub Project | Gradle Flavor | Environment | Build Type | Target |
|---|---|---|---|---|
| `niramayStag` | `niramayStag` | `HWC_STAG` | debug | Firebase App Distribution |
| `niramayUat` | `niramayUat` | `HWC_UAT` | debug | Firebase App Distribution |
| `niramay` | `niramay` | `HWC_PRODUCTION` | release | Play Console internal track |

## Required GitHub Configuration

### Environments

Create these GitHub environments:

- `HWC_STAG`
- `HWC_UAT`
- `HWC_PRODUCTION`

### Secrets

Store the following secrets in the appropriate environment or at repository scope:

- `GOOGLE_SERVICES_JSON_GENERIC`
- `FIREBASE_CREDENTIALS_JSON`
- `GOOGLE_PLAY_JSON_KEY`
- `FIREBASE_APP_ID`
- `ABHA_CLIENT_ID`
- `ABHA_CLIENT_SECRET`
- `DEBUG_KEYSTORE_FILE`
- `DEBUG_KEYSTORE_PASSWORD`
- `DEBUG_KEY_ALIAS`
- `DEBUG_KEY_PASSWORD`
- `KEYSTORE_FILE`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

### Variables

Store non-secret runtime config as GitHub variables:

- `BASE_TMC_URL`
- `BASE_AMRIT_URL`
- `BASE_FLW_URL`
- `BASE_ABHA_URL`
- `SANJEEVANI_API_URL`

## Google Cloud / Firebase / Play Console

### Google Cloud Platform

1. Confirm the correct GCP project for HWC.
2. Enable the APIs needed for Firebase App Distribution and Play upload.
3. Create a service account for CI.
4. Grant the service account the minimum roles needed for:
   - Firebase App Distribution uploads
   - Play Console uploads
5. Download the service account JSON and base64-encode it for GitHub secrets.

### Firebase

1. Register the app for each HWC package name that will be distributed.
2. Download the matching `google-services.json` file.
3. Base64-encode it and store it in `GOOGLE_SERVICES_JSON_GENERIC`.
4. Create the Firebase App Distribution service account JSON and store it in `FIREBASE_CREDENTIALS_JSON`.
5. Ensure tester groups in `FirebaseAppDistributionConfig/groups.txt` are correct.

### Play Console

1. Confirm the release package name is registered in Play Console.
2. Link the Play app to the CI service account.
3. Grant upload permission to the service account.
4. Base64-encode the Play service account JSON and store it in `GOOGLE_PLAY_JSON_KEY`.

## Native Build Inputs

The native build reads these environment variables from the workflow:

- `ABHA_CLIENT_ID`
- `ABHA_CLIENT_SECRET`
- `BASE_TMC_URL`
- `BASE_AMRIT_URL`
- `BASE_FLW_URL`
- `BASE_ABHA_URL`
- `SANJEEVANI_API_URL`

`app/src/main/cpp/CMakeLists.txt` injects them into `native-lib.cpp` at compile time.

## Signing

- Debug builds use a shared debug keystore if `debug-keystore.properties` is present.
- Release builds use `keystore.properties`.
- Neither file should be committed.

## Missing Items To Double-Check

- Confirm the exact Firebase app IDs for each package name.
- Confirm whether `GOOGLE_SERVICES_JSON_GENERIC` contains all HWC flavor clients or if you want separate json files per flavor.
- Confirm the Play Console release package name.
- Confirm the debug keystore secrets are available in GitHub.
- Confirm whether production releases should require GitHub environment approval.
