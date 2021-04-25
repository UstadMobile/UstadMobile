package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin
import com.ustadmobile.lib.db.entities.ClazzWorkContentJoin

@Dao
@Repository
abstract class ClazzAssignmentContentJoinDao : BaseDao<ClazzWorkContentJoin>,
        OneToManyJoinDao<ClazzAssignmentContentJoin> {



}