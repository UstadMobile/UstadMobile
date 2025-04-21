package com.ustadmobile.core.domain.navigation

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel

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
) {

    operator fun invoke(): String {
        return if(learningSpace.url == systemUrlConfig.systemBaseUrl || learningSpace.isLocal) {
            ContentEntryListViewModel.DEST_NAME_HOME
        }else {
            ClazzListViewModel.DEST_NAME_HOME
        }
    }

}