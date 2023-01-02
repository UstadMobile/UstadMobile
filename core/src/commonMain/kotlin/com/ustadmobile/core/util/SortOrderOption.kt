package com.ustadmobile.core.util

data class SortOrderOption(
    /**
     * MessageID constant for the field to be sorted by e.g. MessageID.firstNames
     */
    val fieldMessageId: Int,

    /**
     * The flag value that is used by the DAO for this sort option e.g. PersonDao.SORT_FIRST_NAME_ASC
     */
    val flag: Int,

    /**
     * The order
     *
     * Ascending = true, descending = false
     */
    val order: Boolean
)