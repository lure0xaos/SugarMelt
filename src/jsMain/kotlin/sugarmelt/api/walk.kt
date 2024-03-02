package sugarmelt.api

import sugarmelt.app.SugarMelt.Companion.supportedTypes
import kotlin.js.Json

fun walkVars(vars: Json, action: (String, String, Any?) -> Unit) {
    fun walkVars(path: String, name: String, value: Any?, action: (String, String, Any?) -> Unit): Unit =
        when {
            value == null -> {}
            jsIsArray(value) -> value.unsafeCast<Array<Any?>>().forEachIndexed { index, child ->
                walkVars("$path[$index]", index.toString(), child, action)
            }

            jsIsObject(value) -> value.unsafeCast<Json>().entries.forEach { (key, child) ->
                walkVars("$path['$key']", key, child, action)
            }

            jsIsMap(value) -> value.unsafeCast<Map<String, Any?>>().entries.forEach { (key, child) ->
                walkVars("$path['$key']", key, child, action)
            }

            jsTypeOf(value) in supportedTypes -> action(path, name, value)
            else -> console.warn("unsupported ${jsTypeOf(value)}")
        }
    walkVars("", "", vars, action)
}
