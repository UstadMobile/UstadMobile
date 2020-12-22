package com.ustadmobile.core.util

/**
 * Represents an option for the user with a string to display for the ui and optionId to use in
 * event processing
 */
open class IdOption(val description: String, val optionId: Int){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdOption) return false

        if (description != other.description) return false
        if (optionId != other.optionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + optionId
        return result
    }
}
