package com.ustadmobile.libuicompose.view.videocontent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.components.UstadLinkifyText
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

/**
 * Checks to ensure that VLC native is found. If not, then show an error message. Once found, then
 * show the content.
 */
@Composable
fun VlcCheck(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    var vlcFound: Boolean? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            vlcFound = try {
                NativeDiscovery().discover()
            }catch(e: Throwable) {
                false
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        if(vlcFound == true) {
            content()
        }else if(vlcFound == false) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        modifier = Modifier.size(64.dp),
                        contentDescription = null
                    )
                    UstadLinkifyText(
                        text = stringResource(MR.strings.could_not_load_vlc),
                        linkExtractor = rememberLinkExtractor(),
                        modifier = Modifier.width(450.dp)
                    )
                }
            }
        }
    }



}