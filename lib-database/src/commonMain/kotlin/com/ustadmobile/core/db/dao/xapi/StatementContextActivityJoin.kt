package com.ustadmobile.core.db.dao.xapi

/**
 *
 *
 * @param scajFromStatementIdHi the most significant bits of the statement uuid
 * @param scajFromStatementIdLo the least significant bits of the statement uuid
 * @param scajToHash Hash of "scajContextType-otheractivityid" e.g. generates a hash that is unique
 * in the context of the statement
 * @param scajContextType Integer flag based on the contextActivity property e.g. parent, grouping,
 * category, or other
 * @param scajToActivityId the IRI id of the activity that is being referenced
 * @param scajToActivityUid for key that joins to the activity (ActivityEntity.activityUid)
 */
data class StatementContextActivityJoin(
    var scajFromStatementIdHi: Long,
    var scajFromStatementIdLo: Long,
    var scajToHash: Long, // Hash of "scajContextType-otheractivityid"
    var scajContextType: Int = 0,
    var scajToActivityUid: Long = 0,
    var scajToActivityId: String? = null,
) {
    companion object {

        const val TYPE_PARENT = 1

        const val TYPE_GROUPING = 2

        const val TYPE_CATEGORY = 3

        const val TYPE_OTHER = 4

    }
}