package sugarmelt.api

import kotlin.js.Json

fun Json.getJson(propertyName: String): Json? =
    get(propertyName)?.unsafeCast<Json>()

fun <T> Json.getArray(propertyName: String): Array<T>? =
    get(propertyName)?.unsafeCast<Array<T>>()

fun Json.getString(propertyName: String): String =
    get(propertyName).toString()
