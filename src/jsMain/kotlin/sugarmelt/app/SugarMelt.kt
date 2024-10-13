package sugarmelt.app

import org.w3c.dom.*
import sugarmelt.Messages
import sugarmelt.api.*
import sugarmelt.css.Stylesheets
import sugarmelt.data.SugarMeltOptions
import sugarmelt.data.info.*
import sugarmelt.data.ui.GeneralUI
import sugarmelt.data.ui.VarUI
import kotlin.js.Date
import kotlin.js.Json

class SugarMelt(private val root: HTMLElement) {
    private val devEnableExperimentalTools = false
    private val isDevelopment: Boolean =
        root.parentDocument.querySelector("meta[name=\"mode\"]")?.getAttribute("content") == "development"

    @Suppress("SpellCheckingInspection")
    private val engines = mapOf(
        "SugarCube1" to "SugarCube.state.active.variables",
        "SugarCube2" to "SugarCube.State.active.variables",
        "wetgame" to "wetgame.state.story.variablesState._globalVariables"
    )
    private val content: HTMLElement
    private val status: HTMLElement
    private val gameTitle: HTMLHeadingElement
    private val toolbar: HTMLDivElement
    private val options: SugarMeltOptions = SugarMeltOptions()
    private var isUpdating: Boolean = false
    private var rootExpression: String = ""
    private lateinit var rootData: RootObjectData
    private fun detectEngines() {
        var tries = engines.size - 1
        evaluate<String>("window.document.title",
            { gameTitle.text(it) },
            { alertError(it, Messages.panel_error(it.message ?: "")) })
        engines.forEach { (key: String, value: String) ->
            evaluate<Json?>("try{${value}}catch(e){null}", { vars ->
                if (rootExpression.isBlank()) {
                    if (vars == null) {
                        tries--
                        if (tries == 0) messageUi("error", Messages.engines_detected_error())
                    } else {
                        messageUi("success", Messages.engines_detected_success(key))
                        onInspect(value, vars)
                    }
                }
            }, {
                tries--
                if (tries == 0) messageUi("error", Messages.engines_detected_error())
            })
        }
    }

    private fun updateAllFields(updateStyle: Boolean) {
        if (rootExpression.isNotBlank()) {
            setFieldLockedValues()
            evaluate<Any>(rootExpression, { vars ->
                callbackUpdateAllFields(vars, updateStyle)
            }) { alertError(it, Messages.panel_error_expression(rootExpression, it.message ?: "")) }
        }
    }

    private fun callbackUpdateAllFields(vars: Any, updateStyle: Boolean) {
        if (!isUpdating) {
            isUpdating = true
            try {
                updateFromRoot(vars.unsafeCast<Json>(), updateStyle)
            } catch (e: Exception) {
                alertError(e, e.message ?: "")
            }
            isUpdating = false
            scheduleUpdate()
        }
    }

    private fun updateFromRoot(vars: Json, updateStyle: Boolean) =
        updateFieldRootObject(rootData, updateStyle, vars)

    private fun updateField(
        parentData: GeneralCollectionData, updateStyle: Boolean,
        path: List<String>, name: String, value: Any?
    ) {
        try {
            when {
                value == null -> updateFieldNull(parentData, name, value)
                jsIsObject(value) -> {
                    val json = value.unsafeCast<Json>()
                    if (path.isEmpty() && name.isEmpty()) updateFieldRootObject(parentData, updateStyle, json)
                    else updateFieldObject(parentData, updateStyle, name, json)
                }

                jsIsArray(value) -> updateFieldArray(parentData, updateStyle, name, value.unsafeCast<Array<Any?>>())
                jsIsMap(value) -> updateFieldMap(parentData, updateStyle, name, value.unsafeCast<Map<String, Any?>>())
                jsTypeOf(value) in supportedTypes -> updateFieldVar(parentData, name, value)
                else -> console.error(Messages.panel_error_type(jsTypeOf(value)))
            }
        } catch (e: Exception) {
            alertError(e, e.message ?: "")
        }
    }

