package com.ustadmobile.core.domain.contententry.getlocalurlforcontent

/**
 * Provide a local URL (e.g. on the Embedded server) for a given path in a given ContentEntryVersion
 */
interface GetLocalUrlForContentUseCase {

    operator fun invoke(contentEntryVersionUid: Long, pathInContentEntry: String): String

}