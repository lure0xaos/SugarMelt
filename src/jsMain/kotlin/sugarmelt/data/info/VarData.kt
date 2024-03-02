package sugarmelt.data.info

import sugarmelt.data.ui.VarUI
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class VarData(
    parent: GeneralCollectionData?,
    path: List<String>,
    name: String,
    val type: KClass<out Any>,
    jsType: String,
    var value: Any?,
    override val ui: VarUI
) : GeneralData(parent, path, name, jsType, ui) {
    var isEditing: Boolean = false
    var isLocked: Boolean = false
    var lockedValue: Any? = value
    override fun toString(): String = "${super.toString()}[$value:$type]"
}
