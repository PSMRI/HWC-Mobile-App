#include <jni.h>
#include <string>
#include <android/log.h>
#define LOG_TAG "JNI_KEYS"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


// ================== Development Constants ================== //
// #if IS_DEVELOPMENT
const char* M_ENCRYPTED_PASS_KEY = "ENCRYPTED_PASS_KEY";
const char* M_ABHA_CLIENT_SECRET = "ABHA_CLIENT_SECRET";
const char* M_ABHA_CLIENT_ID = "ABHA_CLIENT_ID";
const char* M_BASE_TMC_URL = "BASE_TMC_URL";
const char* M_BASE_AMRIT_URL = "BASE_AMRIT_URL";
const char* M_BASE_FLW_URL = "BASE_FLW_URL";
const char* M_BASE_ABHA_URL = "BASE_ABHA_URL";
const char* M_ABHA_TOKEN_URL = "ABHA_TOKEN_URL";
const char* M_ABHA_AUTH_URL = "ABHA_AUTH_URL";
const char* M_SANJEEVANI_API_URL = "SANJEEVANI_API_URL";
// ================== Production Constants (from Environment) ================== //
// #else
// const char* M_ENCRYPTED_PASS_KEY = ENCRYPTED_PASS_KEY;
// const char* M_ABHA_CLIENT_SECRET = ABHA_CLIENT_SECRET;
// const char* M_ABHA_CLIENT_ID = ABHA_CLIENT_ID;
// const char* M_BASE_TMC_URL = BASE_TMC_URL;
// const char* M_BASE_ABHA_URL = BASE_ABHA_URL;
// const char* M_ABHA_TOKEN_URL = ABHA_TOKEN_URL;
// const char* M_ABHA_AUTH_URL = ABHA_AUTH_URL;
// const char* M_CHAT_URL = CHAT_URL;
// #endif

// =================================================================== //

// JNI functions
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_encryptedPassKey(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ENCRYPTED_PASS_KEY);
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_abhaClientSecret(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_CLIENT_SECRET);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_abhaClientID(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_CLIENT_ID);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_baseTMCUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_BASE_TMC_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_baseAmritUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_BASE_AMRIT_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_baseFlwUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_BASE_FLW_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_baseAbhaUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_BASE_ABHA_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_abhaTokenUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_TOKEN_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_abhaAuthUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_AUTH_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_cho_utils_KeyUtils_sanjeevaniApiUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_SANJEEVANI_API_URL);
}