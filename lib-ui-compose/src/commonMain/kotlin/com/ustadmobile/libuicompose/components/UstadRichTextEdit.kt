package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import androidx.compose.ui.text.input.TextFieldValue
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadRichTextEdit(
    modifier: Modifier = Modifier,
    html: String = "",
    onHtmlChange: (String) -> Unit = {}
){

    val richTextState = rememberRichTextState()
//    richTextState.setHtml(html)


    Column(
        modifier = modifier
            .fillMaxHeight()
    ) {

        if (isDesktop())
            RichTextStyleRow(state = richTextState, onClick = onHtmlChange)

        OutlinedRichTextEditor(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            state = richTextState,
            enabled = isDesktop()
        )
    }
}

@Composable
private fun RichTextStyleRow(
    state: RichTextState,
    onClick: (String) -> Unit
){

    var isDialogOpen by remember { mutableStateOf(false) }
    var link by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
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
                    onClick(state.toHtml())
                },
                isSelected = state.isUnorderedList,
                icon = Icons.Outlined.FormatListBulleted,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleOrderedList()
                    onClick(state.toHtml())
                },
                isSelected = state.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    isDialogOpen = true
                },
                isSelected = state.isUnorderedList,
                icon = Icons.Outlined.Link,
            )
        }
        item {
            if (isDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isDialogOpen = false },
                    dismissButton = {
                        Button(
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
                        Button(
                            onClick = {
                                state.addLink(text = text, url = link)
                                onClick(state.toHtml())
                            }
                        ) {
                            Text(
                                stringResource(MR.strings.add),
                            )
                        }
                    },
                    title = { Text(
                        modifier = Modifier.defaultItemPadding(),
                        text = stringResource(MR.strings.enter_link))
                    },
                    text = {
                        Column(
                            modifier = Modifier.defaultItemPadding()
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                                value = link,
                                onValueChange = { newText ->
                                    link = newText
                                },
                                label = { Text(stringResource(MR.strings.enter_link)) },
                            )

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                                value = link,
                                onValueChange = { newText ->
                                    text = newText
                                },
                                label = { Text(stringResource(MR.strings.add_text)) },
                            )
                        }
                    },
                )
            }
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
                MaterialTheme.colors.onPrimary
            } else {
                MaterialTheme.colors.onBackground
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
                        MaterialTheme.colors.primary
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
        )
    }
}