package sugarmelt.data.info

import sugarmelt.data.ui.GeneralUI

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ArrayData(parent: GeneralCollectionData?, path: List<String>, name: String, jsType: String, element: GeneralUI) :
    GeneralCollectionData(parent, path, name, jsType, element) {
    fun has(index: Int): Boolean = has(index.toString())
    operator fun get(index: Int): GeneralData? = get(index.toString())
    operator fun set(index: Int, value: GeneralData): Unit = set(index.toString(), value)
    operator fun plusAssign(child: GeneralData) {
        require(this != child) {
            "adding self as child"
        }
        items[size.toString()] = child
    }
}
