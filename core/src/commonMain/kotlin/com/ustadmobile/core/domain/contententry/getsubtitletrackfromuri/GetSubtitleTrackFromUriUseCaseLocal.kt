package com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri.GetSubtitleTrackFromUriUseCase.Companion.VTT_HEADER_LINE
import com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri.GetSubtitleTrackFromUriUseCase.Companion.VTT_MIME_TYPE
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.removeFileExtension
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.readString


class GetSubtitleTrackFromUriUseCaseLocal(
    private val uriHelper: UriHelper,
    private val dispatcher: CoroutineDispatcher,
    private val supportedLanguagesConfig: SupportedLanguagesConfig,
): GetSubtitleTrackFromUriUseCase {

    override suspend fun invoke(
        subtitleTrackUri: DoorUri,
        filename: String,
        locale: String?
    ): SubtitleTrack = withContext(dispatcher){
        val text = uriHelper.openSource(subtitleTrackUri).readString(byteCount = HEADER_BYTECOUNT)
        if(!text.trimStart().startsWith(VTT_HEADER_LINE, ignoreCase = true)) {
            throw IllegalArgumentException("Text does not start with WEBVTT")
        }

        SubtitleTrack(
            uri = subtitleTrackUri.toString(),
            mimeType = VTT_MIME_TYPE,
            langCode = locale ?: supportedLanguagesConfig.displayedLocale,
            title = filename.removeFileExtension(),
        )
    }

    companion object {


        const val HEADER_BYTECOUNT = 32L

    }
}