    private fun updateFieldNull(
        parentData: GeneralCollectionData,
        name: String, value: Any?
    ) {
        getData(
            parentData[name] as? NullData?, parentData.path + listOf(name), name, jsTypeOf(value), value
        ) { _, path, dataName, jsType, dataValue ->
            NullData(
                parentData,
                path,
                dataName,
                jsType,
                dataValue,
                createUi(parentData.ui, path, dataName, jsType, isMultiple = false, isEmpty = true)
            )
        }.also {
            addVarToolsHandlers(it)
            parentData[name] = it
        }
    }

    private fun updateFieldRootObject(
        parentData: GeneralCollectionData, updateStyle: Boolean,
        value: Json
    ) {
        if (parentData !is RootObjectData) {
            console.error(Messages.engines_detected_error)
        }
        getData(parentData as RootObjectData, listOf(), "", jsTypeOf(value), value) { _, _, _, _, _ ->
            rootData
        }.also { rootObjectData ->
            addVarToolsHandlers(rootObjectData)
            value.entries.sortedBy { it.first.lowercase() }.forEach { (key, child) ->
                updateField(rootObjectData, updateStyle, listOf(key), key, child)
            }
        }
    }

    private fun updateFieldObject(
        parentData: GeneralCollectionData, updateStyle: Boolean,
        name: String, value: Json
    ) {
        val path = parentData.path + listOf(name)
        getData(
            parentData[name] as? ObjectData?, path, name, jsTypeOf(value), value
        ) { _, dataPath, dataName, jsType, dataValue ->
            ObjectData(
                parentData,
                dataPath,
                dataName,
                jsType,
                createUi(parentData.ui, dataPath, dataName, jsType, true, dataValue.keys.isEmpty())
            )
        }.also { objectData ->
            addVarToolsHandlers(objectData)
            parentData[name] = objectData
            value.entries.sortedBy { it.first.lowercase() }.forEach { (key, child) ->
                updateField(objectData, updateStyle, path + listOf(key), key, child)
            }
        }
    }

    private fun updateFieldMap(
        parentData: GeneralCollectionData, updateStyle: Boolean,
        name: String, value: Map<String, Any?>
    ) {
        val path = parentData.path + listOf(name)
        getData(
            parentData[name] as? ObjectData?, path, name, jsTypeOf(value), value
        ) { _, dataPath, dataName, jsType, dataValue ->
            ObjectData(
                parentData,
                dataPath,
                dataName,
                jsType,
                createUi(parentData.ui, dataPath, dataName, jsType, true, dataValue.isEmpty())
            )
        }.also { objectData ->
            addVarToolsHandlers(objectData)
            parentData[name] = objectData
            value.entries.sortedBy { it.key.lowercase() }.forEach { (key, child) ->
                updateField(objectData, updateStyle, path + listOf(key), key, child)
            }
        }
    }

    private fun updateFieldArray(
        parentData: GeneralCollectionData, updateStyle: Boolean,
        name: String, value: Array<Any?>
    ) {
        val path = parentData.path + listOf(name)
        getData(
            parentData[name] as? ArrayData?, path, name, jsTypeOf(value), value
        ) { _, dataPath, dataName, jsType, dataValue ->
            ArrayData(
                parentData,
                dataPath,
                dataName,
                jsType,
                createUi(parentData.ui, dataPath, dataName, jsType, true, dataValue.isEmpty())
            )
        }.also {
            addVarToolsHandlers(it)
            parentData[name] = it
            value.forEachIndexed { index, item ->
                updateField(it, updateStyle, path + listOf(index.toString()), "$index", item)
            }
        }
    }

