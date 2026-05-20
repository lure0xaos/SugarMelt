package sugarmelt.css

import nl.astraeus.css.properties.*
import nl.astraeus.css.style
import nl.astraeus.css.style.ConditionalStyle
import nl.astraeus.css.style.Style
import nl.astraeus.css.style.cls
import nl.astraeus.css.style.id

object Stylesheets {
    private val backgroundColor = Color("#000000")
    private val bodyColor = Color("#cccccc")
    private val secondaryColor = Color("#333333")
    private val dangerColor = Color("#ff0000")
    private val successColor = Color("#008000")
    private val tertiaryColor = Color("#808080")
    private val highlightColor = Color("#ffff00")
    private val changedColor = Color("#00ff00")
    private val hoverColor = Color("#ccccff")
    private val collapseColor = Color("#660000")
    private val collapseHoverColor = Color("#803333")

    private val colorBorders = Color.gray

    private val sliderHeight = 17.px
    private val sliderWidth = 30.px
    private val sliderSize = 13.px
    private val sliderPos = 2.px
    private val sliderColorOn = Color.mediumBlue
    private val sliderColorOff = Color("#333333")
    private val sliderColor = Color.white
    private val sliderTransition = ".4s"

    private val lockColorSecondary = "#0000FF"
    private val lockColorPrimary = "#FF00FF"
    private val lockSvgUnchecked =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"15\" height=\"15\" viewBox=\"0 0 150 150\" version=\"1.0\" y=\"0\" x=\"0\"><g transform=\"translate(-2.9648 2.5)\"><path style=\"fill-rule:evenodd;fill:$lockColorSecondary\" d=\"m112.5 4.1909c-20.072 0-36.231 16.159-36.231 36.231v40.256h10.064v-40.256c0-14.497 11.671-26.167 26.167-26.167 14.5 0 26.17 11.67 26.17 26.167v27.756h10.06v-27.756c0-20.072-16.16-36.231-36.23-36.231z\"/><rect style=\"fill-rule:evenodd;fill:$lockColorPrimary\" rx=\"0.5\" ry=\"0.5\" height=\"66.259\" width=\"85.603\" y=\"74.55\" x=\"7.1987\"/></g></svg>"
    private val lockSvgChecked =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"15\" height=\"15\" viewBox=\"0 0 150 150\" version=\"1.0\" y=\"0\" x=\"0\"><g transform=\"translate(10.641 12.191)\"><path style=\"fill-rule:evenodd;fill:$lockColorSecondary\" d=\"m64.359 2c-20.072 0-36.231 16.159-36.231 36.231v40.256h10.064v-40.256c0-14.497 11.671-26.167 26.167-26.167s26.167 11.67 26.167 26.167v40.256h10.064v-40.256c0-20.072-16.159-36.231-36.231-36.231z\"/><rect style=\"fill-rule:evenodd;fill:$lockColorPrimary\" rx=\"0.5\" ry=\"0.5\" height=\"66.259\" width=\"85.603\" y=\"57.359\" x=\"21.558\"/></g></svg>"


    val stylesheet: String = style {
        cssTables()
        cssCommon()
        select(id("container")) {
            cssContainer()
        }
        select(cls("tools")) {
            cssTools()
        }
        select(id("content")) {
            cssContent()
        }
    }.generateCss()

