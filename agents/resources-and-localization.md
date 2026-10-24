# Compose Resources and Localization Guide

## Resource locations
- Shared Compose Resources are under `composeApp/src/commonMain/composeResources/`.
- The default string resource file is `values/strings.xml`; many localized `values-xx/strings.xml` folders exist.
- Android platform resources are separate under `composeApp/src/androidMain/res/`; do not confuse Android `R.string` resources with generated Compose Resources.

## Adding or repairing a string
1. Search `values/strings.xml` for the intended key and inspect nearby naming conventions.
2. Check whether the key is present in the target locale files and whether translations are expected for this feature.
3. Use the generated Compose Resources API already used by the surrounding code (for example `Res.string.*` and `stringResource`).
4. Do not hand-edit generated build output or invent generated package names.
5. Validate resource XML structure, duplicate names, placeholders/format arguments and capitalization.
6. Compile the affected target; verify the UI text in the intended locale when feasible.

## Translation safety
- Preserve placeholder order and meaning across languages.
- Do not overwrite all locale files with English as a shortcut.
- Avoid introducing hard-coded user-facing strings when the surrounding UI is resource-based.
- When a key is renamed or removed, search all usages and all locale files first.
