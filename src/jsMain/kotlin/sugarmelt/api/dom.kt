@file:Suppress("NOTHING_TO_INLINE")

package sugarmelt.api

import kotlinx.dom.createElement
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

typealias Attributes = Map<String, String>
typealias Initializer<T> = T.() -> Unit
typealias EventHandler<T> = T.(Event) -> Unit

inline val <T : Element>T.parentDocument: Document get() = ownerDocument ?: error("not attached")
inline val Document.window: Window get() = defaultView ?: error("no window for document")
inline val <T : Element>T.parentWindow: Window get() = parentDocument.window
fun <T : Element> Element.createElement(
    tag: String,
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<T> = {}
): T =
    parentDocument.createElement(tag) {
        classes?.also { setAttribute("class", it) }
        attrs?.forEach { (key: String, value: String) -> setAttribute(key, value) }
    }.unsafeCast<T>().apply(init).also(::appendChild)

fun <T : Element> T.createText(text: String): T =
    parentDocument.createTextNode(text).also(::appendChild).let { this }

fun <T : Element> T.text(text: String): T = createText(text)

fun <T : Element> Element.findElement(id: String): T? =
    ownerDocument?.getElementById(id)?.unsafeCast<T>()

fun <T : Element> Element.getElement(id: String): T =
    findElement(id) ?: error("element '$id' not found")

fun <T : Element> Element.findElementsByClass(className: String): HTMLCollection? =
    ownerDocument?.getElementsByClassName(className) ?: error("no elements '$className'")

fun <T : Element> Element.findElementByClass(className: String): T? =
    ownerDocument?.getElementsByClassName(className)?.item(0)?.unsafeCast<T>()

fun <T : Element> Element.getElementByClass(className: String): T =
    findElementByClass(className) ?: error("element '$className' not found")

class AddEventListenerOptions {
    var capture: Boolean = true
    var once: Boolean = false
    var passive: Boolean = false
    var signal: Boolean = false
}

fun <T : EventTarget> T.addListener(type: String, callback: EventHandler<T>): T =
    apply { addEventListener(type, { callback(it) }, AddEventListenerOptions()) }

operator fun HTMLCollection.iterator(): Iterator<Element> =
    object : Iterator<Element> {
        private var i = 0
        override fun hasNext(): Boolean = i < length
        override fun next(): Element = item(i).also { i++ } ?: error("No such Element")
    }

fun HTMLCollection.toList(): List<Element> =
    List(length) { item(it) ?: error("No such Element") }

inline fun HTMLCollection.forEach(action: (Element) -> Unit) {
    for (element in this) action(element)
}

inline fun HTMLCollection.forEachIndexed(action: (index: Int, Element) -> Unit) {
    var index = 0
    for (item in this) action(index++, item)
}

inline val HTMLCollection.size: Int get() = length
inline fun HTMLCollection.first(): Element? = get(0)
inline fun HTMLCollection.last(): Element? = get(length - 1)

inline operator fun <reified E : HTMLElement> HTMLCollection.get(i: Int): E =
    item(i).also { require(it is E) { "item is not a ${E::class}" } } as E

inline fun <reified E : HTMLElement> HTMLCollection.single(): E =
    also { require(length == 1) { "collection must contain only one element" } }[0]

fun HTMLCollection.single(): HTMLElement =
    also { require(length == 1) { "collection must contain only one element" } }.get<HTMLElement>(0)

inline fun HTMLElement.removeAll() {
    for (item in children) item.remove()
}

fun makeClasses(vararg classes: String?): String =
    classes.filterNot { it.isNullOrBlank() }.joinToString(" ")

fun Element.addStyle(vararg styles: String) {
    parentDocument.head!!.createElement<HTMLStyleElement>("style").textContent = styles.joinToString("\n")
}

operator fun DOMTokenList.contains(classes: String): Boolean = contains(classes)
operator fun DOMTokenList.plusAssign(classes: String): Unit = add(classes)
operator fun DOMTokenList.minusAssign(classes: String): Unit = remove(classes)
operator fun DOMTokenList.remAssign(classes: String) {
    toggle(classes)
}
