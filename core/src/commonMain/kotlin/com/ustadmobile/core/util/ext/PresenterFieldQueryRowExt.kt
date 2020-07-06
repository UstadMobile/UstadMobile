package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.PresenterFieldQueryRow
import com.ustadmobile.lib.db.entities.PresenterFieldRow

/**
 * The query used to find PresenterFieldQueryRow will return duplicate rows in case of a dropdown
 * with multiple options. This function merges the rows together to ignore duplicates.
 */
fun List<PresenterFieldQueryRow>.toPresenterFieldRows(): List<PresenterFieldRow> {
    return groupBy { it.presenterField?.personDetailPresenterFieldUid }
            .map {
                PresenterFieldRow(
                    it.value.first().presenterField,
                    it.value.first().customField,
                    it.value.first().customFieldValue,
                    it.value.filter { it.customFieldValueOption != null }
                            .map { it.customFieldValueOption!! })
            }
}