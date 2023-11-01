package com.ustadmobile.core.viewmodel.epubcontent

import com.ustadmobile.core.contentformats.epub.opf.Package
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.adaptivity.xmlutil.serialization.XML
import org.kodein.di.DI
import org.kodein.di.instance

data class EpubContentUiState(
    val spineUrls: List<String> = emptyList()
)

class EpubContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val httpClient: HttpClient by instance()

    private val xml: XML by instance()

    private val _uiState = MutableStateFlow(EpubContentUiState())

    val uiState: Flow<EpubContentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val contentEntryVersion = activeRepo.contentEntryVersionDao
                .findByUidAsync(entityUidArg) ?: return@launch
            val cevUrl = contentEntryVersion.cevUrl ?: return@launch

            withContext(Dispatchers.Default) {
                try {
                    val opfStr = httpClient.get(cevUrl).bodyAsText()
                    val opfPackage = xml.decodeFromString(
                        deserializer = Package.serializer(),
                        string = opfStr
                    )
                    val cevUrlObj = UrlKmp(cevUrl)

                    val manifestItemsMap = opfPackage.manifest.items.associateBy { it.id }
                    val spineUrls = opfPackage.spine.itemRefs.mapNotNull { itemRef ->
                        manifestItemsMap[itemRef.idRef]?.let {
                            cevUrlObj.resolve(it.href)
                        }?.toString()
                    }

                    _uiState.update { prev ->
                        prev.copy(
                            spineUrls = spineUrls,
                        )
                    }

                }catch(e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

    }

    companion object {

        const val DEST_NAME = "EpubContent"

    }

}