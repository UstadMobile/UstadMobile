package com.ustadmobile.core.viewmodel.site


import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.lib.db.entities.Site
import dev.icerock.moko.resources.StringResource

data class TopLevelNavInfo(
    val destRoute: String,
    val label: StringResource,
    val flag: Long
)

 val COMMON_TOP_LEVEL_NAV_ITEMS = listOf(
    TopLevelNavInfo(
        destRoute = ClazzListViewModel.DEST_NAME_HOME,
        label = MR.strings.courses,
        flag = Site.SHOW_COURSE
    ),
    TopLevelNavInfo(
        destRoute = ContentEntryListViewModel.DEST_NAME_HOME,
        label = MR.strings.library,
        flag = Site.SHOW_LIBRARY
    ),
    TopLevelNavInfo(
        destRoute = ConversationListViewModel.DEST_NAME_HOME,
        label = MR.strings.messages,
        flag = Site.SHOW_MESSAGES
    ),
    TopLevelNavInfo(
        destRoute = PersonListViewModel.DEST_NAME_HOME,
        label = MR.strings.people,
        flag = Site.SHOW_PEOPLE
    )
)
