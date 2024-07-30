package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import java.io.File


private val PDF_DEFAULT_SCALE = 1.5f

//https://stackoverflow.com/questions/67959032/how-to-use-webview-in-jetpack-compose-for-desktop-app
@Composable
actual fun PdfFileComponent(
    pdfFile: File,
    onActiveChanged: (Boolean) -> Unit,
    onProgressed: (Int) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier,
) {
    val helper: PdfFileHelper = remember {
        PdfFileHelper()
    }

    val scale by remember {
        mutableStateOf(PDF_DEFAULT_SCALE)
    }

    DisposableEffect(Unit) {
        onDispose {
            helper.close()
        }
    }

    LaunchedEffect(pdfFile) {
        helper.load(pdfFile)
    }

    val numPages: Int by helper.numPages.collectAsState(0)

    val lazyListState = rememberLazyListState()
    val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
    val totalItems = lazyListState.layoutInfo.totalItemsCount
    LaunchedEffect(lastVisibleIndex, totalItems) {
        if(totalItems == 0 || lastVisibleIndex  == null)
            return@LaunchedEffect

        if (lastVisibleIndex == totalItems - 1) {
            onCompleted()
        }else {
            onProgressed(((lastVisibleIndex + 1) * 100 / totalItems))
        }
    }

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
    ) {
        items(
            count = numPages,
            key = { it }
        ) { index ->
            PdfPage(
                helper = helper,
                page = index,
                scale = scale,
            )
        }
    }
}

@Composable
fun PdfPage(
    helper: PdfFileHelper,
    page: Int,
    scale: Float,
){
    var imageBitmap: ImageBitmap by remember {
        //get from cache
        mutableStateOf(helper.getCachedPage(page, scale)
            ?: helper.getPlaceholderImage(page, scale))
    }

    var pageHeightPx by remember {
        mutableIntStateOf(600)
    }

    LaunchedEffect(helper, page, scale) {
        //For render size calculations: see PDFRenderer#renderImage (line 260)
        imageBitmap = helper.loadPage(page, scale) ?: throw IllegalArgumentException("could not load page $page")
        pageHeightPx = helper.getPageSize(page)?.height?.toInt()?.let { it * scale }?.toInt() ?: 600
    }

    val imageVal = imageBitmap

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Image(
                painter = BitmapPainter(imageVal),
                modifier = Modifier.border(1.dp, MaterialTheme.colors.onBackground),
                contentDescription = null
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
