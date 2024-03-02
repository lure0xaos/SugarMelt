package sugarmelt.data.ui

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class VarUI(
    container: HTMLElement,
    labelPoint: HTMLElement = container,
    childrenPoint: HTMLElement,
    val editor: HTMLInputElement,
    val lock: HTMLInputElement,
    deleteButton: HTMLElement?,
    createButton: HTMLElement?,
) : GeneralUI(container, labelPoint, childrenPoint, deleteButton, createButton)
