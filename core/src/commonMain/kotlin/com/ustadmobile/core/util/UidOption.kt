package com.ustadmobile.core.util

class UidOption(val description: String, val uidOption: Long) {

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UidOption) return false

        if (description != other.description) return false
        if (uidOption != other.uidOption) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + uidOption.hashCode()
        return result
    }


}