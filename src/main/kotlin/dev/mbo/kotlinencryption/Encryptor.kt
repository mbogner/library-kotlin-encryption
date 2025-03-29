package dev.mbo.kotlinencryption

import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Encryptor(
    private val encryptionKey: String
) {

    private val saltLength = 16
    private val ivLength = 12 // 12 bytes is the standard IV size for GCM
    private val iterationCount = 65536
    private val keyLength = 256
    private val algorithm = "AES/GCM/NoPadding"
    private val gcmTagLength = 128 // in bits

    fun encrypt(plainText: String): String {
        val salt = ByteArray(saltLength).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(ivLength).also { SecureRandom().nextBytes(it) }

        val key = deriveKey(encryptionKey, salt)
        val cipher = Cipher.getInstance(algorithm)
        val gcmSpec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine salt + iv + cipherText and base64-encode
        val combined = salt + iv + cipherText
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.getDecoder().decode(encryptedText)

        val salt = combined.sliceArray(0 until saltLength)
        val iv = combined.sliceArray(saltLength until saltLength + ivLength)
        val cipherText = combined.sliceArray(saltLength + ivLength until combined.size)

        val key = deriveKey(encryptionKey, salt)
        val cipher = Cipher.getInstance(algorithm)
        val gcmSpec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }
}