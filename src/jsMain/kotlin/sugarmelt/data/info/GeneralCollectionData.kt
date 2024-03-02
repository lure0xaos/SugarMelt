package sugarmelt.data.info

import sugarmelt.data.ui.GeneralUI

sealed class GeneralCollectionData(
    parent: GeneralCollectionData?,
    path: List<String>,
    name: String,
    jsType: String,
    ui: GeneralUI
) :
    GeneralData(parent, path, name, jsType, ui) {
    protected val items: MutableMap<String, GeneralData> = mutableMapOf()
    val size: Int = items.size
    override fun toString(): String = "${super.toString()}($items)"
    fun has(key: String): Boolean = items.containsKey(key)
    operator fun get(key: String): GeneralData? = items[key]
    operator fun set(key: String, child: GeneralData) {
        require(this != child) {
            "adding self as child"
        }
        items[key] = child
    }

    fun forEach(action: (GeneralData) -> Unit) {
        val sorted = items.entries.sortedBy { it.key }
        for ((_, value) in sorted) action(value)
    }

    fun forEachIndexed(action: (key: String, GeneralData) -> Unit) {
        val sorted = items.entries.sortedBy { it.key }
        for ((key, value) in sorted) action(key, value)
    }

    fun deleteKey(name: String) {
        items.remove(name)
    }
}
