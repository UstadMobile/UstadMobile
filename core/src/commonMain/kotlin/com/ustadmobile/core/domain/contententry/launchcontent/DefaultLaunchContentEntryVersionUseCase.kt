package com.ustadmobile.core.domain.contententry.launchcontent

import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.lib.db.entities.ContentEntryVersion

/**
 * Default implementation of LaunchContentEntryVersionUseCase: works by navigating to the view for
 * a specific piece of content.
 */
class DefaultLaunchContentEntryVersionUseCase: LaunchContentEntryVersionUseCase {

    override suspend fun invoke(
        contentEntryVersion: ContentEntryVersion,
        navController: UstadNavController,
        target: OpenExternalLinkUseCase.Companion.LinkTarget,
        xapiSession: XapiSession?,
    ): LaunchContentEntryVersionUseCase.LaunchResult {
        val destName = when(contentEntryVersion.cevContentType) {
            ContentEntryVersion.TYPE_XAPI -> XapiContentViewModel.DEST_NAME
            ContentEntryVersion.TYPE_PDF -> PdfContentViewModel.DEST_NAME
            ContentEntryVersion.TYPE_EPUB -> EpubContentViewModel.DEST_NAME
            ContentEntryVersion.TYPE_VIDEO -> VideoContentViewModel.DEST_NAME
            else -> null
        }

        if(destName != null) {
            navController.navigate(
                viewName = destName,
                args = mapOf(
                    UstadViewModel.ARG_ENTITY_UID to contentEntryVersion.cevUid.toString(),
                    UstadViewModel.ARG_COURSE_BLOCK_UID to (xapiSession?.cbUid ?: 0).toString(),
                    UstadViewModel.ARG_CLAZZUID to (xapiSession?.clazzUid ?: 0).toString(),
                    UstadViewModel.ARG_CONTENT_ENTRY_UID to (xapiSession?.contentEntryUid ?: 0).toString(),
                )
            )
        }

        return LaunchContentEntryVersionUseCase.LaunchResult()
    }
}