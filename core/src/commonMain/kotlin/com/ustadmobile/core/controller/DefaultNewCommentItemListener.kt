package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.Comments
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*

class DefaultNewCommentItemListener(di: DI, val context: Any, var fromPerson: Long = 0L,
                                    var toPerson:Long = 0L, var entityId: Long = 0): NewCommentItemListener {
    override fun addComment(entityType: Int, entityUid: Long, comment: String,
                            public: Boolean, to: Long, from: Long) {

//        val accountManager: UstadAccountManager by instance()
//
//        val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

        val comment = Comments(entityType, entityId, fromPerson,
                UMCalendarUtil.getDateInMilliPlusDays(0), comment, public)
        comment.commentsToPersonUid = toPerson
        GlobalScope.launch {
            //db.commentsDao.insertAsync(comment)
        }
    }

}