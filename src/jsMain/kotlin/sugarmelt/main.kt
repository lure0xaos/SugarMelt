package sugarmelt

import de.comahe.i18n4k.config.I18n4kConfigDefault
import de.comahe.i18n4k.i18n4k
import kotlinx.browser.window
import sugarmelt.app.SugarMelt
import sugarmelt.app.SugarMelt.Companion.languageOr
import sugarmelt.data.SugarMeltOptions

fun main(): Unit =
    window.document.addEventListener("DOMContentLoaded", {
        val i18n4kConfig = I18n4kConfigDefault()
        i18n4k = i18n4kConfig
        SugarMeltOptions(language = i18n4kConfig.locale.languageOr()).load {
            i18n4kConfig.locale = it.language
            SugarMelt.construct(it, i18n4kConfig)
        }
    })
