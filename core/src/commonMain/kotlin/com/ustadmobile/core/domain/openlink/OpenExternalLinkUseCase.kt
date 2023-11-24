package com.ustadmobile.core.domain.openlink

/**
 * Interface that is implemented on underlying platforms to handle when the user clicks an external
 * link.
 */
fun interface OpenExternalLinkUseCase {

    operator fun invoke(url: String)

}