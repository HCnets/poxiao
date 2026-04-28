package com.poxiao.app.security

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val SecureAlias = "poxiao_local_secure_key"
private const val SecurePrefix = "enc_v1:"

object SecurePrefs {
    fun getString(
        prefs: SharedPreferences,
        key: String,
        fallbackKey: String? = null,
    ): String {
        val encrypted = prefs.getString(key, null).orEmpty()
        if (encrypted.startsWith(SecurePrefix)) {
            return decrypt(encrypted.removePrefix(SecurePrefix)).orEmpty()
        }
        val fallback = fallbackKey?.let { prefs.getString(it, null).orEmpty() }.orEmpty()
        if (fallback.isNotBlank()) {
            putString(prefs, key, fallback)
            prefs.edit().remove(fallbackKey).apply()
            return fallback
        }
        return encrypted
    }

    fun putString(
        prefs: SharedPreferences,
        key: String,
        value: String,
    ) {
        if (value.isBlank()) {
            prefs.edit().remove(key).apply()
            return
        }
        val encrypted = encrypt(value)
        if (encrypted != null) {
            prefs.edit().putString(key, SecurePrefix + encrypted).apply()
        } else {
            prefs.edit().putString(key, value).apply()
        }
    }

    fun remove(
        prefs: SharedPreferences,
        key: String,
        fallbackKey: String? = null,
    ) {
        val editor = prefs.edit().remove(key)
        if (fallbackKey != null) editor.remove(fallbackKey)
        editor.apply()
    }

    private fun encrypt(value: String): String? {
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv
            val payload = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(iv + payload, Base64.NO_WRAP)
        }.getOrNull()
    }

    private fun decrypt(value: String): String? {
        return runCatching {
            val decoded = Base64.decode(value, Base64.NO_WRAP)
            val iv = decoded.copyOfRange(0, 12)
            val payload = decoded.copyOfRange(12, decoded.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(payload), StandardCharsets.UTF_8)
        }.getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(SecureAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            SecureAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }
}
