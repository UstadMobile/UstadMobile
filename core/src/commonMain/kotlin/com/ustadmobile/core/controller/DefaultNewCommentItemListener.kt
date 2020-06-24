package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.Comments
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DefaultNewCommentItemListener(val db: UmAppDatabase,
                                    val context: Any, var fromPerson: Long = 0L,
                                    var toPerson:Long = 0L, var entityId: Long = 0): NewCommentItemListener {
    override fun addComment(entityType: Int, entityUid: Long, comment: String,
                            public: Boolean, to: Long, from: Long) {
        val comment = Comments(entityType, entityId, fromPerson,
                UMCalendarUtil.getDateInMilliPlusDays(0), comment, public)
        comment.commentsToPersonUid = toPerson
        GlobalScope.launch {
            db.commentsDao.insertAsync(comment)
        }
    }

}