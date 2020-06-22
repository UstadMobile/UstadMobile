package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.Comments
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DefaultNewCommentItemListener(val db: UmAppDatabase,
                                    val context: Any): NewCommentItemListener {
    override fun addComment(entityType: Int, entityUid: Long, comment: String,
                            public: Boolean, to: Long) {
        val comment = Comments(entityType, entityUid, UmAccountManager.getActivePersonUid(context),
                UMCalendarUtil.getDateInMilliPlusDays(0), comment, public)
        GlobalScope.launch {
            db.commentsDao.insertAsync(comment)
        }
    }

}