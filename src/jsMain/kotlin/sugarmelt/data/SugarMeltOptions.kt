package sugarmelt.data

import sugarmelt.Messages
import sugarmelt.api.alertError
import sugarmelt.api.storageGet
import sugarmelt.api.storageSet

class SugarMeltOptions(var interval: Int = 500) {
    val isAutomatic: Boolean
        get() = interval > 0

    fun save(): Unit =
        storageSet(this) { alertError(it, Messages.options_error_save()) }

    fun load(): Unit =
        load { this.interval = it.interval }

    fun load(onOptions: (SugarMeltOptions) -> Unit): Unit =
        storageGet(this, onOptions) { alertError(it, Messages.options_error_load()) }

    init {
        load()
    }
}
