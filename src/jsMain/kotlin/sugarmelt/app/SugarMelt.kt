package sugarmelt.app

import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.config.I18n4kConfig
import de.comahe.i18n4k.config.I18n4kConfigDefault
import de.comahe.i18n4k.getDisplayNameInLocale
import de.comahe.i18n4k.i18n4k
import de.comahe.i18n4k.language
import org.w3c.dom.*
import sugarmelt.Messages
import sugarmelt.api.*
import sugarmelt.css.StylesheetFlags
import sugarmelt.css.StylesheetMain
import sugarmelt.css.StylesheetMain.CLASS_BUTTON
import sugarmelt.css.StylesheetMain.CLASS_CELL
import sugarmelt.css.StylesheetMain.CLASS_CELL_DATA
import sugarmelt.css.StylesheetMain.CLASS_CELL_DATA_COLLAPSED
import sugarmelt.css.StylesheetMain.CLASS_CELL_DATA_COLLAPSIBLE
import sugarmelt.css.StylesheetMain.CLASS_CELL_DATA_MULTIPLE
import sugarmelt.css.StylesheetMain.CLASS_CELL_DATA_SINGLE
import sugarmelt.css.StylesheetMain.CLASS_CELL_LABEL
import sugarmelt.css.StylesheetMain.CLASS_CLICKABLE
import sugarmelt.css.StylesheetMain.CLASS_CONTAINER_BACKGROUND
import sugarmelt.css.StylesheetMain.CLASS_EDITOR
import sugarmelt.css.StylesheetMain.CLASS_EDITOR_CHANGED
import sugarmelt.css.StylesheetMain.CLASS_EDITOR_HIGHLIGHT
import sugarmelt.css.StylesheetMain.CLASS_EDITOR_LOCK
import sugarmelt.css.StylesheetMain.CLASS_EDITOR__TYPE
import sugarmelt.css.StylesheetMain.CLASS_GRID
import sugarmelt.css.StylesheetMain.CLASS_GRID_ROW
import sugarmelt.css.StylesheetMain.CLASS_GRID_ROW_CELL
import sugarmelt.css.StylesheetMain.CLASS_GRID_ROW_CELL_ITEM
import sugarmelt.css.StylesheetMain.CLASS_GRID_ROW_CELL_LABEL
import sugarmelt.css.StylesheetMain.CLASS_GRID_VAR_CONTROLS
import sugarmelt.css.StylesheetMain.CLASS_HEADER
import sugarmelt.css.StylesheetMain.CLASS_HIDDEN
import sugarmelt.css.StylesheetMain.CLASS_I18N
import sugarmelt.css.StylesheetMain.CLASS_LABEL
import sugarmelt.css.StylesheetMain.CLASS_MESSAGE
import sugarmelt.css.StylesheetMain.CLASS_MESSAGE__TYPE
import sugarmelt.css.StylesheetMain.CLASS_OBJECT_EMPTY
import sugarmelt.css.StylesheetMain.CLASS_SMALL
import sugarmelt.css.StylesheetMain.CLASS_STICKY_TOP
import sugarmelt.css.StylesheetMain.CLASS_STICKY_TOP_AFTER
import sugarmelt.css.StylesheetMain.CLASS_SWITCH
import sugarmelt.css.StylesheetMain.CLASS_SWITCH_SLIDER
import sugarmelt.css.StylesheetMain.CLASS_SWITCH_SLIDER_ROUND
import sugarmelt.css.StylesheetMain.CLASS_TABLE
import sugarmelt.css.StylesheetMain.CLASS_TABLE_HEADER
import sugarmelt.css.StylesheetMain.CLASS_TD
import sugarmelt.css.StylesheetMain.CLASS_TH
import sugarmelt.css.StylesheetMain.CLASS_TOGGLE
import sugarmelt.css.StylesheetMain.CLASS_TOOLS
import sugarmelt.css.StylesheetMain.CLASS_TR
import sugarmelt.css.StylesheetMain.CLASS_Z_1
import sugarmelt.css.StylesheetMain.ID_CONTAINER
import sugarmelt.css.StylesheetMain.ID_CONTENT
import sugarmelt.css.StylesheetMain.ID_CONTROLS
import sugarmelt.css.StylesheetMain.ID_GAME_TITLE
import sugarmelt.css.StylesheetMain.ID_TOOLBAR
import sugarmelt.data.SugarMeltOptions
import sugarmelt.data.flag
import sugarmelt.data.getCountryCode
import sugarmelt.data.info.*
import sugarmelt.data.ui.GeneralUI
import sugarmelt.data.ui.VarUI
import kotlin.js.Date
import kotlin.js.Json

