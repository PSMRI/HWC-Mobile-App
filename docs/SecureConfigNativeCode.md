# Secure Config With Native Code

This app keeps sensitive ABHA credentials in native code instead of hardcoding them in Kotlin.

## What Changed

- `AbhaClientConstants.kt` no longer stores the ABHA client ID or client secret directly.
- `KeyUtils.kt` exposes JNI methods that return values from the native library.
- `native-lib.cpp` reads the ABHA values from environment variables injected by CMake.

## Native Inputs

`app/src/main/cpp/CMakeLists.txt` expects these environment variables:

- `ABHA_CLIENT_ID`
- `ABHA_CLIENT_SECRET`
- `BASE_TMC_URL`
- `BASE_AMRIT_URL`
- `BASE_FLW_URL`
- `BASE_ABHA_URL`
- `SANJEEVANI_API_URL`

## Kotlin Access Pattern

`org.piramalswasthya.cho.utils.KeyUtils` loads the `cho` native library and exposes:

- `abhaClientID()`
- `abhaClientSecret()`
- `baseTmcUrl()`
- `baseAmritUrl()`
- `baseFlwUrl()`
- `baseAbhaUrl()`
- `sanjeevaniApiUrl()`

`AbhaClientConstants` now pulls the ABHA secrets through `KeyUtils` so callers do not need to change immediately.

## CI/CD Flow

1. GitHub Actions decodes the required secrets and writes local signing files.
2. The workflow exports native config values as environment variables.
3. Gradle invokes CMake.
4. CMake defines the values as compiler macros.
5. `native-lib.cpp` exposes them through JNI.

## Notes

- Do not commit the generated signing files or downloaded service account JSON files.
- If you add another sensitive runtime value, prefer the same pattern: secret in GitHub, env var in workflow, CMake define, JNI getter in native code.
