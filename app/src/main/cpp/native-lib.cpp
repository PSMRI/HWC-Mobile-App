#include <jni.h>
#include <string>
#include <android/log.h>


// JNI functions
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseTmcUrl(JNIEnv *env, jobject thiz) {
    std::string baseTmcUrl = BASE_TMC_URL;
    return env->NewStringUTF(baseTmcUrl.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseAmritUrl(JNIEnv *env, jobject thiz) {
    std::string baseAmritUrl = BASE_AMRIT_URL;
    return env->NewStringUTF(baseAmritUrl.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseFlwUrl(JNIEnv *env, jobject thiz) {
    std::string baseFlwUrl = BASE_FLW_URL;
    return env->NewStringUTF(baseFlwUrl.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseAbhaUrl(JNIEnv *env, jobject thiz) {
    std::string baseAbhaUrl = BASE_ABHA_URL;
    return env->NewStringUTF(baseAbhaUrl.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_sanjeevaniApiUrl(JNIEnv *env, jobject thiz) {
    std::string sanjeevaniApiUrl = SANJEEVANI_API_URL;
    return env->NewStringUTF(sanjeevaniApiUrl.c_str());
}

