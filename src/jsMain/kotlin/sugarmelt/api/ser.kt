package sugarmelt.api

import kotlinx.serialization.json.Json

val json: Json = Json

inline fun <reified T> decode(data: String): T =
    runCatching { json.decodeFromString<T>(data) }
        .onFailure { alertError("decoding error $data") }
        .getOrThrow()


inline fun <reified T> encode(data: T): String =
    runCatching { json.encodeToString(data) }
        .onFailure { alertError("encoding error $data") }
        .recover { JSON.stringify(data) }
        .getOrThrow()