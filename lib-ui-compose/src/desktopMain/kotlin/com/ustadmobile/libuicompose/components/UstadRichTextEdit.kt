package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.FormatAlignRight
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UstadRichTextEdit(
    html: String,
    onHtmlChange: (String) -> Unit,
    onClickToEditInNewScreen: () -> Unit,
    modifier: Modifier,
    editInNewScreen: Boolean,
    editInNewScreenLabel: String?,
    placeholder: (@Composable () -> Unit)?
) {
    var htmlStr by remember {
        mutableStateOf(html)
    }

    val richTextState = rememberRichTextState()

    /*
     * This effect will set the HTML on richTextState when it is changed by the ViewModel. It will
     * avoid calling setHtml on the RichTextState if the HTML was what was most recently set by the
     * component itself.
     */
    LaunchedEffect(html) {
        if(html != htmlStr) {
            htmlStr = html
            richTextState.setHtml(html)
        }
    }

    Column(
        modifier = modifier
    ) {

        RichTextStyleRow(state = richTextState)

        OutlinedRichTextEditor(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if(editInNewScreen) {
                        it.clickable {
                            onClickToEditInNewScreen()
                        }
                    }else {
                        it
                    }
                }
                .onFocusChanged {
                    val richTextStateHtml = richTextState.toHtml()
                    if(richTextStateHtml != htmlStr) {
                        htmlStr = richTextStateHtml
                        onHtmlChange(richTextStateHtml)
                    }
                },
            state = richTextState,
            readOnly = editInNewScreen,
            placeholder = placeholder
        )
    }

}


@Composable
private fun RichTextStyleRow(
    state: RichTextState
){

    var isDialogOpen by remember { mutableStateOf(false) }
    var linkDialogHref by remember { mutableStateOf("") }
    var linkDialogText by remember { mutableStateOf("") }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
    ) {

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left,
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Left,
                icon = Icons.Outlined.FormatAlignLeft
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Center,
                icon = Icons.Outlined.FormatAlignCenter
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Right,
                icon = Icons.Outlined.FormatAlignRight
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                icon = Icons.Outlined.FormatBold
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                icon = Icons.Outlined.FormatItalic
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                icon = Icons.Outlined.FormatUnderlined
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                icon = Icons.Outlined.FormatStrikethrough
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 28.sp
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontSize == 28.sp,
                icon = Icons.Outlined.FormatSize
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            color = Color.Red
                        )
                    )
                },
                isSelected = state.currentSpanStyle.color == Color.Red,
                icon = Icons.Filled.Circle,
                tint = Color.Red
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            background = Color.Yellow
                        )
                    )
                },
                isSelected = state.currentSpanStyle.background == Color.Yellow,
                icon = Icons.Outlined.Circle,
                tint = Color.Yellow
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleUnorderedList()
                },
                isSelected = state.isUnorderedList,
                icon = Icons.Outlined.FormatListBulleted,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleOrderedList()
                },
                isSelected = state.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )
        }

        item {
            lateinit var textFieldValue: TextFieldValue

            RichTextStyleButton(
                onClick = {
                    linkDialogHref = ""
                    linkDialogText = state.annotatedString.substring(state.selection)
                    isDialogOpen = true
                },
                isSelected = state.isLink,
                icon = Icons.Outlined.Link,
            )
        }

        item {
            if (isDialogOpen)
                AlertDialog(
                    onDismissRequest = { isDialogOpen = false },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                isDialogOpen = false
                            }
                        ) {
                            Text(
                                stringResource(MR.strings.cancel),
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                state.addLink(text = linkDialogText, url = linkDialogHref)
                                isDialogOpen = false
                            }
                        ) {
                            Text(
                                stringResource(MR.strings.add),
                            )
                        }
                    },
                    title = {
                        Text(
                            modifier = Modifier.defaultItemPadding(),
                            text = stringResource(MR.strings.enter_link)
                        )
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().defaultItemPadding()
                                    .testTag("add_link_href"),
                                value = linkDialogHref,
                                onValueChange = {
                                    linkDialogHref = it
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                                label = { Text(stringResource(MR.strings.link)) },
                            )

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().defaultItemPadding()
                                    .testTag("add_link_text"),
                                value = linkDialogText,
                                onValueChange = {
                                    linkDialogText = it
                                },
                                singleLine = true,
                                label = { Text(stringResource(MR.strings.text)) },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        state.addLink(text = linkDialogText, url = linkDialogHref)
                                        isDialogOpen = false
                                    }
                                )
                            )
                        }
                    },
                )
        }
    }
}

@Composable
fun RichTextStyleButton(
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color? = null,
    isSelected: Boolean = false,
) {
    IconButton(
        modifier = Modifier
            // Workaround to prevent the rich editor
            // from losing focus when clicking on the button
            // (Happens only on Desktop)
            .focusProperties { canFocus = false },
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        ),
    ) {
        Icon(
            icon,
            contentDescription = icon.name,
            tint = tint ?: LocalContentColor.current,
            modifier = Modifier
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
        )
    }
}