    private fun ConditionalStyle.cssCommon() {
        select("body") {
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(cls("label")) {
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select("hr") {
            borders(secondaryColor)
        }
    }

    private fun ConditionalStyle.cssTables() {
        select(cls("table")) {
            display(Display.table)
            borderCollapse(BorderCollapse.separate)
            boxSizing(BoxSizing.borderBox)
            textIndent(Measurement.initial)
            borderSpacing(BorderSpacing.px(2))
            borderColor(colorBorders)
            select(cls("caption")) {
                display(Display.tableCaption)
                textAlign(TextAlign.center)
            }
            select(cls("tr")) {
                display(Display.tableRow)
                verticalAlign(VerticalAlign.inherit)
                borderColor(Color.inherit)
                select(cls("th")) {
                    display(Display.tableCell)
                    verticalAlign(VerticalAlign.inherit)
                    fontWeight(FontWeight.bold)
                }
                select(cls("td")) {
                    display(Display.tableCell)
                    verticalAlign(VerticalAlign.inherit)
                }
            }
        }
    }

    private fun Style.cssContainer() {
        val stickyTopCalHeight = 125.px
        select(cls("sticky-top")) {
            position(Position.fixed)
            top(0.px)
            left(0.px)
            width(100.prc)
            height(stickyTopCalHeight)
            and(cls("z-1")) {
                zIndex(100)
            }
        }
        select(cls("sticky-top-after")) {
            paddingTop(stickyTopCalHeight)
            width(100.prc)
        }
        select(cls("background-black")) {
            backgroundColor(Color.black)
        }
        select(cls("table-header")) {
            width(100.prc)
        }
        select(cls("header")) {
            textAlign(TextAlign.center)
            select("a") {
                color(Color.inherit)
                textDecoration("none")
            }
        }
        select(id("game-title")) {
            paddingLeft(5.px)
            paddingRight(5.px)
        }
        select(cls("message")) {
            and(cls("message-error")) {
                color(dangerColor)
            }
            and(cls("message-success")) {
                color(successColor)
            }
        }
        select(id("toolbar")) {
            select(cls("button")) {
                textAlign(TextAlign.center)
                backgroundColor(backgroundColor)
                color(bodyColor)
            }
        }
    }

    private fun Style.cssContent() {
        select(cls("clickable")) {
            select("label") {
                cursor("pointer")
            }
            cursor("pointer")
        }
        select(cls("cell-label")) {
            textAlign(TextAlign.right)
            fontWeight(FontWeight.normal)
            verticalAlign(VerticalAlign.top)
        }
        select(cls("cell-data")) {
            borders(Color.transparent)
            and(cls("single")) {
                whiteSpace(WhiteSpace.nowrap)
            }
        }
        select(cls("editor")) {
            width(100.prc)
            textAlign(TextAlign.left)
            backgroundColor(backgroundColor)
            color(bodyColor)
            marginRight((-25).px)
            and(cls("editor-number")) {
                marginRight((-40).px)
            }
            and(cls("editor-boolean")) {
                minWidth(3.em)
                maxWidth(3.em)
            }
            and(cls("changed")) {
                borderColor(changedColor)
            }
            and(cls("highlight")) {
                color(highlightColor)
            }
        }
        select(cls("switch")) {
            display(Display.inlineBlock)
            height(sliderHeight)
            position(Position.relative)
            width(sliderWidth)
            select("input") {
                display(Display.none)
            }
            select(cls("slider")) {
                backgroundColor(sliderColorOff)
                bottom(0.px)
                cursor("pointer")
                left(0.px)
                position(Position.absolute)
                right(0.px)
                top(0.px)
                transition(sliderTransition)
                and((":before")) {
                    backgroundColor(sliderColor)
                    bottom(sliderPos)
                    content(Content("''"))
                    height(sliderSize)
                    left(sliderPos)
                    position(Position.absolute)
                    transition(sliderTransition)
                    width(sliderSize)
                }
                and(cls("round")) {
                    borderRadius(sliderHeight)
                    and(":before") {
                        borderRadius(50.prc)
                    }
                }
            }
            select("input:checked + .slider") {
                backgroundColor(sliderColorOn)
                and(":before") {
                    transform(Transform("translateX($sliderSize)"))
                }
            }
        }
        select(":has(input[type=\"checkbox\"].editor-lock)") {
            whiteSpace(WhiteSpace.nowrap)
            overflow(Overflow.hidden)
        }
        select("input[type=\"checkbox\"].editor-lock") {
            display(Display.none)
            and(" + label:after") {
                content(Content.inlineSvg(lockSvgUnchecked))
            }
            and(":checked + label:after") {
                content(Content.inlineSvg(lockSvgChecked))
            }
        }
        select(cls("object-empty")) {
            color(tertiaryColor)
        }
        select(cls("grid")) {
            width(100.prc)
            select(cls("row")) {
                borderCollapse(BorderCollapse.collapse)
                bordersHorizontal(secondaryColor)
                hover {
                    bordersHorizontal(hoverColor)
                }
                select(cls("cell")) {
                    bordersHorizontal(Color.inherit)
                    hover {
                        bordersHorizontal(Color.inherit)
                    }
                    and(cls("cell-label")) {
                        textAlign(TextAlign.left)
                        width(30.prc)
                        select(cls("label")) {
                            paddingLeft(5.px)
                            paddingRight(5.px)
                        }
                        and(cls("highlight")) {
                            color(highlightColor)
                            select("label") {
                                color(highlightColor)
                            }
                        }
                        bordersHorizontal(Color.inherit)
                        bordersLeft(Color.inherit)
                        hover {
                            bordersHorizontal(Color.inherit)
                            bordersLeft(Color.inherit)
                        }
                        select(cls("var-controls")) {
                            select(cls("button")) {
                                textAlign(TextAlign.center)
                                backgroundColor(backgroundColor)
                                color(bodyColor)
                            }
                        }
                    }
                    and(cls("cell-data")) {
                        and(not(cls("collapsible"))) {
                            bordersHorizontal(Color.inherit)
                            bordersRight(Color.inherit)
                            hover {
                                bordersHorizontal(Color.inherit)
                                bordersRight(Color.inherit)
                            }
                        }
                        and(cls("collapsible")) {
                            and(cls("collapsed")) {
                                bordersHorizontal(collapseColor)
                                bordersRight(collapseColor)
                                hover {
                                    bordersHorizontal(collapseColor)
                                    bordersRight(collapseColor)
                                }
                                select("*") {
                                    display(Display.none)
                                }
                            }
                            and(cls("collapsed:hover")) {
                                bordersHorizontal(collapseHoverColor)
                                bordersRight(collapseHoverColor)
                                hover {
                                    bordersHorizontal(collapseHoverColor)
                                    bordersRight(collapseHoverColor)
                                }
                                select("*") {
                                    display(Display.none)
                                }
                            }
                        }
                    }
                }
            }
            select(cls("highlight")) {
                select(cls("editor")) {
                    color(highlightColor)
                }
            }
        }
        select(cls("hidden")) {
            display(Display.none)
        }
    }

    private fun Style.cssTools() {
        width(100.prc)
        select(cls("cell-center")) {
            textAlign(TextAlign.center)
        }
        select("a") {
            color(Color.inherit)
            textDecoration("none")
        }
        select(cls("caption")) {
            fontSize(FontSize.large)
            margin(0.px)
        }
        select(cls("cell-label")) {
            textAlign(TextAlign.right)
            fontWeight(FontWeight.normal)
        }
        select(cls("editor")) {
            minWidth(95.prc)
            maxWidth(95.prc)
            textAlign(TextAlign.left)
            backgroundColor(backgroundColor)
            color(bodyColor)
            and(cls("editor-min")) {
                width(8.em)
            }
        }
        select(cls("button")) {
            textAlign(TextAlign.center)
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(cls("small")) {
            fontSize(FontSize.small)
            color(secondaryColor)
        }
    }
}
