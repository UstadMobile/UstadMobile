package com.ustadmobile.port.android.view.composable

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.toughra.ustadmobile.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener

class DefaultToolbarListener(
    private val onInvalidate: () -> Unit
): IAztecToolbarClickListener {
    override fun onToolbarCollapseButtonClicked() {

    }

    override fun onToolbarExpandButtonClicked() {

    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
        onInvalidate()
    }

    override fun onToolbarHeadingButtonClicked() {
        onInvalidate()
    }

    override fun onToolbarHtmlButtonClicked() {

    }

    override fun onToolbarListButtonClicked() {
        onInvalidate()
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

}

/**
 * Basic Composable wrapper for the Aztec editor.
 */
@Composable
fun AztecEditor(
    html: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    var aztec: Aztec? by remember {
        mutableStateOf(null)
    }

    var htmlState: String by remember {
        mutableStateOf(html)
    }

    val coroutineScope = rememberCoroutineScope()

    //Update HTML on the view if the caller state sets new HTML, but avoid calling fromHtml if
    //it was the same HTML we delivered from onChange event.
    LaunchedEffect(html) {
        if(html != htmlState) {
            //The HTML has been changed by the caller, not by our own event.
            Log.v("AztecEditor", "Seems like we got new HTML from our caller - setting")
            aztec?.visualEditor?.fromHtml(html)
        }
    }

    //Listen for changes and call the onChange function: TODO: throttle toFormattedHtml / onChange calls
    DisposableEffect(aztec) {
        val visualEditorInstance = aztec?.visualEditor

        val textWatcher = object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(visualEditorInstance != null) {
                    val newHtml = visualEditorInstance.toFormattedHtml()
                    htmlState = newHtml
                    onChange(newHtml)
                }
            }
        }

        visualEditorInstance?.addTextChangedListener(textWatcher)

        onDispose {
            visualEditorInstance?.removeTextChangedListener(textWatcher)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val rootView = LayoutInflater.from(context).inflate(R.layout.aztec_editor, null, false)
            val visualEditor: AztecText = rootView.findViewById(R.id.editor)
            val aztecEditor = Aztec.with(
                visualEditor = visualEditor,
                toolbar = rootView.findViewById(R.id.formatting_toolbar),
                toolbarClickListener = DefaultToolbarListener(
                    onInvalidate = {
                        coroutineScope.launch {
                            delay(200)
                            val newHtml = visualEditor.toFormattedHtml()
                            htmlState = newHtml
                            onChange(newHtml)
                        }
                    }
                )
            )
            rootView.setTag(R.id.tag_aztec, aztecEditor)
            visualEditor.fromHtml(html)

            aztec = aztecEditor

            rootView
        },
        update = {

        }
    )
    
}