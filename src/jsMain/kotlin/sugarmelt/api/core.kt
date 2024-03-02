package sugarmelt.api

import kotlin.js.Json

fun Exception(exception: ExceptionInfo): Exception {
    return Exception(
        """
        ${if (exception.isError) "Error" else if (exception.isException) "Exception" else ""} ${exception.code}
        ${exception.description}
        ${exception.details.toString()}
        """.trimIndent()
    )
}

external class ExceptionInfo {
    val code: String?
    val description: String?
    val details: Array<Any>?
    val isError: Boolean
    val isException: Boolean
    val value: String?
}

fun <T> jsEval(@Suppress("UNUSED_PARAMETER") o: String): T? = js("eval(o)") as T?
fun jsTypeOf(@Suppress("UNUSED_PARAMETER") o: Any?): String = js("typeof o") as String
fun jsIsUndefined(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    js("typeof o") as String == "undefined"

fun jsIsBoolean(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    js("typeof o") as String == "boolean"

fun jsIsObject(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    (js("o.constructor === ({}).constructor")) as Boolean

fun jsIsArray(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    (js("o.constructor === [].constructor")) as Boolean

fun jsIsMap(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    (js("o.constructor === Map.constructor")) as Boolean

fun jsEntries(@Suppress("UNUSED_PARAMETER") o: dynamic): List<Pair<String, Any?>> =
    (js("Object.entries(o)") as? Array<Array<Any?>>)!!.map { it[0].toString() to it[1] }

fun jsKeys(@Suppress("UNUSED_PARAMETER") o: dynamic): List<String> =
    (js("Object.keys(o)") as? Array<String>)!!.toList()

fun jsValues(@Suppress("UNUSED_PARAMETER") o: dynamic): List<Any?> =
    (js("Object.values(o)") as? Array<Any>)!!.toList()

val Json.entries: List<Pair<String, Any?>>
    get() = jsEntries(this)
val Json.keys: List<String>
    get() = jsKeys(this)
val Json.values: List<Any?>
    get() = jsValues(this)
