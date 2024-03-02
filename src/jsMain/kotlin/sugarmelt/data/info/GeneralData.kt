package sugarmelt.data.info

import sugarmelt.data.ui.GeneralUI

sealed class GeneralData(
    val parent: GeneralCollectionData?,
    val path: List<String>,
    val name: String,
    val jsType: String,
    open val ui: GeneralUI,
) :
    Comparable<GeneralData> {
    val expression: String = path.joinToString("") { "['${it}']" }
    override fun equals(other: Any?): Boolean =
        other != null && (other is GeneralData) && this::class == other::class && path == other.path

    override fun hashCode(): Int = path.hashCode()
    override fun toString(): String = "${this::class.simpleName}@$jsType"
    override fun compareTo(other: GeneralData): Int = expression.lowercase().compareTo(other.expression.lowercase())
}
