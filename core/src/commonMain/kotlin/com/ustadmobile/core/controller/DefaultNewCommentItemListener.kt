package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class DefaultNewCommentItemListener(override val di: DI, val context: Any, val entityUid: Long,
                                    val tableId: Int, val isPublic: Boolean, val toPerson: Long = 0): NewCommentItemListener, DIAware {

    override fun addComment(text: String) {

        val accountManager: UstadAccountManager by instance()

        val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

        val commentObj = Comments(tableId, entityUid, accountManager.activeAccount.personUid,
                getSystemTimeInMillis(), text, isPublic)
        GlobalScope.launch {
            val assignment = repo.clazzAssignmentDao.findByUidAsync(entityUid) ?: return@launch
            val submitterUid = if(assignment.caGroupUid != 0L){
                repo.courseGroupMemberDao.findByPersonUid(assignment.caGroupUid, accountManager.activeAccount.personUid)?.cgmGroupNumber?.toLong() ?: 0
            }else{
                0L
            }
            commentObj.commentSubmitterUid = if(toPerson == 0L) submitterUid else toPerson

            repo.commentsDao.insertAsync(commentObj)
        }
    }

}