    private fun updateFieldVar(
        parentData: GeneralCollectionData,
        name: String, value: Any
    ) {
        getData(parentData[name] as? VarData?, parentData.path + listOf(name), name, jsTypeOf(value), value)
        { _, path, dataName, jsType, dataValue ->
            val tooltip = getTooltipFrom(path)
            val ui = createUi(parentData.ui, path, dataName, jsType, isMultiple = false, isEmpty = false)
            val isBoolean = jsType == "boolean"
            val editor = if (isBoolean) {
                ui.childrenPoint.label("switch") {
                    this.title = tooltip
                }.run {
                    input(getInputType(jsType), toEditor(jsType, dataValue), "editor editor-$jsType") {
                        this.title = tooltip
                        this.checked = toBoolean("boolean", dataValue)
                    }
                }.apply {
                    parentElement?.div("slider round")
                }
            } else {
                ui.childrenPoint.input(
                    getInputType(jsType), toEditor(jsType, dataValue), "editor editor-$jsType"
                ) {
                    this.title = tooltip
                }
            }
            val lock: HTMLInputElement
            ui.childrenPoint.also {
                val lockId = "lock_${getIdFrom(path)}"
                lock = it.input("checkbox", "locked", "editor-lock") {
                    id = lockId
                    title = Messages.panel_field_lock(tooltip)
                }
                it.label {
                    htmlFor = lockId
                }
            }
            VarData(
                parentData, path, dataName, dataValue::class, jsType, dataValue, VarUI(
                    ui.containerPoint, ui.labelPoint, ui.childrenPoint, editor, lock, ui.deleteButton, ui.createButton
                )
            ).also { varData: VarData ->
                varData.ui.editor.apply {
                    onFocus { onBeginEdit(varData) }
                    onBlur { onFinishEdit(varData) }
                    onChange { onFinishEdit(varData) }
                    if (isBoolean) {
                        onClick { onFinishEdit(varData) }
                    }
                }
                varData.ui.lock.onClick {
                    onLock(
                        varData, checked, fromEditor(varData.jsType, varData.ui.editor.value)
                    )
                }
            }
        }.also {
            addVarToolsHandlers(it)
            parentData[name] = it
            updateFieldValue(it, value) { varData, isChanged ->
                if (isChanged) updateFieldStyle(varData, true)
            }
        }
    }

    private fun onBeginEdit(info: VarData) {
        if (!isUpdating && !info.isEditing) {
            updateFieldValue(info) { varData, _ ->
                varData.ui.editor.select()
                updateFieldStyle(varData, false)
                varData.isEditing = true
            }
        }
    }

    private fun onFinishEdit(varData: VarData) {
        if (varData.isEditing) {
            setFieldValue(varData, fromEditor(varData.jsType, varData.ui.editor.value))
            varData.isEditing = false
        }
    }

    private fun updateFieldValue(varData: VarData, callback: (VarData, Boolean) -> Unit = { _, _ -> }) {
        "$rootExpression${varData.expression}".also { expression ->
            evaluate<Any?>(expression, { updateFieldValue(varData, it, callback) }) {
                alertError(it, "Cannot update value for ${varData.path} as ${expression}: ${it.message}")
            }
        }
    }

    private fun updateFieldValue(varData: VarData, newValue: Any?, callback: (VarData, Boolean) -> Unit = { _, _ -> }) {
        val value: Any? = if (varData.isLocked) varData.lockedValue else newValue
        val isChanged = varData.value != value
        if (isChanged && !varData.isEditing) {
            val editorValue = toEditor(varData.jsType, value)
            if (varData.jsType == "boolean") varData.ui.editor.checked = editorValue.toBoolean()
            else varData.ui.editor.value = editorValue
            varData.value = value
        }
        callback(varData, isChanged)
    }

    private fun onLock(varData: VarData, isLocked: Boolean, value: Any?) {
        varData.isLocked = isLocked
        varData.lockedValue = value
        setFieldLockedValue(varData)
    }

    private fun setFieldValue(varData: VarData, value: Any?) {
        updateFieldStyle(varData, false)
        if (varData.value != value) {
            if (varData.isLocked) varData.lockedValue = value
            sendFieldValue(varData, value)
        }
    }

    private fun setFieldLockedValues() {
        if (!isUpdating) walkVars { setFieldLockedValue(it) }
    }

