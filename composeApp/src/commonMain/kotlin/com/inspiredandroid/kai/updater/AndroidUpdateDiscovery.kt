package com.inspiredandroid.kai.updater

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Discovers the newest stable Android release and validates its published update manifest.
 *
 * The manifest is fetched only from the matching GitHub release asset. Its APK URL, package,
 * version/tag relationship, checksums and signer fingerprint are validated before it is returned.
 */
internal object AndroidUpdateDiscovery {
    private const val RELEASES_URL =
        "https://api.github.com/repos/habibbrox2/brox-kai-2.0/releases?per_page=100"
    private const val TAG_PREFIX = "v2.0."
    private const val MANIFEST_ASSET = "update.json"

    private val releaseJson = Json { ignoreUnknownKeys = true }

    @Serializable
    internal data class ReleaseAsset(
        val name: String,
        val browser_download_url: String,
    )

    @Serializable
    internal data class GitHubRelease(
        val tag_name: String,
        val draft: Boolean,
        val prerelease: Boolean,
        val assets: List<ReleaseAsset>,
    )

    sealed interface Result {
        data object UpToDate : Result
        data class UpdateAvailable(val manifest: AndroidUpdateManifest, val releaseTag: String) : Result
        data class InvalidRelease(val reason: String) : Result
    }

    suspend fun check(
        installedVersionCode: Long,
        fetchText: suspend (String) -> String = { url ->
            httpGet(
                url,
                mapOf(
                    "Accept" to "application/vnd.github+json",
                    "User-Agent" to "BroxKai-Android-Updater/1.0",
                ),
            )
        },
    ): Result {
        if (installedVersionCode <= 0L) return Result.InvalidRelease("Invalid installed version code")
        val releases = try {
            releaseJson.decodeFromString<List<GitHubRelease>>(fetchText(RELEASES_URL))
        } catch (_: Exception) {
            return Result.InvalidRelease("Unable to read GitHub releases")
        }
        val stable = releases
            .filter { !it.draft && !it.prerelease && it.tag_name.matches(Regex("^v2\\.0\\.[0-9]+$")) }
            .maxByOrNull { it.tag_name.removePrefix("v").substringAfterLast('.').toLongOrNull() ?: -1L }
            ?: return Result.UpToDate

        val manifestAsset = stable.assets.singleOrNull { it.name == MANIFEST_ASSET }
            ?: return Result.InvalidRelease("Stable release is missing a unique update.json asset")
        if (manifestAsset.browser_download_url !=
            "https://github.com/habibbrox2/brox-kai-2.0/releases/download/${stable.tag_name}/update.json"
        ) {
            return Result.InvalidRelease("Unexpected update manifest URL")
        }

        val manifest = try {
            AndroidUpdateManifest.parse(fetchText(manifestAsset.browser_download_url))
        } catch (_: Exception) {
            return Result.InvalidRelease("Unable to parse update manifest")
        }
        when (val validation = manifest.validateForRelease(stable.tag_name)) {
            ManifestValidation.Valid -> Unit
            is ManifestValidation.Invalid -> return Result.InvalidRelease(validation.reason)
        }
        return if (manifest.isUpgradeFrom(installedVersionCode)) {
            Result.UpdateAvailable(manifest, stable.tag_name)
        } else {
            Result.UpToDate
        }
    }
}
