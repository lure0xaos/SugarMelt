package sugarmelt.data

import de.comahe.i18n4k.Locale

fun getCountryCode(locale: Locale): String {
    var countryCode = ""
    if (locale.getCountry().isNotEmpty()) {
        val lastPart = locale.getCountry().uppercase()
        if (lastPart.length == 2 && lastPart.matches(Regex("[A-Z]{2}"))) {
            countryCode = lastPart
        }
    }
    if (countryCode.isEmpty()) {
        val languageCode = locale.getLanguage().lowercase()
        countryCode = getDefaultCountryForLanguage(languageCode)
    }
    if (countryCode.isEmpty()) return ""
    return countryCode
}

private fun getDefaultCountryForLanguage(language: String): String =
    when (language) {
        "af" -> "ZA"
        "am" -> "ET"
        "ar" -> "EG"
        "az" -> "AZ"
        "be" -> "BY"
        "bg" -> "BG"
        "bn" -> "BD"
        "bs" -> "BA"
        "ca" -> "ES"
        "cs" -> "CZ"
        "da" -> "DK"
        "de" -> "DE"
        "el" -> "GR"
        "en" -> "US"
        "es" -> "ES"
        "et" -> "EE"
        "fa" -> "IR"
        "fi" -> "FI"
        "fr" -> "FR"
        "he" -> "IL"
        "hi" -> "IN"
        "hr" -> "HR"
        "hu" -> "HU"
        "hy" -> "AM"
        "id" -> "ID"
        "is" -> "IS"
        "it" -> "IT"
        "ja" -> "JP"
        "ka" -> "GE"
        "kk" -> "KZ"
        "ko" -> "KR"
        "ky" -> "KG"
        "lt" -> "LT"
        "lv" -> "LV"
        "mk" -> "MK"
        "mn" -> "MN"
        "ms" -> "MY"
        "nl" -> "NL"
        "no" -> "NO"
        "pl" -> "PL"
        "pt" -> "BR"
        "ro" -> "RO"
        "ru" -> "RU"
        "sk" -> "SK"
        "sl" -> "SI"
        "sq" -> "AL"
        "sr" -> "RS"
        "sv" -> "SE"
        "sw" -> "TZ"
        "th" -> "TH"
        "tr" -> "TR"
        "uk" -> "UA"
        "ur" -> "PK"
        "uz" -> "UZ"
        "vi" -> "VN"
        "zh" -> "CN"
        else -> ""
    }