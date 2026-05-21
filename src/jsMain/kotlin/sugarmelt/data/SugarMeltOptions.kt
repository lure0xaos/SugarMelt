package sugarmelt.data

import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.i18n4k
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import sugarmelt.Messages
import sugarmelt.api.*

@Serializable
data class SugarMeltOptions(
    var interval: Int = 500,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @Serializable(with = LocaleAsStringSerializer::class)
    var language: Locale = i18n4k.locale,
) {
    val isAutomatic: Boolean
        get() = interval > 0

    fun loadFrom(it: SugarMeltOptions) {
        this.interval = it.interval
        this.language = it.language
    }

    private val name: String
        get() = this::class.simpleName!!

    fun save(): Unit =
        storageSet(mapOf(name to encode(this)).toJsJson()) { alertError(it, Messages.options_error_save()) }

    fun load(onOptions: (SugarMeltOptions) -> Unit = {}): Unit =
        storageGet(
            mapOf(name to encode(this)).toJsJson(), { it ->
                if (!jsIsUndefined(it)) {
                    it.toKotlinMap().let { it ->
                        if (it.isNotEmpty()) {
                            (decode<SugarMeltOptions>((it[name] ?: return@let).toString()))
                                .also { loadFrom(it) }.let { onOptions(it) }
                        }
                    }
                }
            }) { alertError(it, Messages.options_error_load()) }
}
