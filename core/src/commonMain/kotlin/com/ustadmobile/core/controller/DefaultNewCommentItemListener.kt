package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*

class DefaultNewCommentItemListener(override val di: DI, val context: Any, val entityUid: Long,
                                    val tableId: Int, val isPrivate: Boolean): NewCommentItemListener, DIAware {

    override fun addComment(text: String) {

        val accountManager: UstadAccountManager by instance()

        val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

        val commentObj = Comments(tableId, entityUid, accountManager.activeAccount.personUid,
                getSystemTimeInMillis(), text, isPrivate)
        commentObj.commentsToPersonUid = 0
        GlobalScope.launch {
            repo.commentsDao.insertAsync(commentObj)
        }
    }

}