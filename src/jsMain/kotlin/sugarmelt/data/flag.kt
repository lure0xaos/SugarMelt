package sugarmelt.data

import de.comahe.i18n4k.Locale
import org.w3c.dom.HTMLElement
import sugarmelt.api.i


fun HTMLElement.flag(locale: Locale) {
    i("fi fi-${getCountryCode(locale).lowercase()} fis") {}
}

