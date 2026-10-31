package com.inspiredandroid.kai.updater

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AndroidUpdateDiscoveryTest {
    private fun release(
        tag: String,
        draft: Boolean = false,
        prerelease: Boolean = false,
        includeManifest: Boolean = true,
    ): String {
        val assets = if (includeManifest) {
            """
            "assets": [{
              "name": "update.json",
              "browser_download_url": "https://github.com/habibbrox2/brox-kai-2.0/releases/download/$tag/update.json"
            }]
            """.trimIndent()
        } else {
            "\"assets\": []"
        }
        return """
            {
              "tag_name": "$tag",
              "draft": $draft,
              "prerelease": $prerelease,
              $assets
            }
        """.trimIndent()
    }

    private fun manifest(version: String = "2.0.76", code: Long = 191L): String = """
        {
          "schemaVersion": 1,
          "packageName": "com.inspiredandroid.kai",
          "versionName": "$version",
          "versionCode": $code,
          "assetName": "Brox-Kai-2.0-v$version-Android.apk",
          "downloadUrl": "https://github.com/habibbrox2/brox-kai-2.0/releases/download/v$version/Brox-Kai-2.0-v$version-Android.apk",
          "sha256": "${"a".repeat(64)}",
          "signerCertificateSha256": "${"b".repeat(64)}"
        }
    """.trimIndent()

    @Test
    fun selectsNewestStableReleaseAndValidatesItsManifest() = runTest {
        val api = """
            [
              ${release("v2.0.75")},
              ${release("v2.0.77", prerelease = true)},
              ${release("v2.0.76", draft = true)},
              ${release("v2.0.74")}
            ]
        """.trimIndent()
        val result = AndroidUpdateDiscovery.check(189L) { url ->
            if (url.contains("/releases?")) api else manifest()
        }
        val update = assertIs<AndroidUpdateDiscovery.Result.UpdateAvailable>(result)
        assertEquals("v2.0.75", update.releaseTag)
        assertEquals(191L, update.manifest.versionCode)
    }

    @Test
    fun ignoresDraftAndPrereleaseReleases() = runTest {
        val api = """
            [
              ${release("v2.0.99", prerelease = true)},
              ${release("v2.0.98", draft = true)}
            ]
        """.trimIndent()
        val result = AndroidUpdateDiscovery.check(189L) { api }
        assertEquals(AndroidUpdateDiscovery.Result.UpToDate, result)
    }

    @Test
    fun missingManifestFailsClosed() = runTest {
        val api = "[${release("v2.0.76", includeManifest = false)}]"
        val result = AndroidUpdateDiscovery.check(189L) { api }
        assertIs<AndroidUpdateDiscovery.Result.InvalidRelease>(result)
    }

    @Test
    fun malformedReleaseApiFailsClosed() = runTest {
        val result = AndroidUpdateDiscovery.check(189L) { "not-json" }
        assertIs<AndroidUpdateDiscovery.Result.InvalidRelease>(result)
    }

    @Test
    fun malformedManifestFailsClosed() = runTest {
        val api = "[${release("v2.0.76")}]"
        val result = AndroidUpdateDiscovery.check(189L) { url ->
            if (url.contains("/releases?")) api else "{invalid"
        }
        assertIs<AndroidUpdateDiscovery.Result.InvalidRelease>(result)
    }

    @Test
    fun versionCodePreventsDowngradeAndSameVersionReinstall() = runTest {
        val api = "[${release("v2.0.76")}]"
        val result = AndroidUpdateDiscovery.check(191L) { url ->
            if (url.contains("/releases?")) api else manifest()
        }
        assertEquals(AndroidUpdateDiscovery.Result.UpToDate, result)
    }
}
