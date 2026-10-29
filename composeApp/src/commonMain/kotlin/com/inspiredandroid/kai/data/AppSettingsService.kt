package com.inspiredandroid.kai.data

import com.inspiredandroid.kai.data.AppSettings.Companion.KEY_CONFIGURED_SERVICES
import com.inspiredandroid.kai.data.AppSettings.Companion.KEY_CURRENT_SERVICE_ID
import com.inspiredandroid.kai.data.AppSettings.Companion.KEY_DEFAULT_SERVICES_SEEDED
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

// Service selection
fun AppSettings.selectService(service: Service) {
    settings.putString(KEY_CURRENT_SERVICE_ID, service.id)
}

fun AppSettings.currentService(): Service {
    val id = settings.getString(KEY_CURRENT_SERVICE_ID, Service.Free.id)
    return Service.fromId(id)
}

// API Keys
fun AppSettings.getApiKey(service: Service): String = if (service.requiresApiKey || service.supportsOptionalApiKey) {
    settings.getString(service.apiKeyKey, "")
} else {
    ""
}

fun AppSettings.setApiKey(service: Service, apiKey: String) {
    if (service.requiresApiKey || service.supportsOptionalApiKey) {
        settings.putString(service.apiKeyKey, apiKey)
    }
}

// Model selection
fun AppSettings.getSelectedModelId(service: Service): String = settings.getString(service.modelIdKey, service.defaultModel ?: "")

// Base URL (for self-hosted services like OpenAI-compatible APIs)
fun AppSettings.getBaseUrl(service: Service): String = when (service) {
    Service.OpenAICompatible -> settings.getString(service.baseUrlKey, Service.DEFAULT_OPENAI_COMPATIBLE_BASE_URL)
    else -> ""
}

fun AppSettings.setBaseUrl(service: Service, baseUrl: String) {
    if (service == Service.OpenAICompatible) {
        settings.putString(service.baseUrlKey, baseUrl)
    }
}

// Configured services (ordered list of service instances)
fun AppSettings.getConfiguredServiceInstances(): List<ServiceInstance> {
    val json = settings.getString(KEY_CONFIGURED_SERVICES, "")
    if (json.isBlank()) return emptyList()
    return try {
        val array = Json.parseToJsonElement(json).jsonArray
        array.map { element ->
            if (element is JsonObject) {
                ServiceInstance(
                    instanceId = element["instanceId"]?.jsonPrimitive?.content ?: "",
                    serviceId = element["serviceId"]?.jsonPrimitive?.content ?: "",
                )
            } else {
                val id = element.jsonPrimitive.content
                ServiceInstance(instanceId = id, serviceId = id)
            }
        }.filter { it.instanceId.isNotBlank() && it.serviceId.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }
}

fun AppSettings.setConfiguredServiceInstances(instances: List<ServiceInstance>) {
    val jsonArray = kotlinx.serialization.json.JsonArray(
        instances.map { instance ->
            JsonObject(
                mapOf(
                    "instanceId" to JsonPrimitive(instance.instanceId),
                    "serviceId" to JsonPrimitive(instance.serviceId),
                ),
            )
        },
    )
    settings.putString(KEY_CONFIGURED_SERVICES, jsonArray.toString())
}

// Per-instance settings (API key, model, base URL)
fun AppSettings.getInstanceApiKey(instanceId: String): String = settings.getString("instance_${instanceId}_api_key", "")

fun AppSettings.setInstanceApiKey(instanceId: String, apiKey: String) {
    settings.putString("instance_${instanceId}_api_key", apiKey)
}

fun AppSettings.getInstanceModelId(instanceId: String): String = settings.getString("instance_${instanceId}_model_id", "")

fun AppSettings.setInstanceModelId(instanceId: String, modelId: String) {
    settings.putString("instance_${instanceId}_model_id", modelId)
}

/** When true, chat uses [getInstanceCustomModelId] instead of the list selection in [getInstanceModelId]. */
fun AppSettings.getInstanceUseCustomModel(instanceId: String): Boolean = settings.getBoolean("instance_${instanceId}_use_custom_model", false)

fun AppSettings.setInstanceUseCustomModel(instanceId: String, useCustom: Boolean) {
    settings.putBoolean("instance_${instanceId}_use_custom_model", useCustom)
}

fun AppSettings.getInstanceCustomModelId(instanceId: String): String = settings.getString("instance_${instanceId}_custom_model_id", "")

fun AppSettings.setInstanceCustomModelId(instanceId: String, modelId: String) {
    settings.putString("instance_${instanceId}_custom_model_id", modelId)
}

/**
 * Model id sent to the provider: custom free-text when enabled, otherwise the list selection.
 * Falls back to empty string when neither is set (caller may apply service defaults).
 */
fun AppSettings.getInstanceEffectiveModelId(instanceId: String): String = if (getInstanceUseCustomModel(instanceId)) {
    getInstanceCustomModelId(instanceId)
} else {
    getInstanceModelId(instanceId)
}

fun AppSettings.getInstanceBaseUrl(instanceId: String): String = settings.getString("instance_${instanceId}_base_url", "")

fun AppSettings.setInstanceBaseUrl(instanceId: String, baseUrl: String) {
    settings.putString("instance_${instanceId}_base_url", baseUrl)
}

fun AppSettings.removeInstanceSettings(instanceId: String) {
    settings.remove("instance_${instanceId}_api_key")
    settings.remove("instance_${instanceId}_model_id")
    settings.remove("instance_${instanceId}_base_url")
    settings.remove("instance_${instanceId}_use_custom_model")
    settings.remove("instance_${instanceId}_custom_model_id")
}

fun AppSettings.generateInstanceId(serviceId: String): String {
    val existing = getConfiguredServiceInstances()
    val existingIds = existing.map { it.instanceId }.toSet()
    if (serviceId !in existingIds) return serviceId
    var counter = 2
    while ("${serviceId}_$counter" in existingIds) counter++
    return "${serviceId}_$counter"
}

/**
 * The providers every install starts with.
 *
 * Both serve free models without an API key — Kilo's gateway rotates free models and OpenCode
 * Zen ships free "stealth" models — so a fresh install can chat before the user configures
 * anything. Kilo comes first: it is the widest free catalogue of the two.
 */
private val DEFAULT_SERVICE_INSTANCES = listOf(
    ServiceInstance(instanceId = Service.OpenRouter.id, serviceId = Service.OpenRouter.id),
)

/**
 * Seeds the default providers on a bare install so chatting works without any setup.
 * OpenRouter serves as the hidden default gateway with the "Auto" free models alias.
 * Runs once per install (guarded by [KEY_DEFAULT_SERVICES_SEEDED]). An
 * install that already has services keeps exactly what it has — the defaults are a starting
 * point, not something that overrides the user's own setup.
 */
fun AppSettings.seedDefaultServicesIfNeeded() {
    if (settings.getBoolean(KEY_DEFAULT_SERVICES_SEEDED, false)) return
    settings.putBoolean(KEY_DEFAULT_SERVICES_SEEDED, true)

    if (getConfiguredServiceInstances().isNotEmpty()) return

    setConfiguredServiceInstances(DEFAULT_SERVICE_INSTANCES)
    // Preselect OpenRouter with "openrouter/auto" alias for free models
    setInstanceModelId(Service.OpenRouter.id, "openrouter/auto")
}
