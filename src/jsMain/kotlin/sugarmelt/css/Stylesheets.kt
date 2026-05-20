package sugarmelt.css

import nl.astraeus.css.properties.*
import nl.astraeus.css.style
import nl.astraeus.css.style.ConditionalStyle
import nl.astraeus.css.style.Style
import nl.astraeus.css.style.cls
import nl.astraeus.css.style.id

object Stylesheets {
    private val backgroundColor = Color.black
    private val bodyColor = Color("#cccccc")
    private val secondaryColor = Color("#333333")
    private val dangerColor = Color.red
    private val successColor = Color.green
    private val tertiaryColor = Color.grey
    private val highlightColor = Color.yellow
    private val changedColor = Color.lime
    private val hoverColor = Color("#ccccff")
    private val collapseColor = Color("#660000")
    private val collapseHoverColor = Color("#803333")

    private val backgroundColorLighter = Color("#101010")
    private val containerBackground = backgroundColor

    private val colorBorders = Color.gray

    private val sliderHeight = 17.px
    private val sliderWidth = 30.px
    private val sliderSize = 13.px
    private val sliderPos = 2.px
    private val sliderColorOn = Color("#000033")
    private val sliderColorOff = Color("#333333")
    private val sliderColor = Color.white
    private val sliderTransition = ".4s"

    private val lockColorSecondary = Color.blue
    private val lockColorPrimary = Color.fuchsia
    private val lockSvgUnchecked =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"15\" height=\"15\" viewBox=\"0 0 150 150\" version=\"1.0\" y=\"0\" x=\"0\"><g transform=\"translate(-2.9648 2.5)\"><path style=\"fill-rule:evenodd;fill:$lockColorSecondary\" d=\"m112.5 4.1909c-20.072 0-36.231 16.159-36.231 36.231v40.256h10.064v-40.256c0-14.497 11.671-26.167 26.167-26.167 14.5 0 26.17 11.67 26.17 26.167v27.756h10.06v-27.756c0-20.072-16.16-36.231-36.23-36.231z\"/><rect style=\"fill-rule:evenodd;fill:$lockColorPrimary\" rx=\"0.5\" ry=\"0.5\" height=\"66.259\" width=\"85.603\" y=\"74.55\" x=\"7.1987\"/></g></svg>"
    private val lockSvgChecked =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"15\" height=\"15\" viewBox=\"0 0 150 150\" version=\"1.0\" y=\"0\" x=\"0\"><g transform=\"translate(10.641 12.191)\"><path style=\"fill-rule:evenodd;fill:$lockColorSecondary\" d=\"m64.359 2c-20.072 0-36.231 16.159-36.231 36.231v40.256h10.064v-40.256c0-14.497 11.671-26.167 26.167-26.167s26.167 11.67 26.167 26.167v40.256h10.064v-40.256c0-20.072-16.159-36.231-36.231-36.231z\"/><rect style=\"fill-rule:evenodd;fill:$lockColorPrimary\" rx=\"0.5\" ry=\"0.5\" height=\"66.259\" width=\"85.603\" y=\"57.359\" x=\"21.558\"/></g></svg>"

    const val ID_CONTAINER = "container"
    const val CLASS_TOOLS = "tools"
    const val ID_CONTENT = "content"
    const val ID_CONTROLS = "controls"

    const val TAG_BODY = "body"
    const val CLASS_LABEL = "label"
    const val TAG_HR = "hr"

    const val CLASS_TABLE = "table"
    const val CLASS_TABLE_CAPTION = "caption"
    const val CLASS_TR = "tr"
    const val CLASS_TH = "th"
    const val CLASS_TD = "td"

    const val CLASS_STICKY_TOP = "sticky-top"
    const val CLASS_Z_1 = "z-1"
    const val CLASS_STICKY_TOP_AFTER = "sticky-top-after"

    const val CLASS_CONTAINER_BACKGROUND = "container-background"

    const val CLASS_TABLE_HEADER = "table-header"
    const val CLASS_HEADER = "header"

    const val ID_GAME_TITLE = "game-title"

