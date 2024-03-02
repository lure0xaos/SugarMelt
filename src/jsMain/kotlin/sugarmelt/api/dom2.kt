package sugarmelt.api

import org.w3c.dom.events.EventTarget

fun <T : EventTarget> T.onClick(callback: EventHandler<T>): T =
    addListener("click", callback)

fun <T : EventTarget> T.onChange(callback: EventHandler<T>): T =
    addListener("change", callback)

fun <T : EventTarget> T.onFocus(callback: EventHandler<T>): T =
    addListener("focus", callback)

fun <T : EventTarget> T.onBlur(callback: EventHandler<T>): T =
    addListener("blur", callback)

fun <T : EventTarget> T.onKeyUp(callback: EventHandler<T>): T =
    addListener("keyup", callback)

fun <T : EventTarget> T.onKeyDown(callback: EventHandler<T>): T =
    addListener("keydown", callback)
