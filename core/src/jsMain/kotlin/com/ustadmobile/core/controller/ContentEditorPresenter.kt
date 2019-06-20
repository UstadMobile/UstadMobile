package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.lib.db.entities.Container

actual class ContentEditorPresenter actual constructor(context: Any, arguments: Map<String, String?>,
                                                       view: ContentEditorView, storage: String?,
                                                       mountContainer: suspend (Long) -> String)
    :ContentEditorPresenterCommon(context,arguments,view,storage,mountContainer){


    actual override suspend fun createDocument(title: String, description: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun openExistingDocument(container: Container): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun addMediaContent(path: String, mimetype: String) {
    }

    actual override suspend fun saveContentToFile(filename: String, content: String) {
    }

    actual override suspend fun updateDocumentMetaInfo(documentTitle: String, description: String, isNewDocument: Boolean): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun addPageToDocument(pageTitle: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun removePageFromDocument(href: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun updatePageInDocument(page: EpubNavItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun changeDocumentPageOrder(pageList: MutableList<EpubNavItem>) {
    }

    actual override suspend fun removeUnUsedResources(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun getDocumentPath(storage: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun getEpubNavDocument(): EpubNavDocument? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}