package com.ustadmobile.core.viewmodel.epubcontent

import com.ustadmobile.core.contentformats.epub.opf.Package
import com.ustadmobile.core.domain.openexternallink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.appstate.OverflowItem
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

/**
 * ScrollCommand - see epubScrollCommands
 *
 * @param spineIndex the index of to scroll to in the spine
 * @param hash if not null, the hash within the item to scroll to after loading
 */
data class EpubScrollCommand(
    val spineIndex: Int,
    val hash: String? = null
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

    private val openExternalLinkUseCase: OpenExternalLinkUseCase by instance()

    private val _epubScrollCommands = MutableSharedFlow<EpubScrollCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Scroll commands that are to be observed by the view. These can be emitted when the user clicks
     * on an internal link (e.g. via onClickLink) and when the user clicks on an item from the table
     * of contents.
     *
     * These commands then need to be actioned by the view (e.g. using LazyColumn / TanStack query)
     */
    val epubScrollCommands: Flow<EpubScrollCommand> = _epubScrollCommands.asSharedFlow()

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

                    _appUiState.update { prev ->
                        prev.copy(
                            title = opfPackage.metadata.titles.firstOrNull()?.content ?: "",
                            overflowItems = listOf(
                                OverflowItem(
                                    label = "Table of contents",
                                    onClick = { }
                                )
                            )
                        )
                    }

                }catch(e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun onClickLink(
        baseUrl: String,
        href: String,
    ) {
        val url = UrlKmp(baseUrl).resolve(href)
        val urlStr = url.toString()
        val hashIndex = urlStr.indexOf("#")
        val urlWithoutHash = urlStr.substringBefore("#")
        val indexInSpine = _uiState.value.spineUrls.indexOf(urlWithoutHash)
        if(indexInSpine >= 0) {
            _epubScrollCommands.tryEmit(
                EpubScrollCommand(
                    spineIndex = indexInSpine,
                    hash = if(hashIndex > 0) {
                        urlStr.substring(hashIndex)
                    }else {
                        null
                    }
                )
            )
        }else {
            openExternalLinkUseCase(url.toString())
        }

    }

    companion object {

        const val DEST_NAME = "EpubContent"

    }

}