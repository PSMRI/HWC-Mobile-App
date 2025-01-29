package org.piramalswasthya.cho.utils

import timber.log.Timber

/**
 * Security utility for retrieving sensitive configuration values through native code.
 * This class provides a secure interface to access sensitive information such as keys,
 * secrets, and URLs by leveraging native code to prevent reverse engineering.
 *
 * Note: This class requires the native library 'cho' to be properly configured
 * and built using CMake. Ensure the native implementation follows security best
 * practices for handling sensitive data.
 */
object KeyUtils {

    private const val NATIVE_JNI_LIB_NAME = "cho"

    init {
        try {
            System.loadLibrary(NATIVE_JNI_LIB_NAME)
        } catch (e: UnsatisfiedLinkError) {
            Timber.tag("KeyUtils").e(e, "Failed to load native library")
            throw RuntimeException("Failed to load native library: $NATIVE_JNI_LIB_NAME")
        }

    }


    external fun baseTmcUrl(): String

    external fun baseAmritUrl(): String

    external fun baseFlwUrl(): String

    external fun baseAbhaUrl(): String

    external fun sanjeevaniApiUrl(): String




}