    private fun setFieldLockedValue(varData: VarData) {
        if (!varData.isEditing && varData.isLocked) {
            updateFieldStyle(varData, false)
            sendFieldValue(varData, varData.lockedValue)
        }
    }

    private fun sendFieldValue(varData: VarData, value: Any?) {
        "$rootExpression${varData.expression}=${toExpression(varData.jsType, value)};".also { expr: String ->
            evaluate<Any?>(expr, { varData.value = value }) {
                alertError(it, Messages.panel_error_value(varData.path, expr, it.message ?: ""))
            }
        }
    }

    private fun getInputType(type: String): String = when (type) {
        "Date" -> "datetime-local"
        "boolean" -> "checkbox"
        "number" -> "number"
        "bigint" -> "number"
        else -> "text"
    }

    private fun toEditor(type: String, value: Any?): String = when (type) {
        "Date" -> value.toString()
        "bigint" -> value.toString()
        "number" -> value.toString()
        "boolean" -> value.toString().toBoolean().toString()
        "string" -> value.toString()
        else -> value.toString()
    }

    private fun fromEditor(type: String, value: Any?): Any = when (type) {
        "Date" -> Date(value.toString())
        "bigint" -> value.toString().toInt()
        "number" -> value.toString().toFloat()
        "boolean" -> value.toString().toBoolean()
        "string" -> value.toString()
        else -> value.toString()
    }

    private fun toBoolean(type: String, value: Any?): Boolean = when (type) {
        "Date" -> value.toString().toBoolean()
        "bigint" -> value.toString().toInt() == 0
        "number" -> value.toString().toDouble() == 0.0
        "boolean" -> value.toString().toBoolean()
        "string" -> value.toString() in listOf("true", "1")
        else -> value.toString().toBoolean()
    }

    private fun toExpression(type: String, value: Any?): String = when (type) {
        "Date" -> value.toString()
        "bigint" -> value.toString().toInt().toString()
        "number" -> value.toString().toFloat().toString()
        "string" -> "'${value.toString().replace("'", "\\'")}'"
        "boolean" -> value.toString().toBoolean().toString()
        else -> "'$value'"
    }

    private fun createUi(
        parentUi: GeneralUI,
        path: List<String>,
        name: String,
        jsType: String,
        isMultiple: Boolean,
        isEmpty: Boolean,
    ): GeneralUI = parentUi.childrenPoint.run {
        val table = div(
            makeClasses(
                "table",
                "grid",
                jsType,
                if (isMultiple) "multiple" else "single",
                if (isMultiple && path.isNotEmpty() && name.isNotEmpty()) "collapsible" else "",
                if (isEmpty) "object-empty" else "",
            )
        ) {
            id = "object_${getIdFrom(path)}"
        }
        val tr = table.div("tr row") { }
        var deleteVarControl: HTMLInputElement? = null
        var createVarControl: HTMLInputElement? = null
        val th = tr.div("th cell cell-label clickable") { }.apply {
            label("label $name") { title = "$path $jsType" }.text(getLabelFrom(name))
            if (devEnableExperimentalTools) {
                span("var-controls").apply {
                    if (parentUi != rootData.ui && path.isNotEmpty() && name.isNotEmpty()) {
                        deleteVarControl = input("button", Messages.var_control_delete_button(), "button") {
                            title = Messages.var_control_delete_button_help()
                        }
                    }
                    if (!isMultiple) {
                        createVarControl = input("button", Messages.var_control_create_button(), "button") {
                            title = Messages.var_control_create_button()
                        }
                    }
                }
            }
        }
        val td = tr.div("td cell cell-data collapsible") { }
        th.onClick {
            if (isMultiple && path.isNotEmpty() && name.isNotEmpty()) td.classList %= "collapsed"
        }
        GeneralUI(table, th, td, deleteVarControl, createVarControl)
    }

    private fun addVarToolsHandlers(varData: NullData) {
        addVarToolsHandlersGeneral(varData)
    }

    private fun addVarToolsHandlers(varData: RootObjectData) {
        addVarToolsHandlersGeneralCollection(varData)
    }

