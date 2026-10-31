# Android update manifest (schema v1)

The stable GitHub Release publishes `update.json` beside the universal FOSS APK.

## Contract

| Field | Meaning |
|---|---|
| `schemaVersion` | Manifest format version; currently `1`. |
| `packageName` | Must equal `com.inspiredandroid.kai`. |
| `versionName` | Human-readable release version, e.g. `2.0.75`. |
| `versionCode` | Authoritative Android upgrade ordering value. |
| `assetName` | Exact universal APK filename attached to this release. |
| `downloadUrl` | HTTPS URL for that exact APK asset. |
| `sha256` | Lowercase SHA-256 digest of the APK bytes. |
| `signerCertificateSha256` | Lowercase SHA-256 digest of the APK signing certificate. |

## Client requirements

- Consume stable releases only; ignore drafts and prereleases.
- Compare `versionCode` with the installed package version code. Never use `versionName` for upgrade ordering.
- Reject an unexpected schema version, package name, asset name, URL origin, malformed digest, or non-increasing version code.
- Verify APK SHA-256 and signing certificate before invoking Android's package installer.
- Treat the manifest as untrusted remote input. HTTPS alone is not an integrity or signer-trust guarantee.
- The production signer certificate fingerprint must be pinned in the client in a later updater phase; do not trust a fingerprint merely because it is supplied by the same remote manifest.
