package com.ustadmobile.core.viewmodel.xapicontent

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

data class XapiContentUiState(
    val url: String? = null,
)

class XapiContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(XapiContentUiState())

    val uiState: Flow<XapiContentUiState> = _uiState.asStateFlow()

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val httpClient: HttpClient by instance()

    init {
        viewModelScope.launch {
            try {
                val contentEntryVersion = activeRepo.contentEntryVersionDao
                    .findByUidAsync(entityUidArg) ?: return@launch
                val cevUrl = contentEntryVersion.cevUrl ?: return@launch

                val tinCanXmlStr = httpClient.get(cevUrl)
                    .bodyAsText()

                val xppFactory: XmlPullParserFactory = di.direct.instance(tag = DiTag.XPP_FACTORY_NSAWARE)
                val xpp = xppFactory.newPullParser()
                xpp.setInputString(tinCanXmlStr)
                val tinCanXml = TinCanXML.loadFromXML(xpp)
                val launchHref = tinCanXml.launchActivity?.launchUrl ?: return@launch
                val href = UMFileUtil.resolveLink(cevUrl, launchHref)
                _uiState.update { prev ->
                    prev.copy(url = href)
                }
                _appUiState.update { prev ->
                    prev.copy(
                        title = tinCanXml.launchActivity?.name,
                        hideBottomNavigation = true,
                    )
                }
            }catch(e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    companion object {

        const val DEST_NAME = "XapiContent"

    }

}