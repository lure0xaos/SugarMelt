package sugarmelt.api

import kotlinx.browser.window

private fun toJsString(vararg info: Any?) = info.joinToString { if (info.isEmpty()) "" else JSON.stringify(info) }

fun alert(message: String, vararg info: Any?): Unit =
    window.alert("$message ${toJsString(info)}")

fun confirm(message: String, vararg info: Any?): Boolean =
    window.confirm("$message ${toJsString(info)}")

fun prompt(message: String, default: String? = null, vararg info: Any?): String? =
    if (default == null)
        window.prompt("$message ${toJsString(info)}")
    else
        window.prompt("$message ${toJsString(info)}", default)

fun alertError(e: Throwable? = null, message: String, vararg info: Any?): Unit =
    window.alert("Error: $message ${toJsString(info)}${e?.stackTraceToString()?.let { ": $it" } ?: ""}").also {
        console.error(info)
    }

fun alertError(message: String, vararg info: Any?): Unit =
    window.alert("Error: $message ${toJsString(info)}").also { console.error(info) }
