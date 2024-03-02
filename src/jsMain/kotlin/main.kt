import kotlinx.browser.window
import sugarmelt.app.SugarMelt

fun main(): Unit = window.document.addEventListener("DOMContentLoaded", { SugarMelt.construct() })
