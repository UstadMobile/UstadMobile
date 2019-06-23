package com.ustadmobile.port.android.umeditor


import java.io.Serializable

/**
 * Class which represents the content formatting object, this handles the
 * content styling on the active editor.
 *
 * @author kileha3
 */
class UmFormat : Serializable {

    /**
     * Get formatting icon
     * @return resource drawable id
     */
    /**
     * Set formatting Icon
     * @param formatIcon resource drawable id.
     */
    var formatIcon: Int = 0

    /**
     * Get formatting executable format command
     * @return executable command
     */
    /**
     * Set formatting executable command
     * @param formatCommand command to be executed.
     *
     * @see com.ustadmobile.core.view.ContentPreviewView for the list of all possible commands.
     */
    var formatCommand: String? = null

    /**
     * Check if the formatting has been activated
     * @return formatting state.
     */
    /**
     * Change formatting state
     * @param active True if it is activated otherwise false.
     */
    var active: Boolean = false

    /**
     * Get type of the formatting (Text / Paragraph)
     * @return Type of the formatting.
     */
    /**
     * Set type of content formatting (Text / Paragraph)
     * @param formatType formatting type.
     */
    var formatType: Int = 0

    var formatTitle: Int = 0

    var formatId = 0

    /**
     * Constructor which will be used to create an instance of content formatting.
     * @param formatIcon Formatting icon
     * @param formatCommand Formatting executable command
     * @param active Flag to indicate if the format is active or not
     * @param formatType Flag which shows which type of the formatting is.
     */
    constructor(formatIcon: Int, formatCommand: String, active: Boolean, formatType: Int) {
        this.formatIcon = formatIcon
        this.formatCommand = formatCommand
        this.active = active
        this.formatType = formatType
    }

    /**
     * Constructor which will be used to create an instance of content formatting.
     * @param formatIcon Formatting icon
     * @param formatCommand Formatting executable command
     * @param active Flag to indicate if the format is active or not
     * @param formatType Flag which shows which type of the formatting is.
     * @param formatId formatting id.
     */
    constructor(formatIcon: Int, formatCommand: String, active: Boolean,
                formatType: Int, formatId: Int) {
        this.formatIcon = formatIcon
        this.formatCommand = formatCommand
        this.active = active
        this.formatType = formatType
        this.formatId = formatId
    }

    constructor(formatIcon: Int, formatCommand: String, active: Boolean,
                formatType: Int, formatId: Int, formatTitle: Int) {
        this.formatIcon = formatIcon
        this.formatCommand = formatCommand
        this.active = active
        this.formatType = formatType
        this.formatId = formatId
        this.formatTitle = formatTitle
    }
}
