package com.ustadmobile.core.domain.openlink

/**
 * Interface that is implemented on underlying platforms to handle when the user clicks an external
 * link.
 *
 * The LinkTarget is only usable on the Web/JS version. The primary purpose of which is to allow
 * an end-to-end test to explicitly specify that a link should be opened in the same tab, because
 * cypress does not allow new tabs.
 */
fun interface OpenExternalLinkUseCase {

    operator fun invoke(url: String, target: LinkTarget)

    companion object {

        enum class LinkTarget {
            BLANK, TOP, SELF, DEFAULT;

            companion object {
                fun of(targetAttr: String): LinkTarget {
                    return when(targetAttr.lowercase()) {
                        "_top" -> TOP
                        "_blank" -> BLANK
                        "_self" -> SELF
                        else -> DEFAULT
                    }
                }
            }

        }
    }

}