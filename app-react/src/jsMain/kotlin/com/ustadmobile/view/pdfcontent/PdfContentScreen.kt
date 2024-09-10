package com.ustadmobile.view.pdfcontent

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentUiState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.hooks.useMessageEffect
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.hooks.useWindowFocusedEffect
import com.ustadmobile.mui.components.UstadFullSizeIframe
import js.uri.encodeURIComponent
import org.kodein.di.direct
import org.kodein.di.instance
import react.FC
import react.Props
import react.useEffect
import react.useMemo
import react.useRequiredContext
import web.dom.document
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import web.html.HTMLIFrameElement
import web.window.window

external interface PdfContentScreenProps : Props{
    var uiState: PdfContentUiState

    var onProgressed: (Int) -> Unit

    var onComplete: () -> Unit

    var onActiveChanged: (Boolean) -> Unit
}

/**
 * The PdfContentComponent uses PDF.js viewer to get page change events. This is bundled to
 * ensure it works on Firefox, Chrome, etc (even though PDF.js is already built into Firefox). PDF.js
 * is bundled in app-react/src/jsMain/resources/pdf-js. Only viewer.html has a small modification to
 * listen for page change events and then post a message to the parent frame ( e.g. as per
 * https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage ).
 *
 * The normal technique to initialize listening for events as per
 * https://github.com/mozilla/pdf.js/wiki/Third-party-viewer-usage does not work when viewer.html is
 * being used in an iframe - the webviewerloaded event will instead be fired to the parent frame.
 *
 * The PdfContentComponent uses a listener to catch the webviewerloaded event and send it back
 * to the viewer.html iframe so it can initiate listening for page change events.
 */
val PdfContentComponent = FC<PdfContentScreenProps> { props ->
    val di = useRequiredContext(DIContext)
    val learningSpace = useMemo(dependencies = emptyArray()) {
        di.direct.instance<UstadAccountManager>().activeLearningSpace
    }

    useWindowFocusedEffect { focused ->
        props.onActiveChanged(focused)
    }

    useMessageEffect<String> {
        if(!learningSpace.url.startsWith(it.origin, ignoreCase = true))
            return@useMessageEffect

        //viewer.html will send a message in the form of pdf-pages:pageNum/numPages
        if(it.data.startsWith("pdf-pages:", ignoreCase = true)) {
            val pageComponents = it.data.substringAfter("pdf-pages:")
                .split("/")

            val (pageNum, numPages) = pageComponents[0].toInt() to pageComponents[1].toInt()
            if(numPages == 0)
                return@useMessageEffect

            if(pageNum == numPages) {
                props.onComplete()
            }else {
                props.onProgressed((pageNum * 100) / numPages)
            }
        }
    }

    props.uiState.pdfUrl?.also { pdfUrl ->
        UstadFullSizeIframe {
            src = "${learningSpace.url}umapp/pdf-js/web/viewer.html?file=${encodeURIComponent(pdfUrl)}"
            id = "pdf_js"
        }
    }

    useEffect(dependencies = emptyArray()) {
        val eventHandler: (Event) -> Unit = {
            val iframeEl = document.getElementById("pdf_js") as HTMLIFrameElement
            iframeEl.contentDocument?.dispatchEvent(Event(EventType("webviewerloaded")))
        }

        window.addEventListener(EventType("webviewerloaded"), eventHandler)

        cleanup {
            window.removeEventListener(EventType("webviewerloaded"), eventHandler)
        }
    }
}

val PdfContentScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        PdfContentViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(PdfContentUiState())

    PdfContentComponent {
        uiState  = uiStateVal
        onProgressed = viewModel::onProgressed
        onComplete = viewModel::onComplete
        onActiveChanged = viewModel::onActiveChanged
    }
}

