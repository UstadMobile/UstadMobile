package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionResponse

@UmDao
@UmRepository
@Dao
abstract class ClazzWorkQuestionResponseDao : BaseDao<ClazzWorkQuestionResponse> {

    @Query("SELECT * FROM ClazzWorkQuestionResponse " +
            "WHERE clazzWorkQuestionResponseUid = :uid " +
            " AND CAST(clazzWorkQuestionResponseInactive AS INTEGER) = 0")
    abstract fun findByUidAsync(uid: Long): ClazzWorkQuestionResponse?

    @Query(FIND_BY_QUESTIONUID_AND_CLAZZ_MEMBER_UID)
    abstract fun findByQuestionUidAndClazzMemberUidAsync(uid: Long,
                    clazzMemberUid: Long):List<ClazzWorkQuestionResponse>

    @Update
    abstract suspend fun updateAsync(entity: ClazzWorkQuestionResponse) : Int


    companion object{

        const val FIND_BY_QUESTIONUID = """
            SELECT * FROM ClazzWorkQuestionResponse WHERE
            clazzWorkQuestionResponseQuestionUid = :uid
            AND CAST(clazzWorkQuestionResponseInactive AS INTEGER) = 0
        """

        const val FIND_BY_QUESTIONUID_AND_CLAZZ_MEMBER_UID = """
            SELECT * FROM ClazzWorkQuestionResponse WHERE
            clazzWorkQuestionResponseQuestionUid = :uid
            AND CAST(clazzWorkQuestionResponseInactive AS INTEGER) = 0
            AND clazzWorkQuestionResponseClazzMemberUid = :clazzMemberUid
        """
    }
}
