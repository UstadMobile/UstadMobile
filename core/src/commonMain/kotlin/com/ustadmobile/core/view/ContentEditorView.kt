package com.ustadmobile.core.view

interface ContentEditorView: UstadViewWithProgressDialog {

    /**
     * Set bold formatting on selected/focused content
     */
    fun setContentBold()

    /**
     * Set italic formatting on selected/focused content
     */
    fun setContentItalic()

    /**
     * Set underline formatting on selected/focused content
     */
    fun setContentUnderlined()

    /**
     * Set strike through formatting on selected/focused content
     */
    fun setContentStrikeThrough()

    /**
     * Set font size on selected/focused content
     * @param fontSize font size to be set in pt unit.
     */
    fun setContentFontSize(fontSize: String)

    /**
     * Make selected/focused content a superscript
     */
    fun setContentSuperscript()

    /**
     * Make selected/focused content a subscript
     */
    fun setContentSubScript()

    /**
     * Justify your selected/focused paragraph content
     */
    fun setContentJustified()

    /**
     * Center align your selected/focused paragraph content
     */
    fun setContentCenterAlign()

    /**
     * Left align your selected/focused paragraph content
     */
    fun setContentLeftAlign()

    /**
     * Right align your selected/focused paragraph content
     */
    fun setContentRightAlign()

    /**
     * Insert ordered list on cursor position
     */
    fun setContentOrderedList()

    /**
     * Insert unordered list on cursor position
     */
    fun setContentUnOrderList()

    /**
     * Increase indentation of the content
     */
    fun setContentIncreaseIndent()

    /**
     * Decrease indentation of the content
     */
    fun setContentDecreaseIndent()

    /**
     * Redo changes that has been deleted/ undo from the content editing area
     */
    fun setContentRedo()

    /**
     * Undo changed which has been introduced to the content editing area.
     */
    fun setContentUndo()

    /**
     * Change language directionality (LTR / RTL)
     * @param command Tinymce command for language directionality.
     */
    fun setContentTextDirection(command: String)

    /**
     * Insert multi-choice question template on the content editing area
     */
    fun insertMultipleChoiceQuestion()

    /**
     * Insert fill in the blanks template on the content editing area.
     */
    fun insertFillTheBlanksQuestion()

    /**
     * Insert content to the editor
     * @param content content to be added to the editor
     */
    fun insertContent(content: String)

    /**
     * Select all content added to to editor
     */
    fun selectAllContent()

    /**
     * Handle all document pages
     */
    fun loadPage(pageUrl: String)

    /**
     * Reove all content from the editable section
     */
    fun clearEditableSection()

    /**
     * Focus next link
     */
    fun focusNextLink()

    fun showErrorMessage(message: String)

    fun saveContent()

    fun cleanUnUsedResources()

    companion object {

        const val VIEW_NAME = "ContentEditor"

        const val CONTENT_STORAGE_OPTION = "content_storage_option"

        /**
         * List of all available text formatting.
         */

        const val TEXT_FORMAT_TYPE_BOLD = "Bold"

        const val TEXT_FORMAT_TYPE_UNDERLINE = "Underline"

        const val TEXT_FORMAT_TYPE_ITALIC = "Italic"

        const val TEXT_FORMAT_TYPE_STRIKE = "Strikethrough"

        const val TEXT_FORMAT_TYPE_FONT = "FontSize"

        const val TEXT_FORMAT_TYPE_SUP = "Superscript"

        const val TEXT_FORMAT_TYPE_SUB = "Subscript"

        /**
         * List of all available paragraph formatting.
         */
        const val PARAGRAPH_FORMAT_ALIGN_CENTER = "JustifyCenter"

        const val PARAGRAPH_FORMAT_ALIGN_LEFT = "JustifyLeft"

        const val PARAGRAPH_FORMAT_ALIGN_RIGHT = "JustifyRight"

        const val PARAGRAPH_FORMAT_ALIGN_JUSTIFY = "JustifyFull"

        const val PARAGRAPH_FORMAT_LIST_ORDERED = "InsertOrderedList"

        const val PARAGRAPH_FORMAT_LIST_UNORDERED = "InsertUnorderedList"

        const val PARAGRAPH_FORMAT_INDENT_INCREASE = "Indent"

        const val PARAGRAPH_FORMAT_INDENT_DECREASE = "Outdent"

        /**
         * List of all actions to be taken from the editing screen.
         */

        const val ACTION_REDO = "Redo"

        const val ACTION_UNDO = "Undo"

        const val ACTION_TEXT_DIRECTION_LTR = "mceDirectionLTR"

        const val ACTION_TEXT_DIRECTION_RTL = "mceDirectionRTL"

        const val ACTION_INSERT_CONTENT = "insertContent"

        const val ACTION_FOCUS_NEXT_LINK = "focusOnNextLink"

        /**
         * List of callback received from JS to control native behavior
         */
        const val ACTION_ENABLE_EDITING = "onEditingModeOn"

        const val ACTION_CONTROLS_ACTIVATED = "onActiveControlCheck"

        const val ACTION_SAVE_CONTENT = "onSaveContent"

        const val ACTION_SELECT_ALL = "selectAll"

        const val ACTION_CLEAR_ALL = "clearAll"

        const val ACTION_CONTENT_CUT = "onContentCut"

        const val ACTION_EDITOR_INITIALIZED = "onCreate"

        const val ACTION_PAGE_LOADED = "onWindowLoad"

        const val ACTION_LINK_CHECK = "onLinkPropRequested"

        /**
         * Question templates insert command tag.
         */
        const val CONTENT_INSERT_MULTIPLE_CHOICE_QN = "MultipleChoice"

        const val CONTENT_INSERT_FILL_THE_BLANKS_QN = "FillTheBlanks"

        /**
         * List of editor core resources.
         */

        const val RESOURCE_JS_USTAD_WIDGET = "UmWidgetManager.js"

        const val RESOURCE_JS_USTAD_EDITOR = "UmContentCore.js"

        const val RESOURCE_JS_TINYMCE = "tinymce.min.js"

        const val RESOURCE_BLANK_DOCUMENT = "umEditorBlankDoc.zip"
    }

}
