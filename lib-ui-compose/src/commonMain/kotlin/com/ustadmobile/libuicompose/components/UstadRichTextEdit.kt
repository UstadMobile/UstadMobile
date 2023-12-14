package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun UstadRichTextEdit(
    modifier: Modifier = Modifier,
    editInNewScreen: Boolean,
    onClickEditInNewScreen: () -> Unit = {},
    html: String = "",
    onHtmlChange: (String) -> Unit = {}
){
    if (editInNewScreen){
        RichTextEditorRow(
            modifier = modifier,
            html = html,
            enabled = false,
            onClick = onHtmlChange
        )
    } else {
        RichTextEditorRow(
            modifier = modifier,
            html = html,
            enabled = true,
            toolbar = { Text("") }
        )
    }
}

@Composable
fun RichTextEditorRow(
    modifier: Modifier,
    html: String,
    enabled: Boolean,
    toolbar: @Composable (() -> Unit)? = null,
    onClick: (String) -> Unit = {}
) {

    val state = rememberRichTextState()

    var html1 by remember { mutableStateOf(html) }
    state.setHtml(html1)

    var isDialogOpen by remember { mutableStateOf(false) }
    var link by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
                    html1 = state.toHtml()
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
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
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
                    onDismissRequest = { },
                    confirmButton = {
                        Button(onClick = { isDialogOpen = false }) {
                            Text(stringResource(MR.strings.add))
                        }
                    },
                    title = { Text("Add Link",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().defaultItemPadding()) },
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
                                label = { androidx.compose.material3.Text(stringResource(MR.strings.enter_link)) },
                            )

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                                value = link,
                                onValueChange = { newText ->
                                    text = newText
                                },
                                label = { androidx.compose.material3.Text(stringResource(MR.strings.add_text)) },
                            )
                        }
                    },
                )
            }
//            DialogWindow(
//                onCloseRequest = { dialogState.value = false },
//                visible = dialogState.value,
//                title = stringResource(MR.strings.enter_link),
//                icon = painterResource(MR.images.ustad_logo),
//                content = {
//                    Column(
//                        modifier = Modifier.fillMaxHeight(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center,
//                    ) {
//                        OutlinedTextField(
//                            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
//                            value = link,
//                            onValueChange = { newText ->
//                                link = newText
//                            },
//                            label = { androidx.compose.material3.Text(stringResource(MR.strings.enter_link)) },
//                        )
//
//                        OutlinedTextField(
//                            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
//                            value = link,
//                            onValueChange = { newText ->
//                                text = newText
//                            },
//                            label = { androidx.compose.material3.Text(stringResource(MR.strings.add_text)) },
//                        )
//
//                        Button(
//                            onClick = {
//                                state.addLink(text = text, url = link)
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                backgroundColor = MaterialTheme.colors.secondary
//                            )
//                        ) {
//                            Text(
//                                stringResource(MR.strings.submit).uppercase(),
//                                color = contentColorFor(MaterialTheme.colors.secondary)
//                            )
//                        }
//                    }
//                }
//            )
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