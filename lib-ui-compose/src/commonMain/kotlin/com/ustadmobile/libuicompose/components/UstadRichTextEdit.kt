package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * UstadRichTextEdit provides a HTML formatted WYSIWYG text editor.
 *
 * On Android: Uses Aztec (Wordpress WYSIWYG library). The Jetpack compose library still has some
 *   teething issues on Android - does not handle arrow keys. On Android editing rich text normally
 *   needs to take the user to a new screen to make room for the toolbar etc.
 *
 * On Desktop/JVM: Uses Richtextedit multiplatform library
 *
 * @param html the current HTML
 * @param onHtmlChange event to call when the HTML is updated.
 * @param modifier Modifier
 * @param editInNewScreen if true, then on platforms where editing is done in a new screen (Android)
 *        we will just show the HTML inside an outlined text field. When the user taps it, it will
 *        trigger the onClickToEditInNewScreen function
 * @param onClickToEditInNewScreen where editInNewScreen is true and this is running on a platform
 *        where rich text editing is done in a new screen, this function must handle the click. This
 *        is normally handled by the ViewModel which will initiate navigation for result e.g. via
 *        UstadViewModel.navigateToEditHtml
 * @param editInNewScreenLabel where editInNewScreen is true and this is a platform where editing is
 *        done on a new screen, then this is the label that will be added to the outlined text box.
 * @param placeholderText On platforms where editing is not done in a new screen (Desktop), this is the
 *        placeholder text.
 */
@Composable
expect fun UstadRichTextEdit(
    html: String,
    onHtmlChange: (String) -> Unit,
    onClickToEditInNewScreen: () -> Unit,
    modifier: Modifier = Modifier,
    editInNewScreen: Boolean = !isDesktop(),
    editInNewScreenLabel: String? = null,
    placeholderText: String? = null,
)