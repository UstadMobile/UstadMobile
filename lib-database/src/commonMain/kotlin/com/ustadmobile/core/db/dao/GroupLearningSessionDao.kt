package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.GroupLearningSession

@Repository
@Dao
abstract class GroupLearningSessionDao : BaseDao<GroupLearningSession> {



}
