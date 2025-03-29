package dev.mbo.kotlinencryption

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.GeneralSecurityException
import java.util.UUID

class EncryptorTest {

    private val sharedInstance = Encryptor("fd286d47-eefe-416b-8c28-3a64e013e9e9")
    private val strOriginal = "This is a test!"
    private val strEncrypted = "RYDc8oKd8NaG5orH/tWDzWEcA92VFSxhrPI3tkelCPYQDG4njuebn4nvKOT0V55kl94oVftfAiXqdS8="

    @Test
    fun encrypt() {
        // two instances with different passwords
        val instanceA = Encryptor(UUID.randomUUID().toString())
        val instanceB = Encryptor(UUID.randomUUID().toString())

        val str = UUID.randomUUID().toString() // some random string

        val encryptedByA = instanceA.encrypt(str)
        val sameEncryptedByA = instanceA.encrypt(str)
        // encrypt doesn't result in the same string with same input when running it again
        assertThat(encryptedByA).isNotEqualTo(sameEncryptedByA)
        // make sure we can decrypt both
        instanceA.decrypt(encryptedByA)
        instanceA.decrypt(sameEncryptedByA)

        // try second instance
        val encryptedByB = instanceB.encrypt(str)
        assertThat(encryptedByA).isNotEqualTo(encryptedByB)
        instanceB.decrypt(encryptedByB)

        // make sure they can't decrypt vice versa
        Assertions.assertThrows(GeneralSecurityException::class.java) {
            instanceA.decrypt(encryptedByB)
        }
        Assertions.assertThrows(GeneralSecurityException::class.java) {
            instanceB.decrypt(encryptedByA)
        }

    }

    @Test
    fun decrypt() {
        assertThat(sharedInstance.decrypt(strEncrypted)).isEqualTo(strOriginal)
    }

    @Test
    fun decryptWithWrongPassword() {
        Assertions.assertThrows(GeneralSecurityException::class.java) {
            assertThat(Encryptor(UUID.randomUUID().toString()).decrypt(strEncrypted))
                .isNotEqualTo(strOriginal)
        }
    }

}