    private fun addVarToolsHandlers(varData: ObjectData) {
        addVarToolsHandlersGeneralCollection(varData)
    }

    private fun addVarToolsHandlers(varData: ArrayData) {
        addVarToolsHandlersGeneralCollection(varData)
    }

    private fun addVarToolsHandlers(varData: VarData) {
        addVarToolsHandlersGeneral(varData)
    }

    private fun addVarToolsHandlersGeneral(varData: GeneralData) {
        varData.ui.deleteButton?.onClick {
            if (confirm(Messages.var_control_delete_button_confirm())) {
                "delete $rootExpression${varData.expression};".also { expr: String ->
                    evaluate<Any?>(expr, {
                        updateAllFields(false)
                    }) {
                        alertError(it, Messages.panel_error_delete(varData.path, expr, it.message ?: ""))
                    }
                }
            }
        }
    }

    private fun addVarToolsHandlersGeneralCollection(varData: GeneralCollectionData) {
        addVarToolsHandlersGeneral(varData)
        varData.ui.createButton?.onClick {
            prompt(Messages.var_control_create_button_help()).let { key: String? ->
                if (key != null) {
                    "$rootExpression${varData.expression}['$key']={};".also { expr: String ->
                        evaluate<Any?>(expr, {
                            updateAllFields(false)
                        }) {
                            alertError(it, Messages.panel_error_create(varData.path, expr, it.message ?: ""))
                        }
                    }
                }
            }
        }
    }

    private fun <I : GeneralData, T : Any?> getData(
        varData: I?,
        path: List<String>,
        name: String,
        jsType: String,
        value: T,
        creator: (child: I?, path: List<String>, name: String, jsType: String, value: T) -> I
    ): I =
        (if ((jsIsUndefined(value)) || (varData == null) || (varData is NullData && varData.jsType != jsType) || (varData is VarData && varData.jsType != jsType) || (varData is GeneralCollectionData && varData.size > getValueSize(
                value
            ))
        ) create(varData, path, name, jsType, value, creator) else varData)

    private fun getValueSize(value: Any?): Int = when {
        value == null -> 0
        jsIsArray(value) -> value.unsafeCast<Array<Any?>>().size
        jsIsObject(value) -> value.unsafeCast<Json>().entries.size
        jsIsMap(value) -> value.unsafeCast<Map<String, Any?>>().size
        jsTypeOf(value) in supportedTypes -> 1
        else -> 0
    }

    private fun <I : GeneralData, T : Any?> create(
        child: I?,
        path: List<String>,
        name: String,
        jsType: String,
        value: T,
        creator: (I?, List<String>, String, String, T) -> I
    ): I {
        child?.ui?.containerPoint?.remove()
        return creator(child, path, name, jsType, value)
    }

    private fun onInspect(expression: String, vars: Json) {
        toolbar.apply {
            buttonCollapse()
            buttonExpand()
        }
        rootExpression = expression
        rootData = RootObjectData(jsTypeOf(expression), GeneralUI(content, content, content, null, null))
        callbackUpdateAllFields(vars, false)
        content.parentDocument.window.apply {
            onFocus {
                if (it.currentTarget == this) updateAllFields(true)
            }
            onBlur { clearAllFieldsStyle() }
        }
    }

    private fun updateFieldStyle(varData: VarData, changed: Boolean): HTMLElement = varData.ui.editor.apply {
        if (changed) classList.add("changed") else classList.remove("changed")
    }

    private fun clearAllFieldsStyle() {
        walkVars { it: VarData -> updateFieldStyle(it, false) }
    }

    private fun walkUp(
        varData: GeneralData = rootData, match: (GeneralData) -> Boolean = { true }, action: (GeneralData) -> Unit
    ) {
        if (match(varData)) action(varData)
        varData.parent?.also { walkUp(it, match, action) }
    }

