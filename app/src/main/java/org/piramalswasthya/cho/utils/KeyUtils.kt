package org.piramalswasthya.cho.utils

object KeyUtils {

    private const val NATIVE_JNI_LIB_NAME = "cho"

    init {
        try {
            System.loadLibrary(NATIVE_JNI_LIB_NAME)
        } catch (e: UnsatisfiedLinkError) {
            throw RuntimeException("Failed to load native library: $NATIVE_JNI_LIB_NAME")
        }

    }


    external fun baseTmcUrl(): String

    external fun baseAmritUrl(): String

    external fun baseFlwUrl(): String

    external fun baseAbhaUrl(): String

    external fun sanjeevaniApiUrl(): String




}