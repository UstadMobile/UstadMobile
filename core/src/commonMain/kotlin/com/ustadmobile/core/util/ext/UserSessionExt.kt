package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.UserSession

fun UserSession.isTemporary() : Boolean {
    return (usSessionType and UserSession.TYPE_TEMP_LOCAL) == UserSession.TYPE_TEMP_LOCAL
}
