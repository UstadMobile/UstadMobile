package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD

/**
 *
 */
fun PersonDetailPresenterField.isCoreEntityField(): Boolean
        = this.fieldType == TYPE_FIELD && this.fieldUid < 1000

fun PersonDetailPresenterField.isCustomField(): Boolean
        = this.fieldType == TYPE_FIELD && this.fieldUid > 1000
