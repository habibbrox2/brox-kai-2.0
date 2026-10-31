package com.inspiredandroid.kai.updater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * Verifies APK identity against the certificate of the currently installed application.
 *
 * The release manifest's signer fingerprint is treated only as a claim. The trust anchor
 * is independently obtained from PackageManager for the installed app, preventing a
 * compromised manifest from authorizing an attacker-signed APK.
 */
internal class AndroidApkSignatureVerifier(
    private val context: Context,
) {
    fun installedSignerSha256(): String? {
        val packageInfo = getPackageInfo(context.packageName) ?: return null
        return packageInfo.signerSha256()
    }

    fun verifyDownloadedApk(
        apkFile: File,
        expectedPackageName: String,
        expectedSignerSha256: String,
    ): VerificationResult {
        if (!apkFile.isFile || apkFile.length() <= 0L) {
            return VerificationResult.Invalid("Downloaded APK is missing or empty")
        }
        if (!SHA256.matches(expectedSignerSha256)) {
            return VerificationResult.Invalid("Invalid expected signer fingerprint")
        }

        val installedSigner = installedSignerSha256()
            ?: return VerificationResult.Invalid("Unable to establish installed app signer")
        if (!installedSigner.equals(expectedSignerSha256, ignoreCase = true)) {
            return VerificationResult.Invalid("Release manifest signer does not match installed app")
        }

        val archiveInfo = context.packageManager.getPackageArchiveInfo(
            apkFile.absolutePath,
            signingFlags(),
        ) ?: return VerificationResult.Invalid("Unable to read downloaded APK metadata")

        if (archiveInfo.packageName != expectedPackageName ||
            archiveInfo.packageName != context.packageName
        ) {
            return VerificationResult.Invalid("Downloaded APK package name mismatch")
        }

        val archiveSigner = archiveInfo.signerSha256()
            ?: return VerificationResult.Invalid("Downloaded APK must have exactly one signer")
        if (!archiveSigner.equals(installedSigner, ignoreCase = true)) {
            return VerificationResult.Invalid("Downloaded APK signer does not match installed app")
        }

        return VerificationResult.Valid(archiveSigner)
    }

    private fun getPackageInfo(packageName: String): PackageInfo? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(signingFlags().toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, signingFlags())
        }
    }.getOrNull()

    private fun signingFlags(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
    }

    private fun PackageInfo.signerSha256(): String? {
        val certificates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo?.apkContentsSigners?.map { it.toByteArray() }.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            signatures?.map { it.toByteArray() }.orEmpty()
        }
        if (certificates.size != 1) return null
        return certificates.single().sha256Hex()
    }

    private fun ByteArray.sha256Hex(): String = MessageDigest.getInstance("SHA-256")
        .digest(this)
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    sealed interface VerificationResult {
        data class Valid(val signerSha256: String) : VerificationResult
        data class Invalid(val reason: String) : VerificationResult
    }

    private companion object {
        val SHA256 = Regex("^[a-fA-F0-9]{64}$")
    }
}
