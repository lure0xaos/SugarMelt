package sugarmelt.data.info

import sugarmelt.data.ui.GeneralUI

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ObjectData(parent: GeneralCollectionData?, path: List<String>, name: String, jsType: String, element: GeneralUI) :
    GeneralCollectionData(parent, path, name, jsType, element)
