package sugarmelt.api

import org.w3c.dom.Window
import kotlin.js.Json
import kotlin.js.Promise

private val isChrome: Boolean = js("typeof browser === 'undefined'") as Boolean

fun <T> evaluate(expression: String, onSuccess: (T) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
    if (isChrome)
        chrome.devtools.inspectedWindow.eval<T>(expression, mapOf()) { result, exception ->
            if (exception == null) onSuccess(result) else onError(Exception(exception))
        }
    else
        browser.devtools.inspectedWindow.eval<T>(expression) { result, exception ->
            if (exception == null) onSuccess(result) else onError(Exception(exception))
        }
}

fun createPanel(title: String, icon: String, page: String, onShown: (Window) -> Unit, onHidden: (Window) -> Unit) {
    if (isChrome)
        chrome.devtools.panels.create(title, icon, page) { panel ->
            panel.onShown.addListener { window -> onShown(window) }
            panel.onHidden.addListener { window -> onHidden(window) }
        }
    else
        browser.devtools.panels.create(title, icon, page).then { panel ->
            panel.onShown.addListener { window -> onShown(window) }
            panel.onHidden.addListener { window -> onHidden(window) }
        }
}

fun <T : Any> storageGet(defData: T, onSuccess: (T) -> Unit, onError: (Throwable) -> Unit) {
    if (isChrome)
        chrome.storage.local.get(defData) { data: T -> onSuccess(data) }
    else
        browser.storage.local.get(defData).then(onSuccess).catch(onError)
}

fun <T : Any> storageSet(data: T, onError: (Throwable) -> Unit) {
    if (isChrome)
        chrome.storage.local.set(data) {}
    else
        browser.storage.local.set(data).catch(onError)
}

fun getManifest(): Json =
    if (isChrome) chrome.runtime.getManifest() else browser.runtime.getManifest()

@Suppress("ClassName")
external object browser {
    object devtools {
        object inspectedWindow {
            fun <T> eval(
                expression: String,
                options: Map<String, Any?> = definedExternally,
                callback: (T, ExceptionInfo?) -> Unit = definedExternally
            ): Promise<T>
        }

        object panels {
            fun create(title: String, icon: String, page: String): Promise<ExtensionPanel>
        }
    }

    object storage {
        object local {
            fun <T> get(defData: T): Promise<T> = definedExternally
            fun set(data: Any): dynamic = definedExternally
        }
    }

    object runtime {
        fun getManifest(): Json
    }
}

@Suppress("ClassName")
external object chrome {
    object devtools {
        object inspectedWindow {
            fun <T> eval(
                expression: String,
                options: Map<String, Any?> = definedExternally,
                callback: (T, ExceptionInfo?) -> Unit = definedExternally
            )
        }

        object panels {
            fun create(
                title: String,
                icon: String,
                page: String,
                callback: (ExtensionPanel) -> Unit
            ): Promise<ExtensionPanel>
        }
    }

    object storage {
        object local {
            fun <T> get(keys: Any, callback: (data: T) -> Unit): dynamic = definedExternally
            fun set(data: Any, callback: () -> Unit): dynamic = definedExternally
        }
    }

    object runtime {
        fun getManifest(): Json
    }
}

external class ExtensionPanel {
    val onShown: ExtensionPanelProperty
    val onHidden: ExtensionPanelProperty
}

external class ExtensionPanelProperty {
    fun addListener(callback: (Window) -> Unit)
}
