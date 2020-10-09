package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.GroupLearningSession

@UmDao
@UmRepository
@Dao
abstract class GroupLearningSessionDao : BaseDao<GroupLearningSession> {



}
