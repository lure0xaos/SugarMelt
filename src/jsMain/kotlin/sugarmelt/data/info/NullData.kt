package sugarmelt.data.info

import sugarmelt.data.ui.GeneralUI

@Suppress("unused", "MemberVisibilityCanBePrivate")
class NullData(
    parent: GeneralCollectionData?,
    path: List<String>,
    name: String,
    jsType: String,
    var value: Any?,
    ui: GeneralUI
) :
    GeneralData(parent, path, name, jsType, ui) {
    override fun toString(): String = "${super.toString()}[null]"
}
