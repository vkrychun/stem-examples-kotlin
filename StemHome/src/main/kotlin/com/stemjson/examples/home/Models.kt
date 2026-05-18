package com.stemjson.examples.home

/**
 * Native projection of a device row inside the StemJSON module's state.
 */
internal data class Device(
    val id: String,
    val name: String,
    val room: String,
    val kind: String,
    val icon: String,
    val on: Boolean,
)

/** Native projection of one entry in the module's `recentEvents` array. */
internal data class ActivityEntry(
    val id: String,
    val deviceName: String,
    val action: String,
    val timestamp: Double,
)

/**
 * `runtime.subscribe` hands the host raw Kotlin values: `null`, primitive
 * types, `List<Any?>`, `Map<String, Any?>`. Native code parses them back
 * into typed data classes with a guarded `as?` cascade.
 */
@Suppress("UNCHECKED_CAST")
internal fun parseDevices(raw: Any?): List<Device> {
    val arr = raw as? List<Map<String, Any?>> ?: return emptyList()
    return arr.mapNotNull { dict ->
        Device(
            id = dict["id"] as? String ?: return@mapNotNull null,
            name = dict["name"] as? String ?: return@mapNotNull null,
            room = dict["room"] as? String ?: return@mapNotNull null,
            kind = dict["kind"] as? String ?: return@mapNotNull null,
            icon = dict["icon"] as? String ?: return@mapNotNull null,
            on = dict["on"] as? Boolean ?: return@mapNotNull null,
        )
    }
}

@Suppress("UNCHECKED_CAST")
internal fun parseActivity(raw: Any?): List<ActivityEntry> {
    val arr = raw as? List<Map<String, Any?>> ?: return emptyList()
    return arr.mapNotNull { dict ->
        ActivityEntry(
            id = dict["id"] as? String ?: return@mapNotNull null,
            deviceName = dict["deviceName"] as? String ?: return@mapNotNull null,
            action = dict["action"] as? String ?: return@mapNotNull null,
            timestamp = (dict["timestamp"] as? Number)?.toDouble() ?: 0.0,
        )
    }
}
