package com.ustadmobile.core.domain.openexternallink

/**
 * Interface that is implemented on underlying platforms to handle when the user clicks an external
 * link.
 */
interface OpenExternalLinkUseCase {

    operator fun invoke(url: String)

}