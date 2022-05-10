package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ClazzAssignment

suspend fun ClazzAssignment.getSubmitterUid(db: UmAppDatabase, personUid: Long): Long {
    return if(caGroupUid != 0L){
        val groupMemberPerson = db.courseGroupMemberDao.findByPersonUid(
            caGroupUid, personUid)
        groupMemberPerson?.cgmGroupNumber?.toLong() ?: 0
    }else{
        personUid
    }
}