class SugarMelt(
    private val root: HTMLElement,
    private val options: SugarMeltOptions,
    private val i18n4kConfig: I18n4kConfigDefault
) {
    private val devEnableExperimentalTools = false

    @Suppress("SpellCheckingInspection")
    private val engines = mapOf(
        "SugarCube1" to "SugarCube.state.active.variables",
        "SugarCube2" to "SugarCube.State.active.variables",
        "wetgame" to "wetgame.state.story.variablesState._globalVariables"
    )

    private enum class MessageType(val value: String) {
        SUCCESS("success"),
        ERROR("error"),
    }

    private val content: HTMLElement
    private val status: HTMLElement
    private val gameTitle: HTMLHeadingElement
    private val toolbar: HTMLDivElement
    private var isUpdating: Boolean = false
    private var rootExpression: String = ""
    private lateinit var rootData: RootObjectData
    private fun detectEngines() {
        var tries = engines.size - 1
        evaluate<String>(
            "window.document.title",
            { gameTitle.text(it) },
            { alertError(it, Messages.panel_error(it.message ?: "")) })
        engines.forEach { (key: String, value: String) ->
            evaluate<Json?>("try{${value}}catch(e){null}", { vars ->
                if (rootExpression.isBlank()) {
                    if (vars == null) {
                        tries--
                        if (tries == 0) messageUi(MessageType.ERROR, Messages.engines_detected_error())
                    } else {
                        messageUi(MessageType.SUCCESS, Messages.engines_detected_success(key))
                        onInspect(value, vars)
                    }
                }
            }, {
                tries--
                if (tries == 0) messageUi(MessageType.ERROR, Messages.engines_detected_error())
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
            value.entries.sortedBy { (key) -> key.lowercase() }.forEach { (key, child) ->
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
            value.entries.sortedBy { (key) -> key.lowercase() }.forEach { (key, child) ->
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
            value.entries.sortedBy { (key) -> key.lowercase() }.forEach { (key, child) ->
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
                ui.childrenPoint.label(CLASS_SWITCH) {
                    this.title = tooltip
                }.run {
                    input(
                        getInputType(jsType),
                        toEditor(jsType, dataValue),
                        "$CLASS_EDITOR ${CLASS_EDITOR__TYPE}$jsType"
                    ) {
                        this.title = tooltip
                        this.checked = toBoolean("boolean", dataValue)
                    }
                }.apply {
                    parentElement?.div("$CLASS_SWITCH_SLIDER $CLASS_SWITCH_SLIDER_ROUND")
                }
            } else {
                ui.childrenPoint.input(
                    getInputType(jsType), toEditor(jsType, dataValue), "$CLASS_EDITOR ${CLASS_EDITOR__TYPE}$jsType"
                ) {
                    this.title = tooltip
                }
            }
            val lock: HTMLInputElement
            ui.childrenPoint.also {
                val lockId = "lock_${getIdFrom(path)}"
                lock = it.input("checkbox", "locked", CLASS_EDITOR_LOCK) {
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
            varData.isEditing = false
            setFieldValue(varData, fromEditor(varData.jsType, varData.ui.editor.value))
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
                CLASS_TABLE,
                CLASS_GRID,
                jsType,
                if (isMultiple) CLASS_CELL_DATA_MULTIPLE else CLASS_CELL_DATA_SINGLE,
                if (isMultiple && path.isNotEmpty() && name.isNotEmpty()) CLASS_CELL_DATA_COLLAPSIBLE else "",
                if (isEmpty) CLASS_OBJECT_EMPTY else "",
            )
        ) {
            id = "object_${getIdFrom(path)}"
        }
        val tr = table.div("$CLASS_TR $CLASS_GRID_ROW") { }
        var deleteVarControl: HTMLInputElement? = null
        var createVarControl: HTMLInputElement? = null
        val th = tr.div("$CLASS_TH $CLASS_GRID_ROW_CELL $CLASS_GRID_ROW_CELL_LABEL $CLASS_CLICKABLE") { }.apply {
            label("label $name") { title = "$path $jsType" }.text(getLabelFrom(name))
            if (devEnableExperimentalTools) {
                span(CLASS_GRID_VAR_CONTROLS).apply {
                    if (parentUi != rootData.ui && path.isNotEmpty() && name.isNotEmpty()) {
                        deleteVarControl = input(CLASS_BUTTON, Messages.var_control_delete_button(), CLASS_BUTTON) {
                            title = Messages.var_control_delete_button_help()
                        }
                    }
                    if (!isMultiple) {
                        createVarControl = input(CLASS_BUTTON, Messages.var_control_create_button(), CLASS_BUTTON) {
                            title = Messages.var_control_create_button()
                        }
                    }
                }
            }
        }
        val td = tr.div("$CLASS_TD $CLASS_CELL $CLASS_CELL_DATA $CLASS_CELL_DATA_COLLAPSIBLE") { }
        th.onClick {
            if (isMultiple && path.isNotEmpty() && name.isNotEmpty()) {
                td.classList %= CLASS_CELL_DATA_COLLAPSED
                if (td.classList.contains(CLASS_CELL_DATA_COLLAPSED)) {
                    val bb = td.getBoundingClientRect()
                    val headerHeight = td.getElementByClass<Element>(CLASS_TABLE_HEADER).clientHeight
                    if (bb.top > td.parentWindow.innerHeight || bb.bottom < headerHeight)
                        td.parentWindow.scrollTo(0.0, td.offsetTop - headerHeight.toDouble())
                }
            }
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

    private fun <I : GeneralData, T> getData(
        varData: I?,
        path: List<String>,
        name: String,
        jsType: String,
        value: T,
        creator: (child: I?, path: List<String>, name: String, jsType: String, value: T) -> I
    ): I =
        (if (
            (jsIsUndefined(value)) ||
            (varData == null) ||
            (varData is NullData && varData.jsType != jsType) ||
            (varData is VarData && varData.jsType != jsType) ||
            (varData is GeneralCollectionData && varData.size > getValueSize(value))
        )
            create(varData, path, name, jsType, value, creator) else varData)

    private fun getValueSize(value: Any?): Int = when {
        value == null -> 0
        jsIsArray(value) -> value.unsafeCast<Array<Any?>>().size
        jsIsObject(value) -> value.unsafeCast<Json>().entries.size
        jsIsMap(value) -> value.unsafeCast<Map<String, Any?>>().size
        jsTypeOf(value) in supportedTypes -> 1
        else -> 0
    }

    private fun <I : GeneralData, T> create(
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
        if (changed) classList += CLASS_EDITOR_CHANGED else classList -= CLASS_EDITOR_CHANGED
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

    private fun messageUi(type: MessageType? = null, message: String): HTMLElement = status.apply {
        className = (makeClasses(CLASS_MESSAGE, if (type == null) "" else "${CLASS_MESSAGE__TYPE}${type.value}"))
        textContent = message
    }

    private fun getLabelFrom(name: String) = name.split("_").joinToString(" ") {
        it[0].uppercaseChar() + it.substring(1).split(Regex("(?=[A-Z])")).joinToString(" ")
    }

    private fun getTooltipFrom(path: List<String>) = path.joinToString(": ") {
        it[0].uppercaseChar() + it.substring(1).split(Regex("(?=[A-Z])")).joinToString(" ").split('_').joinToString(" ")
    }

    private fun getIdFrom(path: List<String>) = path.joinToString("_")

    private fun HTMLElement.initTools() {
        val tr = div { id = ID_CONTROLS }.div("$CLASS_TABLE $CLASS_GRID $CLASS_TOOLS").div("$CLASS_TR $CLASS_GRID_ROW")
        tr.div("$CLASS_TH $CLASS_CELL $CLASS_CELL_LABEL").label(CLASS_LABEL).text(Messages.options_interval_label())
        tr.div("$CLASS_TD $CLASS_CELL $CLASS_CELL_DATA").input("number", options.interval.toString(), CLASS_EDITOR) {
            onChange {
                options.interval = value.toInt()
                options.save()
            }
        }
        tr.div("$CLASS_TD $CLASS_CELL $CLASS_CELL_DATA $CLASS_SMALL").text(Messages.options_interval_help())
        tr.div("$CLASS_TH $CLASS_CELL $CLASS_CELL_LABEL").label(CLASS_LABEL).text(Messages.controls_filter_label())
        val elementFilter = tr.div("$CLASS_TD $CLASS_CELL $CLASS_GRID_ROW_CELL_ITEM").input("text", "", CLASS_EDITOR) {
            title = Messages.controls_filter_help()
            onKeyUp { filterSome(value) }
            onFocus { select() }
        }
        tr.div("$CLASS_TD $CLASS_CELL $CLASS_GRID_ROW_CELL_ITEM $CLASS_SMALL")
            .input("button", Messages.controls_filter_button(), CLASS_BUTTON) {
                title = Messages.controls_filter_button_help()
                onClick {
                    elementFilter.value = ""
                    filterSome("")
                    elementFilter.focus()
                }
            }
        tr.div("$CLASS_TH $CLASS_CELL $CLASS_CELL_LABEL").label(CLASS_LABEL).text(Messages.controls_highlight_label())
        val elementHighlight =
            tr.div("$CLASS_TD $CLASS_CELL $CLASS_GRID_ROW_CELL_ITEM").input("text", "", CLASS_EDITOR) {
                title = Messages.controls_highlight_help()
                onKeyUp { highlightSome(value) }
                onFocus { select() }
            }
        tr.div("$CLASS_TD $CLASS_CELL $CLASS_GRID_ROW_CELL_ITEM $CLASS_SMALL")
            .input("button", Messages.controls_highlight_button(), CLASS_BUTTON) {
                title = Messages.controls_highlight_button_help()
                onClick {
                    elementHighlight.value = ""
                    highlightSome("")
                    elementHighlight.focus()
                }
            }
    }

    private fun <T : Element> T.buttonCollapse(): T = apply {
        input("button", Messages.controls_collapse_button(), CLASS_BUTTON) {
            title = Messages.controls_collapse_button_help()
            onClick { expandCollapseAll(true) }
        }
    }

    private fun <T : Element> T.buttonExpand(): T = apply {
        input("button", Messages.controls_expand_button(), CLASS_BUTTON) {
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
                    element.classList += CLASS_EDITOR_HIGHLIGHT
                } else {
                    element.classList -= CLASS_EDITOR_HIGHLIGHT
                }
            }
        }
        walkVars {
            val matchingPattern = pattern.isNotBlank() && isMatchingPattern(pattern, it)
            it.ui.editor.also { element ->
                if (matchingPattern) element.classList += CLASS_EDITOR_HIGHLIGHT else element.classList -= CLASS_EDITOR_HIGHLIGHT
            }
        }
    }

    private fun filterSome(pattern: String) {
        walkData {
            val matchingPattern = pattern.isBlank() || isMatchingPattern(pattern, it) ||
                    (it is VarData && isMatchingPattern(pattern, it))
            it.ui.labelPoint.apply {
                if (matchingPattern) classList -= CLASS_HIDDEN else classList += CLASS_HIDDEN
            }
            it.ui.childrenPoint.apply {
                if (matchingPattern) classList -= CLASS_HIDDEN else classList += CLASS_HIDDEN
            }
            if (matchingPattern) {
                walkUp(it) { ancestor -> ancestor.ui.childrenPoint.classList -= CLASS_HIDDEN }
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
        generalData.ui.childrenPoint.apply { if (collapse) classList += (CLASS_CELL_DATA_COLLAPSED) else classList -= (CLASS_CELL_DATA_COLLAPSED) }
    }

    private fun isCollapsible(element: HTMLElement): Boolean = CLASS_CELL_DATA_COLLAPSIBLE in element.classList

    private fun scheduleUpdate() {
        if (options.isAutomatic) root.parentDocument.window.setTimeout({ updateAllFields(false) }, options.interval)
    }

    private fun destroy() {
        root.removeAll()
    }

    init {
        root.apply {
            addStyle(StylesheetMain.stylesheet)
            addStyle(StylesheetFlags.stylesheet)
            removeAll()
            div { id = ID_CONTAINER }.apply {
                val table =
                    div("$CLASS_TABLE $CLASS_TABLE_HEADER $CLASS_STICKY_TOP $CLASS_Z_1 $CLASS_CONTAINER_BACKGROUND")
                table.div("$CLASS_TR $CLASS_GRID_ROW").also { row ->
                    row.div("$CLASS_TD $CLASS_CELL").div(CLASS_HEADER) {
                        h2 {
                            getManifest().also {
                                a(it.getString("homepage_url")) {
                                    target = "_blank"
                                    text("${it["name"]} v.${it["version"]}")
                                }
                            }
                        }
                    }
                    row.div("$CLASS_TD $CLASS_CELL").div(CLASS_HEADER).span(CLASS_I18N).i18n()
                    status = row.div("$CLASS_TD $CLASS_CELL").div(CLASS_HEADER).div(CLASS_MESSAGE)
                }
                table.div("$CLASS_TR $CLASS_GRID_ROW").also {
                    gameTitle = it.div("$CLASS_TD $CLASS_CELL").h1 { id = ID_GAME_TITLE }
                    toolbar = it.div("$CLASS_TD $CLASS_CELL").div { id = ID_TOOLBAR }
                    it.div("$CLASS_TD $CLASS_CELL").initTools()
                }
                div(CLASS_STICKY_TOP_AFTER) {
                    style.paddingTop = "${table.clientHeight}px"
                }
                content = div { id = ID_CONTENT }
            }
        }
        detectEngines()
    }

    private fun HTMLElement.i18n() {
        div(CLASS_TOGGLE) {
            SugarMeltLocales.locales.forEach {
                input("radio") {
                    id = it.toString()
                    name = "languages"
                    value = it.language
                    checked = it.language == i18n4kConfig.locale.getLanguage()
                    onClick {
                        i18n4kConfig.locale = Locale(this.value)
                        this@SugarMelt.options.language = Locale(this.value)
                        this@SugarMelt.options.save()
                    }
                }
                label("fi fi-${getCountryCode(it).lowercase()} fis") {
                    htmlFor = it.toString()
                    flag(it)
                    title = it.getDisplayNameInLocale()
                }
            }
        }
    }

    companion object {
        val supportedTypes: List<String> = listOf("bigint", "boolean", "number", "string", "Date")
        
        private var instance: SugarMelt? = null
        fun construct(options: SugarMeltOptions, i18n4kConfig: I18n4kConfigDefault) {
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
                        instance = SugarMelt(it.document.body ?: error("cannot get body"), options, i18n4kConfig)
                    },
                    {
                        instance?.destroy()
                        instance = null
                    })
            }
        }

        fun Locale.languageOr(i18n4kConfig: I18n4kConfig = i18n4k): Locale =
            takeIf { it in SugarMeltLocales.locales } ?: i18n4kConfig.locale
    }
}
