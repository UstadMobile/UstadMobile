package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.lib.db.entities.Container

expect class ContentEditorPresenter(context: Any, arguments: Map<String, String?>,
                                    view: ContentEditorView, storage: String?,
                                    database : UmAppDatabase,repository : UmAppDatabase,
                                    mountContainer: suspend (Long) -> String)
    : ContentEditorPresenterCommon{

    override suspend fun createDocument(title: String, description: String): Boolean

    override suspend fun openExistingDocument(container: Container): Boolean

    override suspend fun addMediaContent(path: String, mimetype: String): Boolean

    override suspend fun saveContentToFile(filename: String, content: String): Boolean

    override suspend fun updateDocumentMetaInfo(documentTitle: String, description: String, isNewDocument: Boolean): String?

    override suspend fun addPageToDocument(pageTitle: String): Boolean

    override suspend fun removePageFromDocument(href: String): String?

    override suspend fun updatePageInDocument(page: EpubNavItem): Boolean

    override suspend fun changeDocumentPageOrder(pageList: MutableList<EpubNavItem>)

    override suspend fun removeUnUsedResources(): Boolean

    override suspend fun getDocumentPath(storage: String?): String

    override fun getEpubNavDocument(): EpubNavDocument?

}