    private fun walkData(
        varData: GeneralData = rootData, match: (GeneralData) -> Boolean = { true }, action: (GeneralData) -> Unit
    ) {
        when (varData) {
            is NullData -> {
                if (match(varData)) action(varData)
            }

            is RootObjectData -> {
                if (match(varData)) action(varData)
                varData.forEach { walkData(it, match, action) }
            }

            is ObjectData -> {
                if (match(varData)) action(varData)
                varData.forEach { walkData(it, match, action) }
            }

            is ArrayData -> {
                if (match(varData)) action(varData)
                varData.forEach { walkData(it, match, action) }
            }

            is VarData -> {
                if (match(varData)) action(varData)
            }
        }
    }

    private fun walkVars(
        varData: GeneralData = rootData, match: (GeneralData) -> Boolean = { true }, action: (VarData) -> Unit
    ): Unit = when (varData) {
        is NullData -> {}
        is RootObjectData -> varData.forEach { walkVars(it, match, action) }
        is ObjectData -> varData.forEach { walkVars(it, match, action) }
        is ArrayData -> varData.forEach { walkVars(it, match, action) }
        is VarData -> if (match(varData)) action(varData) else {
        }
    }

    private fun messageUi(type: String = "", message: String): HTMLElement = status.apply {
        className = (makeClasses("message", if (type.isBlank()) "" else "message-${type}"))
        textContent = message
    }

    private fun getLabelFrom(name: String) = name.split("_").joinToString(" ") {
        it[0].uppercaseChar() + it.substring(1).split(Regex("(?=[A-Z])")).joinToString(" ")
    }

    private fun getTooltipFrom(path: List<String>) = path.joinToString(": ") {
        it[0].uppercaseChar() + it.substring(1).split(Regex("(?=[A-Z])")).joinToString(" ").split('_').joinToString(" ")
    }

    private fun getIdFrom(path: List<String>) = path.joinToString("_")

    private fun HTMLElement.initHeader() {
        getManifest().also {
            div("header") {
                a(it.getString("homepage_url")) {
                    h2("${it["name"]} v.${it["version"]}")
                    target = "_blank"
                }
            }
        }
    }

    private fun HTMLElement.initTools() {
        val tr = div { id = "controls" }.div("table grid tools").div("tr row")
        tr.div("th cell cell-label").label("label").text(Messages.options_interval_label())
        tr.div("td cell cell-data").input("number", options.interval.toString(), "editor") {
            onChange {
                options.interval = value.toInt()
                options.save()
            }
        }.apply {
            options.load {
                value = options.interval.toString()
            }
        }
        tr.div("td cell cell-data small").text(Messages.options_interval_help())
        tr.div("th cell cell-label").label("label").text(Messages.controls_filter_label())
        val elementFilter = tr.div("td cell cell-item").input("text", "", "editor") {
            title = Messages.controls_filter_help()
            onKeyUp { filterSome(value) }
            onFocus { select() }
        }
        tr.div("td cell cell-item small").input("button", Messages.controls_filter_button(), "button") {
            title = Messages.controls_filter_button_help()
            onClick {
                elementFilter.value = ""
                filterSome("")
                elementFilter.focus()
            }
        }
        tr.div("th cell cell-label").label("label").text(Messages.controls_highlight_label())
        val elementHighlight = tr.div("td cell cell-item").input("text", "", "editor") {
            title = Messages.controls_highlight_help()
            onKeyUp { highlightSome(value) }
            onFocus { select() }
        }
        tr.div("td cell cell-item small").input("button", Messages.controls_highlight_button(), "button") {
            title = Messages.controls_highlight_button_help()
            onClick {
                elementHighlight.value = ""
                highlightSome("")
                elementHighlight.focus()
            }
        }
    }

    private fun <T : Element> T.buttonCollapse(): T = apply {
        input("button", Messages.controls_collapse_button(), "button") {
            title = Messages.controls_collapse_button_help()
            onClick { expandCollapseAll(true) }
        }
    }

    private fun <T : Element> T.buttonExpand(): T = apply {
        input("button", Messages.controls_expand_button(), "button") {
            title = Messages.controls_expand_button_help()
            onClick { expandCollapseAll(false) }
        }
    }

