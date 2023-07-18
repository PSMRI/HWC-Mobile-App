package org.piramalswasthya.cho.crypt


import android.util.Base64
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoUtil {

    private val keySize = 256
    private val ivSize = 128
    private val iterationCount = 1989
    private val passPhrase = "Piramal12Piramal"

    private fun generateKey(
        salt: String,
    ): ByteArray {
        val saltBytes = hexStringToByteArray(salt)
        val keySpec = PBEKeySpec(passPhrase.toCharArray(), saltBytes, iterationCount, keySize)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val secretKey = secretKeyFactory.generateSecret(keySpec)
        return secretKey.encoded
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun encryptWithIvSalt(
        salt: String, iv: String, plainText: String
    ): String {
        val key = generateKey(salt)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(hexStringToByteArray(iv))

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray())

        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }

    fun encrypt(plainText: String): String {
        val ivarr = ByteArray(ivSize / 8)
        Random().nextBytes(ivarr)
        val iv = ivarr.toHexString()

        val saltArr = ByteArray(keySize / 8)
        Random().nextBytes(saltArr)
        val salt = saltArr.toHexString()

        val ciphertext = encryptWithIvSalt(salt, iv, plainText)

        return salt + iv + ciphertext
    }


    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val byteArray = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val hex = hexString.substring(i, i + 2)
            byteArray[i / 2] = hex.toInt(16).toByte()
            i += 2
        }
        return byteArray
    }
}