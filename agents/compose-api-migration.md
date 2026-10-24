# Compose API and UI Repair Guide

## First principles
- Read the current imports, function body, call sites and compiler message before editing.
- Check the versions in `gradle/libs.versions.toml` and the API actually resolved by this checkout. Compose APIs may differ between AndroidX Compose and Compose Multiplatform artifacts.
- Prefer the smallest source-compatible correction; do not change library versions or introduce dependencies without approval.

## Common error categories
- Missing brace/parenthesis: map the lexical structure and composable nesting; inspect the full file and nearby diff. Do not "fix" by deleting UI blocks or adding braces based only on indentation.
- Unresolved Compose symbol: verify artifact/source-set availability, correct package, and whether the API exists in this version before adding an import.
- Signature mismatch (`TooltipBox`, overscroll APIs, draggable handles, animation APIs, Autofill): compare the exact overload and opt-in requirements for the resolved version; adapt the call site while preserving behavior.
- State/focus/keyboard errors: preserve state ownership, focus lifecycle, key handling and accessibility semantics; avoid repeated focus requests that cause loops.
- Wasm-only failure: verify browser target support and transitive dependencies rather than moving JVM code into common code.

## Safe repair sequence
1. Capture the first compiler error and exact file/line.
2. Inspect the full file, adjacent types/imports, git diff, relevant version catalog entry and target source set.
3. Search for working call sites elsewhere in the repository.
4. Form a root-cause hypothesis and minimal patch plan.
5. After authorization, patch one error class at a time.
6. Compile the affected target and run related tests; inspect the final diff for behavior loss.

## UI regression checks
- Verify selection, dismissal, keyboard Escape behavior, focus, scrolling, empty/loading states, and interaction semantics when relevant.
- Keep existing Material 3 theme tokens and navy/cyan branding.
- Do not remove accessibility labels, semantics, tooltips or keyboard navigation to make compilation pass.
