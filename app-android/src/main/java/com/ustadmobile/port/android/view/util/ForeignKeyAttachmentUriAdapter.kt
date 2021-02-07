package com.ustadmobile.port.android.view.util

import com.ustadmobile.core.db.UmAppDatabase

/**
 * This adapter helps when in situations where it is desired to display attachment data together
 * with other data and avoid a situation where loading the attachment data delays the display of
 * the other data.
 *
 * For example where there is a list of of people and an attachment entity that contains the profile,
 * picture it would be desired to show the person's name etc. without waiting for the picture in the
 * attachment data to load.
 *
 * In this situation attachment data is typically connected using a foreign key.
 */
interface ForeignKeyAttachmentUriAdapter {

    /**
     * This function is responsible to get an Android Uri for the given foreign key. This would
     * normally be done using a database query. The query should use coroutines (e.g. suspended).
     *
     * Normally something using this would call this function twice: the first time on the db, the
     * second time on the repo. The repo is included as an additional parameter because it is required
     * to resolve the attachment's Uri.
     *
     * @param foreignKey the foreign key to lookup
     * @param dbToUse the database against which to run a query (e.g. the db, then the repo)
     * @param repo an instance of the database that is always the repo. This is used to resolve the
     * attachment uri.
     */
    suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String?

}
