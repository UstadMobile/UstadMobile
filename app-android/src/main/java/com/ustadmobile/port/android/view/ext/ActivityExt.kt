package com.ustadmobile.port.android.view.ext

import android.app.Activity
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView

/**
 * Convenience methods for edit activities. The title should be either New Foobar where a new item
 * is being created or Edit Foobar when an existing entity is being edited. This method will
 * determine if an existing item was passed in the arguments and set the title accordingly.
 */
fun Activity.setEditActivityTitle(entityTitleId: Int) {
    val entityUid = intent.extras?.getString(UstadView.ARG_ENTITY_UID)?.toLong() ?: 0L
    val entityJsonStr = intent.extras?.getString(UstadEditView.ARG_ENTITY_JSON)
    if(entityUid != 0L || entityJsonStr != null){
        title = getString(R.string.edit_entity, getString(entityTitleId))
    }else {
        title = getString(R.string.new_entity, getString(entityTitleId))
    }
}
