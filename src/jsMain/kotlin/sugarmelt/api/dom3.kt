package sugarmelt.api

import org.w3c.dom.*

fun Element.div(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLDivElement> = {}
): HTMLDivElement =
    createElement("div", classes, attrs, init)

fun Element.span(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLSpanElement> = {}
): HTMLSpanElement =
    createElement("span", classes, attrs, init)

fun Element.h1(
    text: String = "",
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLHeadingElement> = {}
): HTMLHeadingElement =
    createElement("h1", classes, attrs, init).createText(text)

fun Element.h2(
    text: String = "",
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLHeadingElement> = {}
): HTMLHeadingElement =
    createElement("h2", classes, attrs, init).createText(text)

fun Element.h3(
    text: String = "",
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLHeadingElement> = {}
): HTMLHeadingElement =
    createElement("h3", classes, attrs, init).createText(text)

fun Element.hr(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLHRElement> = {}
): HTMLHRElement =
    createElement("hr", classes, attrs, init)

fun Element.input(
    type: String,
    value: String = "",
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLInputElement> = {}
): HTMLInputElement =
    createElement("input", classes, attrs) { this.type = type; this.value = value; init() }

fun Element.label(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLLabelElement> = {}
): HTMLLabelElement =
    createElement("label", classes, attrs, init)

fun Element.table(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLTableElement> = {}
): HTMLTableElement =
    createElement("table", classes, attrs, init)

fun HTMLTableElement.caption(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLTableCaptionElement> = {}
): HTMLTableCaptionElement =
    createElement("caption", classes, attrs, init)

fun Element.tr(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLTableRowElement> = {}
): HTMLTableRowElement =
    createElement("tr", classes, attrs, init)

fun Element.td(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLTableCellElement> = {}
): HTMLTableCellElement =
    createElement("td", classes, attrs, init)

fun Element.th(
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLTableCellElement> = {}
): HTMLTableCellElement =
    createElement("th", classes, attrs, init)

fun Element.a(
    url: String = "#",
    classes: String? = null,
    attrs: Attributes? = null,
    init: Initializer<HTMLAnchorElement> = {}
): HTMLAnchorElement =
    createElement("a", classes, attrs) { href = url; init() }
