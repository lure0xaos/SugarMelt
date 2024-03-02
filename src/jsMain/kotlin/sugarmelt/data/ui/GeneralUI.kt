package sugarmelt.data.ui

import org.w3c.dom.HTMLElement

open class GeneralUI(
    val containerPoint: HTMLElement,
    val labelPoint: HTMLElement,
    val childrenPoint: HTMLElement,
    val deleteButton: HTMLElement?,
    val createButton: HTMLElement?,
)
