package com.ustadmobile.libuicompose.components

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import com.ustadmobile.libuicompose.R


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
    placeholderText: String? = null,
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


    //Could use pointer interop
    // see https://stackoverflow.com/questions/69869068/can-jetpack-compose-input-modifiers-be-prevented-from-consuming-input-events
    // To draw a placeholder over the top
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
    ) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                val rootView = LayoutInflater.from(context).inflate(R.layout.aztec_editor, null, false)
                val placeholderTextView: TextView = rootView.findViewById(R.id.aztec_placeholder)
                if(placeholderText != null) {
                    placeholderTextView.text = placeholderText
                }else {
                    placeholderTextView.visibility = View.GONE
                }

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
                ).also {
                    it.visualEditor.setCalypsoMode(false)
                    it.addPlugin(CssUnderlinePlugin())
                    it.initSourceEditorHistory()
                }

                visualEditor.takeIf {
                    placeholderText != null
                }?.setOnFocusChangeListener { _, isFocused ->
                    placeholderTextView.visibility = if(
                        !isFocused && visualEditor.text.isBlank()
                    ) {
                        View.VISIBLE
                    }else {
                        View.GONE
                    }
                }

                rootView.setTag(R.id.tag_aztec, aztecEditor)
                visualEditor.fromHtml(html)
                aztec = aztecEditor

                rootView
            },
            update = {

            }
        )
    }

    
}