# Cloudflare Workers AI

Brox Kai exposes Cloudflare Workers AI as a provider using Cloudflare's account-scoped API.

## Provider configuration

1. Open **Settings → Services → Cloudflare Workers AI**.
2. Enter the 32-character Cloudflare Account ID in **Cloudflare Account ID**.
3. Add a Cloudflare API token with the **Workers AI Read** permission to discover models and invoke Workers AI.
4. Refresh the model list. Model IDs are retained exactly as returned by Cloudflare (for example, `@cf/...`).

The Account ID is stored in the existing provider base-URL setting for backwards-compatible persistence. It is not a URL. The API token is stored through the existing provider credential mechanism; do not commit it or include it in logs.

## API endpoints

- Chat: `POST https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/v1/chat/completions`
- Model discovery: `GET https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/models/search?page={page}&per_page=100`

Model discovery parses Cloudflare's v4 `result` / `result_info` envelope rather than assuming an OpenAI `data` array. Results are paginated (up to 20 pages per refresh) and de-duplicated by model ID.

## Cloudflare AI Gateway

This provider entry targets **direct Workers AI**. It does not currently configure an AI Gateway ID or route third-party models through AI Gateway. Those are distinct endpoint and routing modes and must not be configured by pasting a gateway ID into the Account ID field.

## Cloudflare MCP

The curated MCP entry points to `https://mcp.cloudflare.com/mcp`. Select it from the popular MCP list and enter a Cloudflare API token in the Authorization field. The token is sent as a Bearer token. Use the minimum Cloudflare permissions required by the MCP tools you intend to enable.

The MCP client supports the optional Skills-over-MCP extension:
- `skills/list` discovers published skill metadata.
- `resources/read` reads a skill document only when its URI uses the `skill://` scheme.

Remote skill metadata and content are untrusted input. Brox Kai must treat them as instructions/data for the model context, never execute embedded scripts automatically. The existing MCP client does not implement Cloudflare OAuth login; configure an API token explicitly.

## Cloudflare Skills marketplace

The Skills browser includes the official `cloudflare/skills` GitHub repository. Skills are fetched from its `skills/` root through the existing GitHub marketplace installer. Review skill contents and any bundled scripts before enabling them.

## Validation

Unit tests cover the Cloudflare response envelope, pagination metadata, invalid response shapes, Account ID validation, and the curated MCP authentication preset.
