package sugarmelt.css

import nl.astraeus.css.properties.BorderStyle
import nl.astraeus.css.properties.BorderWidth
import nl.astraeus.css.properties.Color
import nl.astraeus.css.properties.Content
import nl.astraeus.css.style.Style

fun Style.borders(
    color: Color, width: BorderWidth = BorderWidth.thin, style: BorderStyle = BorderStyle.solid,
    vertical: Boolean = true, horizontal: Boolean = true
) {
    if (vertical) bordersVertical(color, width, style)
    if (horizontal) bordersHorizontal(color, width, style)
}

fun Style.bordersVertical(color: Color, width: BorderWidth = BorderWidth.thin, style: BorderStyle = BorderStyle.solid) {
    bordersLeft(color, width, style)
    bordersRight(color, width, style)
}

fun Style.bordersHorizontal(
    color: Color,
    width: BorderWidth = BorderWidth.thin,
    style: BorderStyle = BorderStyle.solid
) {
    bordersTop(color, width, style)
    bordersBottom(color, width, style)
}

fun Style.bordersLeft(color: Color, width: BorderWidth = BorderWidth.thin, style: BorderStyle = BorderStyle.solid) {
    borderLeftWidth(width)
    borderLeftStyle(style)
    borderLeftColor(color)
}

fun Style.bordersRight(color: Color, width: BorderWidth = BorderWidth.thin, style: BorderStyle = BorderStyle.solid) {
    borderRightWidth(width)
    borderRightStyle(style)
    borderRightColor(color)
}

fun Style.bordersTop(color: Color, width: BorderWidth = BorderWidth.thin, style: BorderStyle = BorderStyle.solid) {
    borderTopWidth(width)
    borderTopStyle(style)
    borderTopColor(color)
}

fun Style.bordersBottom(color: Color, width: BorderWidth = BorderWidth.thin, style: BorderStyle = BorderStyle.solid) {
    borderBottomWidth(width)
    borderBottomStyle(style)
    borderBottomColor(color)
}

fun Content.Companion.inlineSvg(svg: String): Content =
    url("'data:image/svg+xml;utf8,${svg.replace("#", "%23")}'")
