package com.ustadmobile.core.viewmodel.epubcontent

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget
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
import com.ustadmobile.core.MR
import com.ustadmobile.core.contentformats.epub.opf.Item
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.domain.epub.GetEpubTableOfContentsUseCase
import com.ustadmobile.core.util.requireEntryByUri
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.kodein.di.direct
import kotlin.concurrent.Volatile

data class EpubContentUiState(
    val contentEntryVersionUid: Long = 0,
    val spineUrls: List<String> = emptyList(),
    val tableOfContents: List<EpubTocItem> = emptyList(),
    val tableOfContentsOpen: Boolean = false,
    val collapsedTocUids: Set<Int> = emptySet(),
    val coverImageUrl: String? = null,
) {

    /**
     * The table of contents to be displayed: this will hide any children of collapsed items.
     */
    val tableOfContentToDisplay: List<EpubTocItem> = tableOfContents.filter { tocItem ->
        !collapsedTocUids.any { tocItem.isChildOfUid(it) }
    }

}

data class EpubTocItem(
    val uid: Int,
    val label: String,
    val href: String?,
    val children: List<EpubTocItem>,
    /*
     * A list of all the ancestor uids - makes it easy to filter out children of collapsed toc items
     */
    val parentUids: Set<Int> = emptySet(),
    val indentLevel: Int = 0,
) {

    val hasChildren: Boolean = children.isNotEmpty()

    fun isChildOfUid(
        uid: Int
    ) : Boolean {
        return uid in parentUids
    }

}

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
    private val getEpubTableOfContentsUseCase: GetEpubTableOfContentsUseCase =
        GetEpubTableOfContentsUseCase(
            xml = di.direct.instance()
        )
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val httpClient: HttpClient by instance()

    private val xml: XML by instance()

    private val _uiState = MutableStateFlow(
        EpubContentUiState(
            contentEntryVersionUid = entityUidArg
        )
    )

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

    @Volatile
    private var navUrl: String? =null

    init {
        val argManifestUrl = savedStateHandle[ARG_MANIFEST_URL]
        val argCevOpenUri = savedStateHandle[ARG_CEV_URI]

        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
            )
        }
        viewModelScope.launch {
            val (cevManifestUrl, cevOpenUri) = if(argManifestUrl != null && argCevOpenUri != null) {
                argManifestUrl to argCevOpenUri
            }else {
                val contentEntryVersion = activeRepo.contentEntryVersionDao
                    .findByUidAsync(entityUidArg) ?: return@launch
                val entityCevManifestUrl = contentEntryVersion.cevManifestUrl ?: return@launch
                val entityCevOpenUri = contentEntryVersion.cevOpenUri ?: return@launch
                entityCevManifestUrl to entityCevOpenUri
            }
            println("EpubContentViewModel manifest=$cevManifestUrl cevOpenUri=$cevOpenUri")
            println("EpubContent?$ARG_MANIFEST_URL=${UrlEncoderUtil.encode(cevManifestUrl)}&$ARG_CEV_URI=${UrlEncoderUtil.encode(cevOpenUri)}")

            val cevManifestUrlObj = UrlKmp(cevManifestUrl)
            val opfBaseUrl = cevManifestUrlObj.resolve(cevOpenUri)
            val manifest: ContentManifest = json.decodeFromString(
                httpClient.get(cevManifestUrl).bodyAsText())

            withContext(Dispatchers.Default) {
                try {
                    val opfEntry = manifest.requireEntryByUri(cevOpenUri)
                    val opfStr = httpClient.get(opfEntry.bodyDataUrl).bodyAsText()
                    val opfPackage = xml.decodeFromString(
                        deserializer = PackageDocument.serializer(),
                        string = opfStr
                    )

                    val manifestItemsMap = opfPackage.manifest.items.associateBy { it.id }
                    val spineUrls = opfPackage.spine.itemRefs.mapNotNull { itemRef ->
                        manifestItemsMap[itemRef.idRef]?.let {
                            opfBaseUrl.resolve(it.href)
                        }?.toString()
                    }
                    val coverImageUrl = opfPackage.coverItem()?.let {
                        opfBaseUrl.resolve(it.href)
                    }?.toString()

                    _uiState.update { prev ->
                        prev.copy(
                            spineUrls = spineUrls,
                            coverImageUrl = coverImageUrl
                        )
                    }

                    _appUiState.update { prev ->
                        prev.copy(
                            title = opfPackage.metadata.titles.firstOrNull()?.content ?: "",
                            overflowItems = listOf(
                                OverflowItem(
                                    label = systemImpl.getString(MR.strings.table_of_contents),
                                    onClick = {
                                        _uiState.update { prev ->
                                            prev.copy(
                                                tableOfContentsOpen = true,
                                            )
                                        }
                                    }
                                )
                            )
                        )
                    }

                    navUrl = opfPackage.tableOfContentItem()?.let {
                        opfBaseUrl.resolve(it.href).toString()
                    }

                    fun Item.bodyDataUrl(): String {
                        val prefix = cevOpenUri.substringBeforeLast("/", missingDelimiterValue = "")
                        val pathInManifest = if(prefix.isNotEmpty()) {
                            "$prefix/${href}"
                        }else {
                            href
                        }

                        return manifest.requireEntryByUri(pathInManifest).bodyDataUrl
                    }

                    val tocItems = getEpubTableOfContentsUseCase(
                        opfPackage = opfPackage,
                        readItemText = {
                            httpClient.get(it.bodyDataUrl()).bodyAsText()
                        }
                    ) ?: emptyList()

                    _uiState.update { prev ->
                        prev.copy(
                            tableOfContents = tocItems,
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
            openExternalLinkUseCase(url.toString(), LinkTarget.BLANK)
        }
    }

    fun onClickTocItem(
        tocItem: EpubTocItem,
    ) {
        _uiState.update { prev ->
            prev.copy(tableOfContentsOpen = false)
        }

        //NavUrl will always be set before the table of contents is displayed, so this is safe
        val baseHref = navUrl ?: return
        val itemUrl = tocItem.href ?: return
        onClickLink(baseHref, itemUrl)
    }

    fun onClickToggleTocItem(tocItem: EpubTocItem) {
        _uiState.update { prev ->
            prev.copy(
                collapsedTocUids = if(tocItem.uid in prev.collapsedTocUids) {
                    prev.collapsedTocUids.filter { it != tocItem.uid }.toSet()
                }else {
                    prev.collapsedTocUids + tocItem.uid
                }
            )
        }
    }

    fun onDismissTableOfContentsDrawer() {
        _uiState.update { prev ->
            prev.copy(
                tableOfContentsOpen = false,
            )
        }
    }

    companion object {

        const val ARG_MANIFEST_URL = "manifestUrl"

        const val ARG_CEV_URI = "cevUri"

        const val DEST_NAME = "EpubContent"

    }

}