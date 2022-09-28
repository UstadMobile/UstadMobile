package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class DefaultNewCommentItemListener(
    override val di: DI,
    val context: Any,
    val entityUid: Long,
    val tableId: Int,
    val isPublic: Boolean,
    private val commentOnSubmitterUid: Long? = null
): NewCommentItemListener, DIAware {

    override fun addComment(text: String) {

        val accountManager: UstadAccountManager by instance()

        val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

        val commentObj = Comments(tableId, entityUid, accountManager.activeAccount.personUid,
                getSystemTimeInMillis(), text, isPublic)
        GlobalScope.launch {
            commentObj.commentSubmitterUid = commentOnSubmitterUid ?: repo.clazzAssignmentDao
                                .getSubmitterUid(entityUid, accountManager.activeAccount.personUid)

            repo.commentsDao.insertAsync(commentObj)
        }
    }

}