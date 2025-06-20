package com.ustadmobile.core.domain.navigation

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.lib.db.entities.Site
/**
 * Provides the default destination. This is a scoped dependency.
 *
 * The default destination is:
 * When on a personal account learning space or local : ContentEntryList
 * When on a multi user learning space: ClazzList
 *
 * This is a trivial use case; its sole reason to exist is to provide a sensible single point of
 * truth
 */
class GetDefaultDestinationUseCase(
    private val systemUrlConfig: SystemUrlConfig,
    private val learningSpace: LearningSpace,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) {

    suspend operator fun invoke(): String {
        val effectiveDb = (repo ?: db)
        val site =  effectiveDb.siteDao().getSiteAsync()
        val visibilityFlag = site?.bottomNavVisibilityFlag ?: 0L

        val defaultDestination = if(learningSpace.url == systemUrlConfig.newPersonalAccountsLearningSpaceUrl ||
            learningSpace.isLocal) {
            Site.SHOW_LIBRARY to ContentEntryListViewModel.DEST_NAME_HOME
        }else {
            Site.SHOW_COURSE to ClazzListViewModel.DEST_NAME_HOME
        }
        val isLibraryVisible = visibilityFlag and Site.SHOW_LIBRARY != 0L
        val isCourseVisible = visibilityFlag and Site.SHOW_COURSE != 0L

        return when {
            isLibraryVisible && isCourseVisible -> defaultDestination.second

            isLibraryVisible && !isCourseVisible -> {
                if (defaultDestination.first == Site.SHOW_LIBRARY)
                    ContentEntryListViewModel.DEST_NAME_HOME
                else if (visibilityFlag and Site.SHOW_MESSAGES != 0L)
                    ConversationListViewModel.DEST_NAME_HOME
                else if (visibilityFlag and Site.SHOW_PEOPLE != 0L)
                    PersonListViewModel.DEST_NAME_HOME
                else
                    PersonListViewModel.DEST_NAME_HOME
            }

            isCourseVisible && !isLibraryVisible -> {
                if (defaultDestination.first == Site.SHOW_COURSE)
                    ClazzListViewModel.DEST_NAME_HOME
                else if (visibilityFlag and Site.SHOW_MESSAGES != 0L)
                    ConversationListViewModel.DEST_NAME_HOME
                else if (visibilityFlag and Site.SHOW_PEOPLE != 0L)
                    PersonListViewModel.DEST_NAME_HOME
                else
                    PersonListViewModel.DEST_NAME_HOME
            }

            visibilityFlag and Site.SHOW_MESSAGES != 0L ->
                ConversationListViewModel.DEST_NAME_HOME
            visibilityFlag and Site.SHOW_PEOPLE != 0L ->
                PersonListViewModel.DEST_NAME_HOME

            else -> PersonListViewModel.DEST_NAME_HOME
        }
    }
}