    const val CLASS_MESSAGE = "message"
    const val CLASS_MESSAGE__TYPE = "message-"
    const val CLASS_MESSAGE_ERROR = "${CLASS_MESSAGE__TYPE}error"
    const val CLASS_MESSAGE_SUCCESS = "${CLASS_MESSAGE__TYPE}success"

    const val ID_TOOLBAR = "toolbar"

    const val CLASS_BUTTON = "button"
    const val CLASS_CLICKABLE = "clickable"
    const val CLASS_CELL = "cell"
    const val CLASS_CELL_LABEL = "cell-label"
    const val CLASS_CELL_DATA = "cell-data"
    const val CLASS_CELL_DATA_MULTIPLE = "multiple"
    const val CLASS_CELL_DATA_SINGLE = "single"

    const val CLASS_EDITOR = "editor"
    const val CLASS_EDITOR__TYPE = "editor-"
    const val CLASS_EDITOR_NUMBER = "${CLASS_EDITOR__TYPE}number"
    const val CLASS_EDITOR_BOOLEAN = "${CLASS_EDITOR__TYPE}boolean"
    const val CLASS_EDITOR_CHANGED = "changed"
    const val CLASS_EDITOR_HIGHLIGHT = "highlight"

    const val CLASS_SWITCH = "switch"
    const val CLASS_SWITCH_INPUT = "input"
    const val CLASS_SWITCH_SLIDER = "slider"
    const val CLASS_SWITCH_SLIDER_ROUND = "round"
    const val CLASS_SWITCH_SLIDER_CHECKED = "input:checked + .${CLASS_SWITCH_SLIDER}"

    const val CLASS_EDITOR_LOCK = "editor-lock"

    const val CLASS_CHECKBOX_LOCK_OUTER = ":has(input[type=\"checkbox\"].$CLASS_EDITOR_LOCK)"
    const val CLASS_CHECKBOX_LOCK = "input[type=\"checkbox\"].$CLASS_EDITOR_LOCK"
    const val CLASS_CHECKBOX_LOCK_AFTER = " + label:after"
    const val CLASS_CHECKBOX_LOCK_AFTER_CHECKED = ":checked + label:after"

    const val CLASS_OBJECT_EMPTY = "object-empty"

    const val CLASS_GRID = "grid"
    const val CLASS_GRID_ROW = "row"
    const val CLASS_GRID_ROW_CELL = "cell"
    const val CLASS_GRID_ROW_CELL_ITEM = "cell-label"
    const val CLASS_GRID_ROW_CELL_LABEL = "cell-label"
    const val CLASS_GRID_VAR_CONTROLS = "var-controls"
    const val CLASS_CELL_DATA_COLLAPSIBLE = "collapsible"
    const val CLASS_CELL_DATA_COLLAPSED = "collapsed"
    const val CLASS_CELL_DATA_COLLAPSED_HOVER = "collapsed:hover"

    const val CLASS_HIDDEN = "hidden"

    const val CLASS_CELL_CENTER = "cell-center"

    const val CLASS_HEADER_LINK = "a"

    const val CLASS_EDITOR_MIN = "editor-min"

    const val CLASS_SMALL = "small"


    val stylesheet: String = style {
        cssTables()
        cssCommon()
        select(id(ID_CONTAINER)) {
            cssContainer()
        }
        select(cls(CLASS_TOOLS)) {
            cssTools()
        }
        select(id(ID_CONTENT)) {
            cssContent()
        }
    }.generateCss()