    @Suppress("DuplicatedCode")
    private fun highlightSome(pattern: String) {
        walkData {
            val matchingPattern = pattern.isNotBlank() && isMatchingPattern(pattern, it)
            it.ui.labelPoint.also { element ->
                if (matchingPattern) {
                    element.classList.add("highlight")
                } else {
                    element.classList.remove("highlight")
                }
            }
        }
        walkVars {
            val matchingPattern = pattern.isNotBlank() && isMatchingPattern(pattern, it)
            it.ui.editor.also { element ->
                if (matchingPattern) element.classList += "highlight" else element.classList -= "highlight"
            }
        }
    }

    private fun filterSome(pattern: String) {
        walkData {
            val matchingPattern = pattern.isBlank() || isMatchingPattern(pattern, it)
            it.ui.childrenPoint.apply {
                if (matchingPattern) {
                    classList -= "hidden"
                    walkUp { generalData -> generalData.ui.childrenPoint.classList -= "hidden" }
                } else {
                    classList += "hidden"
                }
            }
        }
    }

    private fun isMatchingPattern(pattern: String, varData: GeneralData) =
        pattern.lowercase() in varData.name.lowercase()

    private fun isMatchingPattern(pattern: String, varData: VarData) =
        pattern.lowercase() in varData.name.lowercase() ||
                pattern.lowercase() in toEditor(varData.jsType, varData.value)

    private fun expandCollapseAll(collapse: Boolean) = expandCollapseTo(rootData, collapse)

    private fun expandCollapseTo(varData: GeneralData, collapse: Boolean) {
        walkData(rootData, { isMatchingCollapse(it) }) { expandCollapseAction(it, collapse) }
        walkUp(varData, { isMatchingCollapse(it) }) { expandCollapseAction(it, collapse) }
    }

    private fun isMatchingCollapse(it: GeneralData): Boolean =
        isCollectionData(it) && isCollapsible(it.ui.childrenPoint)

    private fun isCollectionData(it: GeneralData): Boolean = it is GeneralCollectionData && it !is RootObjectData

    private fun expandCollapseAction(generalData: GeneralData, collapse: Boolean) {
        generalData.ui.childrenPoint.apply { if (collapse) classList += ("collapsed") else classList -= ("collapsed") }
    }

    private fun isCollapsible(element: HTMLElement): Boolean = "collapsible" in element.classList

    private fun scheduleUpdate() {
        if (options.isAutomatic) root.parentDocument.window.setTimeout({ updateAllFields(false) }, options.interval)
    }

    private fun destroy() {
        root.removeAll()
    }

    init {
        root.apply {
            addStyle(Stylesheets.stylesheet)
            removeAll()
            div { id = "container" }.apply {
                val table = div("table table-header sticky-top z-1 background-black")
                table.div("tr row").div("td cell").initHeader()
                val row = table.div("tr row")
                gameTitle = row.div("td cell").h1 { id = "game-title" }
                toolbar = row.div("td cell").div { id = "toolbar" }
                row.div("td cell").initTools()
                div("sticky-top-after").also {
                    it.style.paddingTop = "" + table.clientHeight + "px"
                }
                status = div("message")
                content = div { id = "content" }
            }
        }
        detectEngines()
    }

    companion object {
        val supportedTypes: List<String> = listOf("bigint", "boolean", "number", "string", "Date")
        private var instance: SugarMelt? = null
        fun construct() {
            getManifest().apply {
                val iconPath = getJson("icons")?.getString("16") ?: error("no icon")
                val relIconPath =
                    if ("://" in iconPath)
                        iconPath.substringAfter("://").substringAfter('/')
                    else
                        iconPath
                createPanel(
                    Messages.panel_title(),
                    relIconPath,
                    "panel.html",
                    {
                        require(instance == null)
                        instance = SugarMelt(it.document.body ?: error("cannot get body"))
                    },
                    {
                        instance?.destroy()
                        instance = null
                    })
            }
        }
    }
}
