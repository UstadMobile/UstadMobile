package com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.door.DoorUri

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/WebVTT_API/Web_Video_Text_Tracks_Format
 */
interface GetSubtitleTrackFromUriUseCase {

    suspend operator fun invoke(
        subtitleTrackUri: DoorUri,
        filename: String,
        locale: String? = null,
    ): SubtitleTrack

    companion object {

        const val VTT_MIME_TYPE = "text/vtt"

        const val PARAM_TRACK_FILENAME = "filename"

        const val PARAM_LOCALE = "locale"

        const val VTT_HEADER_LINE = "WEBVTT"
    }


}