package com.example.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    
    // Default 256-bit key for zero-config background encryption
    private val defaultKeyBytes = byteArrayOf(
        0x50, 0x72, 0x69, 0x76, 0x61, 0x45, 0x64, 0x69, // PrivaEdi
        0x74, 0x53, 0x65, 0x63, 0x75, 0x72, 0x65, 0x4b, // tSecureK
        0x65, 0x79, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, // ey123456
        0x37, 0x38, 0x39, 0x30, 0x41, 0x42, 0x43, 0x44  // 7890ABCD
    )

    private fun getSecretKeySpec(password: String?): SecretKeySpec {
        if (password.isNullOrEmpty()) {
            return SecretKeySpec(defaultKeyBytes, "AES")
        }
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptBytes(data: ByteArray, password: String? = null): ByteArray {
        return try {
            val keySpec = getSecretKeySpec(password)
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(data)
            iv + encrypted
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    fun decryptBytes(encryptedData: ByteArray, password: String? = null): ByteArray {
        if (encryptedData.size < 16) return ByteArray(0)
        return try {
            val keySpec = getSecretKeySpec(password)
            val iv = encryptedData.sliceArray(0 until 16)
            val encryptedPayload = encryptedData.sliceArray(16 until encryptedData.size)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            cipher.doFinal(encryptedPayload)
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    fun encryptString(data: String, password: String? = null): String {
        if (data.isEmpty()) return ""
        val encrypted = encryptBytes(data.toByteArray(Charsets.UTF_8), password)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptString(encryptedBase64: String, password: String? = null): String {
        if (encryptedBase64.isEmpty()) return ""
        return try {
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val decryptedBytes = decryptBytes(encryptedBytes, password)
            if (decryptedBytes.isEmpty()) "" else String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