    private fun ConditionalStyle.cssCommon() {
        select(TAG_BODY) {
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(cls(CLASS_LABEL)) {
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(TAG_HR) {
            borders(secondaryColor)
        }
    }

    private fun ConditionalStyle.cssTables() {
        select(cls(CLASS_TABLE)) {
            select("*") {
                borderRadius(5.px)
            }
            display(Display.table)
            borderCollapse(BorderCollapse.separate)
            boxSizing(BoxSizing.borderBox)
            textIndent(Measurement.initial)
            borderSpacing(BorderSpacing.px(2))
            borderColor(colorBorders)
            select(cls(CLASS_TABLE_CAPTION)) {
                display(Display.tableCaption)
                textAlign(TextAlign.center)
            }
            select(cls(CLASS_TR)) {
                display(Display.tableRow)
                verticalAlign(VerticalAlign.inherit)
                borderColor(Color.inherit)
                select(cls(CLASS_TH)) {
                    display(Display.tableCell)
                    verticalAlign(VerticalAlign.inherit)
                    fontWeight(FontWeight.bold)
                }
                select(cls(CLASS_TD)) {
                    display(Display.tableCell)
                    verticalAlign(VerticalAlign.inherit)
                }
            }
        }
    }

    private fun Style.cssContainer() {
        val stickyTopCalHeight = 125.px
        select(cls(CLASS_STICKY_TOP)) {
            position(Position.fixed)
            top(0.px)
            left(0.px)
            width(100.prc)
            height(stickyTopCalHeight)
            and(cls(CLASS_Z_1)) {
                zIndex(100)
            }
        }
        select(cls(CLASS_STICKY_TOP_AFTER)) {
            paddingTop(stickyTopCalHeight)
            width(100.prc)
        }
        select(cls(CLASS_CONTAINER_BACKGROUND)) {
            backgroundColor(containerBackground)
        }
        select(cls(CLASS_TABLE_HEADER)) {
            width(100.prc)
        }
        select(cls(CLASS_HEADER)) {
            textAlign(TextAlign.center)
            select(CLASS_HEADER_LINK) {
                color(Color.inherit)
                textDecoration("none")
            }
        }
        select(id(ID_GAME_TITLE)) {
            paddingLeft(5.px)
            paddingRight(5.px)
        }
        select(cls(CLASS_MESSAGE)) {
            and(cls(CLASS_MESSAGE_ERROR)) {
                color(dangerColor)
            }
            and(cls(CLASS_MESSAGE_SUCCESS)) {
                color(successColor)
            }
        }
        select(id(ID_TOOLBAR)) {
            select(cls(CLASS_BUTTON)) {
                textAlign(TextAlign.center)
                backgroundColor(backgroundColor)
                color(bodyColor)
            }
        }
    }

    private fun Style.cssContent() {
        select(cls(CLASS_CLICKABLE)) {
            select(CLASS_LABEL) {
                cursor("pointer")
            }
            cursor("pointer")
        }
        select(cls(CLASS_CELL_LABEL)) {
            textAlign(TextAlign.right)
            fontWeight(FontWeight.normal)
            verticalAlign(VerticalAlign.top)
        }
        select(cls(CLASS_CELL_DATA)) {
            borders(Color.transparent)
            and(cls(CLASS_CELL_DATA_SINGLE)) {
                whiteSpace(WhiteSpace.nowrap)
            }
        }
        select(cls(CLASS_EDITOR)) {
            width(100.prc)
            textAlign(TextAlign.left)
            backgroundColor(backgroundColor)
            color(bodyColor)
            marginRight((-25).px)
            and(cls(CLASS_EDITOR_NUMBER)) {
                marginRight((-40).px)
            }
            and(cls(CLASS_EDITOR_BOOLEAN)) {
                minWidth(3.em)
                maxWidth(3.em)
            }
            and(cls(CLASS_EDITOR_CHANGED)) {
                borderColor(changedColor)
            }
            and(cls(CLASS_EDITOR_HIGHLIGHT)) {
                color(highlightColor)
            }
            and(":focus") {
                backgroundColor(backgroundColorLighter)
            }
        }
        select(cls(CLASS_SWITCH)) {
            display(Display.inlineBlock)
            height(sliderHeight)
            position(Position.relative)
            width(sliderWidth)
            select(CLASS_SWITCH_INPUT) {
                display(Display.none)
            }
            select(cls(CLASS_SWITCH_SLIDER)) {
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
                and(cls(CLASS_SWITCH_SLIDER_ROUND)) {
                    borderRadius(sliderHeight)
                    and(":before") {
                        borderRadius(50.prc)
                    }
                }
            }
            select(CLASS_SWITCH_SLIDER_CHECKED) {
                backgroundColor(sliderColorOn)
                and(":before") {
                    transform(Transform("translateX($sliderSize)"))
                }
            }
        }
        select(CLASS_CHECKBOX_LOCK_OUTER) {
            whiteSpace(WhiteSpace.nowrap)
            overflow(Overflow.hidden)
        }
        select(CLASS_CHECKBOX_LOCK) {
            display(Display.none)
            and(CLASS_CHECKBOX_LOCK_AFTER) {
                content(Content.inlineSvg(lockSvgUnchecked))
            }
            and(CLASS_CHECKBOX_LOCK_AFTER_CHECKED) {
                content(Content.inlineSvg(lockSvgChecked))
            }
        }
        select(cls(CLASS_OBJECT_EMPTY)) {
            color(tertiaryColor)
        }
        select(cls(CLASS_GRID)) {
            width(100.prc)
            select(cls(CLASS_GRID_ROW)) {
                borderCollapse(BorderCollapse.collapse)
                bordersHorizontal(secondaryColor)
                hover {
                    bordersHorizontal(hoverColor)
                }
                select(cls(CLASS_GRID_ROW_CELL)) {
                    bordersHorizontal(Color.inherit)
                    hover {
                        bordersHorizontal(Color.inherit)
                    }
                    and(cls(CLASS_CELL_LABEL)) {
                        textAlign(TextAlign.left)
                        width(30.prc)
                        select(cls(CLASS_LABEL)) {
                            paddingLeft(5.px)
                            paddingRight(5.px)
                        }
                        and(cls(CLASS_EDITOR_HIGHLIGHT)) {
                            color(highlightColor)
                            select(CLASS_LABEL) {
                                color(highlightColor)
                            }
                        }
                        bordersHorizontal(Color.inherit)
                        bordersLeft(Color.inherit)
                        hover {
                            bordersHorizontal(Color.inherit)
                            bordersLeft(Color.inherit)
                        }
                        select(cls(CLASS_GRID_VAR_CONTROLS)) {
                            select(cls(CLASS_BUTTON)) {
                                textAlign(TextAlign.center)
                                backgroundColor(backgroundColor)
                                color(bodyColor)
                            }
                        }
                    }
                    and(cls(CLASS_CELL_DATA)) {
                        and(not(cls(CLASS_CELL_DATA_COLLAPSIBLE))) {
                            bordersHorizontal(Color.inherit)
                            bordersRight(Color.inherit)
                            hover {
                                bordersHorizontal(Color.inherit)
                                bordersRight(Color.inherit)
                            }
                        }
                        and(cls(CLASS_CELL_DATA_COLLAPSIBLE)) {
                            and(cls(CLASS_CELL_DATA_COLLAPSED)) {
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
                            and(cls(CLASS_CELL_DATA_COLLAPSED_HOVER)) {
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
            select(cls(CLASS_EDITOR_HIGHLIGHT)) {
                select(cls(CLASS_EDITOR)) {
                    color(highlightColor)
                }
            }
        }
        select(cls(CLASS_HIDDEN)) {
            display(Display.none)
        }
    }

    private fun Style.cssTools() {
        width(100.prc)
        select(cls(CLASS_CELL_CENTER)) {
            textAlign(TextAlign.center)
        }
        select(CLASS_HEADER_LINK) {
            color(Color.inherit)
            textDecoration("none")
        }
        select(cls(CLASS_TABLE_CAPTION)) {
            fontSize(FontSize.large)
            margin(0.px)
        }
        select(cls(CLASS_CELL_LABEL)) {
            textAlign(TextAlign.right)
            fontWeight(FontWeight.normal)
        }
        select(cls(CLASS_EDITOR)) {
            minWidth(95.prc)
            maxWidth(95.prc)
            textAlign(TextAlign.left)
            backgroundColor(backgroundColor)
            color(bodyColor)
            and(cls(CLASS_EDITOR_MIN)) {
                width(8.em)
            }
        }
        select(cls(CLASS_BUTTON)) {
            textAlign(TextAlign.center)
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(cls(CLASS_SMALL)) {
            fontSize(FontSize.small)
            color(secondaryColor)
        }
    }
}
