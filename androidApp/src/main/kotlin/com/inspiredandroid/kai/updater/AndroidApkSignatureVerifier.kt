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

        val installedSigner = installedSignerSha256()
        val archiveInfo = context.packageManager.getPackageArchiveInfo(
            apkFile.absolutePath,
            signingFlags(),
        )
        val archiveSigner = archiveInfo?.signerSha256()

        return when (
            val validation = AndroidApkIdentityValidation.validate(
                expectedPackageName = expectedPackageName,
                installedPackageName = context.packageName,
                apkPackageName = archiveInfo?.packageName,
                expectedSignerSha256 = expectedSignerSha256,
                installedSignerSha256 = installedSigner,
                apkSignerSha256 = archiveSigner,
            )
        ) {
            is AndroidApkIdentityValidation.Result.Valid ->
                VerificationResult.Valid(validation.signerSha256)
            is AndroidApkIdentityValidation.Result.Invalid ->
                VerificationResult.Invalid(validation.reason)
        }
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
}
