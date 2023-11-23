package com.ustadmobile.libuicompose.helloworld

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import app.cash.paging.Pager
import app.cash.paging.PagingSource
import app.cash.paging.compose.*
import com.ustadmobile.libuicompose.components.HtmlText
import com.ustadmobile.libuicompose.components.UstadRichTextEdit

@Composable
fun HelloWorld() {

    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {


        HtmlText(
            html = "<p style=\"text-align: left;\">Sallam, I am a<span style=\"font-weight: 700; font-style: italic;\">Developer </span>in <span style=\"text-decoration: underline;\">UstadMobile</span></p>"
        )

        UstadRichTextEdit(
            html = "Complete your assignment or <b>else</b>",
//            label = stringResource(MR.strings.description),
//            onClick = {  },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("description")
        )

    }
}


@Composable
fun HelloPaging() {
    lateinit var pagingSourceFactory: () -> PagingSource<Int, String>

    val pager = remember {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = pagingSourceFactory,
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(Modifier.fillMaxSize()) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.hashCode() },
            contentType = lazyPagingItems.itemContentType()
        ) { index ->
            val item = lazyPagingItems[index]
            Text(item ?: "")
        }
    }
}
