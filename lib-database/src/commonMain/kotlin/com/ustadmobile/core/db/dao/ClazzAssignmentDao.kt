package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignment


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {


    companion object{

        const val SORT_DEADLINE_ASC = 1

        const val SORT_DEADLINE_DESC = 2

        const val SORT_TITLE_ASC = 3

        const val SORT_TITLE_DESC = 4

        const val SORT_SCORE_ASC = 5

        const val SORT_SCORE_DESC = 6

    }

}