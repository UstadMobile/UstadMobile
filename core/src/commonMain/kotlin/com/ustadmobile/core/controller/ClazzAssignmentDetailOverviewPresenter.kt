package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class ClazzAssignmentDetailOverviewPresenter(context: Any,
                                             arguments: Map<String, String>, view: ClazzAssignmentDetailOverviewView,
                                             lifecycleOwner: DoorLifecycleOwner,
                                             di: DI,private val newCommentItemListener: DefaultNewCommentItemListener =
                                                     DefaultNewCommentItemListener(di, context))
    : UstadDetailPresenter<ClazzAssignmentDetailOverviewView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner),
        NewCommentItemListener by newCommentItemListener{

    override val persistenceMode: PersistenceMode
          get() = PersistenceMode.DB


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        val clazzAssignment = withTimeoutOrNull(2000) {
            repo.clazzAssignmentDao.findByUidAsync(arguments[ARG_ENTITY_UID]?.toLong()
                    ?: 0)
        }

        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzAssignment?.caClazzUid ?: 0, Role.PERMISSION_ASSIGNMENT_UPDATE)
    }

    override fun handleClickEdit() {
        systemImpl.go(ClazzAssignmentEditView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to entity?.caUid.toString()), context)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val loggedInPersonUid = accountManager.activeAccount.personUid

        val clazzAssignment = withTimeoutOrNull(2000) {
            db.clazzAssignmentDao.findByUidAsync(entityUid)
        } ?: ClazzAssignment()


        val clazzWithSchool = withTimeoutOrNull(2000) {
            db.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        } ?: ClazzWithSchool()

        view.timeZone = clazzWithSchool.effectiveTimeZone()

        view.clazzAssignmentContent =
                withTimeoutOrNull(2000) {
                    repo.clazzAssignmentContentJoinDao.findAllContentByClazzAssignmentUidDF(
                            clazzAssignment.caUid, loggedInPersonUid)
                }

        val loggedInPerson = withTimeoutOrNull(2000) {
            db.personDao.findByUidAsync(loggedInPersonUid)
        }
        val clazzEnrolment: ClazzEnrolment? = withTimeoutOrNull(2000) {
            db.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                    clazzAssignment.caClazzUid)
        }

        val isStudent = if (loggedInPerson?.admin == true) {
            false
        } else {
            if (clazzEnrolment == null) {
                false
            } else {
                (clazzEnrolment.clazzEnrolmentRole != ClazzEnrolment.ROLE_TEACHER)
            }
        }

        if(isStudent){
            view.clazzMetrics = repo.clazzAssignmentDao.getAssignmentMetrics(clazzAssignment.caClazzUid,
                clazzAssignment.caUid)
        }

        if(isStudent && clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = repo.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    loggedInPersonUid)
            view.showPrivateComments = true
        }else{
            view.showPrivateComments = false
        }

        if(clazzAssignment.caClassCommentEnabled){
            view.clazzAssignmentClazzComments = repo.commentsDao.findPublicByEntityTypeAndUidLive(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid)
        }

        return clazzAssignment
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}