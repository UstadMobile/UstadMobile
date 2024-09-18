package com.ustadmobile.lib.rest.domain.contententry.getsubtitletrackfromuri

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri.GetSubtitleTrackFromUriUseCase
import com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri.GetSubtitleTrackFromUriUseCase.Companion.PARAM_TRACK_FILENAME
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.tmpfiles.CreateTempUriUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.util.ext.requireBodyAsText
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.lib.rest.ext.requireQueryParamOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSubtitleTrackFromUriServerUseCase(
    private val saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase,
    private val createTempUriUseCase: CreateTempUriUseCase,
    private val deleteUrisUseCase: DeleteUrisUseCase,
) {

    suspend operator fun invoke(
        request: IHttpRequest
    ): SubtitleTrack {
        val filename = request.requireQueryParamOrThrow(PARAM_TRACK_FILENAME)
        val trackText = request.requireBodyAsText()

        if(!trackText.startsWith(GetSubtitleTrackFromUriUseCase.VTT_HEADER_LINE, ignoreCase = true)) {
            throw HttpApiException(400, "Invalid subtitle track")
        }

        //Needs (another) use case
        val tmpUri = withContext(Dispatchers.IO) {
            createTempUriUseCase("subtitle-upload", "vtt").also {
                it.toFile().writeText(trackText)
            }
        }

        try {
            val savedBlobs = saveLocalUrisAsBlobsUseCase(
                localUrisToSave = listOf(
                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = tmpUri.toString(),
                        deleteLocalUriAfterSave = true,
                        mimeType = "text/vtt",
                    )
                ),
                onTransferJobItemCreated = { _, _ -> },
            )

            val blobUrl = savedBlobs.firstOrNull()?.blobUrl
                ?: throw HttpApiException(500, "Failed to save subtitle track")

            return SubtitleTrack(
                uri = blobUrl,
                mimeType = "text/vtt",
                langCode = "en",//TODO,
                title = filename,
            )
        }finally {
            deleteUrisUseCase(listOf(tmpUri.toString()))
        }
    }

}