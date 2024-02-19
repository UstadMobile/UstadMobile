package com.ustadmobile.core.viewmodel.deleteditem

import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DeletedItem
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR
val DeletedItem.delItemContentTypeStringResource: StringResource?
    get() = when {
        delItemEntityTable == ContentEntryParentChildJoin.TABLE_ID && delItemIsFolder ->
            MR.strings.folder
        delItemEntityTable == ContentEntryParentChildJoin.TABLE_ID -> MR.strings.content
        else -> null
    }
