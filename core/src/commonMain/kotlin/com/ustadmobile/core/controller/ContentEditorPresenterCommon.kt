package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEditorView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Interface which acts as delegate between UI and editor
 */
interface ContentEditorPageDelegate {

    fun loadPage(href: String)

    suspend fun addPage(title: String):Boolean

    suspend fun updatePage(page: EpubNavItem):Boolean

    suspend fun removePage(href: String): String?

    suspend fun changePageOrder(navItems : MutableList<EpubNavItem>)

    fun getCurrentDocument(): EpubNavDocument

}


/**
 * Presenter which defines all file operation during content editing process. It is responsible to
 * create file if does't exist, mount zipped file before editing, delete/add resources and
 * remove all unused resources from the zip when deleted on the content file.
 *
 * **Operational Flow:**
 *
 *
 *
 * Use [createDocument] to create file if the file doesn't
 * exists. i.e when new document is created.
 *
 * Use [mountContainer] to mount zipped file
 * to the temporary directory so that can be edited.
 *
 * Use [removeUnUsedResources] to remove all
 * unused files which are inside the zip but not referenced on content file.
 *
 *
 * @author kileha3
 */

abstract class ContentEditorPresenterCommon(context: Any, arguments: Map<String, String?>, view: ContentEditorView,
                                            private val storage: String?,internal val mountContainer: suspend (Long) -> String)
    : UstadBaseController<ContentEditorView>(context, arguments, view) , ContentEditorPageDelegate{

    internal var contentEntryUid: Long = 0L

    internal var mountedFileAccessibleUrl: String ? = null

    var currentPage: String = ""

    var documentPath : String? = null

    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    abstract var epubNavDocument: EpubNavDocument?

    internal val umAppRepo : UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)

    internal val umDatabase :UmAppDatabase = UmAppDatabase.getInstance(context)

    /**
     * Create new document with title and description
     * @param title document title
     * @param description document description
     */
    abstract suspend fun createDocument(title: String, description:String) : Boolean

    /**
     * Add media content to the document
     * @param media path
     * @param mimetype media mimetype
     */
    abstract suspend fun addMediaContent(path: String, mimetype: String)

    /**
     * Save new content to the file currently being edited
     * @param filename name of the file being edited
     * @param content content to be saved
     */
    abstract suspend fun saveContentToFile(filename: String, content: String)

    /**
     * Update document title and description
     * @param documentTitle new title to be set to the document
     * @param description new description to be set to the document
     */
    abstract suspend fun updateDocumentMetaInfo(documentTitle: String, description: String, isNewDocument: Boolean): String?

    /**
     * App page to the document
     * @param pageTitle title of the page to be added
     */
    abstract suspend fun addPageToDocument(pageTitle: String):Boolean

    /**
     * Update page in the document
     * @param page page to be update in the document
     */
    abstract suspend fun updatePageInDocument(page: EpubNavItem): Boolean

    /**
     * Remove page from the document
     * @param href index of the page to be removed from the document
     */
    abstract suspend fun removePageFromDocument(href: String): String?

    /**
     * Change document page order - From page list fragment
     * @param pageList new page list as arranged from the UI
     */
    abstract suspend fun changeDocumentPageOrder(pageList: MutableList<EpubNavItem>)

    /**
     * Remove all unsed resources from the document
     */
    abstract suspend fun removeUnUsedResources(): Boolean

    /**
     * Get current document path
     */
    internal abstract suspend fun getDocumentPath(storage: String?) : String

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        contentEntryUid = arguments.getOrElse(ContentEditorView.CONTENT_ENTRY_UID, {"0"})!!.toLong()

        GlobalScope.launch {

            //setup document path
            documentPath = getDocumentPath(storage)

            val contentEntry = umAppRepo.contentEntryDao.findByEntryId(contentEntryUid)
            if(contentEntry != null){
               val container = umAppRepo.containerDao.getMostRecentDownloadedContainerForContentEntryAsync(contentEntry.contentEntryUid)
                if(container != null){
                    val created = createDocument(contentEntry.title!!,
                            contentEntry.description!!)
                    if(created){
                        currentPage = epubNavDocument?.toc?.getChild(0)?.href!!
                        loadCurrentPage()
                    }

                }

            }
        }
    }


    private fun showErrorMessage(message: String){
        view.runOnUiThread(Runnable {
            view.showError(message)
        })
    }


    private fun loadCurrentPage(){
        val url = UMFileUtil.joinPaths("", currentPage)
        view.runOnUiThread(Runnable {
            view.loadPage(url)
        })
    }

    fun handleUpdateDocumentMetaInfo(title: String, description: String){
        GlobalScope.launch {
            val result = updateDocumentMetaInfo(title, description, false)
            if(result == null){
                view.runOnUiThread(Runnable {
                    showErrorMessage(impl.getString(MessageID.error_message_update_document, context)) })
            }
        }
    }


    override fun loadPage(href: String) {
         handleSelectedPage(href)
    }

    override fun getCurrentDocument(): EpubNavDocument {
        return epubNavDocument!!
    }

    override suspend fun addPage(title: String):Boolean {
       return addPageToDocument(title)
    }

    override suspend fun updatePage(page: EpubNavItem):Boolean {
        return updatePageInDocument(page)
    }

    /**
     * Remove page from page list
     */
    override suspend fun removePage(href: String):String? {
        return removePageFromDocument(href)
    }

    /**
     * Change page orders
     */
    override suspend fun changePageOrder(navItems: MutableList<EpubNavItem>) {
        changeDocumentPageOrder(navItems)
    }


    /**
     * Save content from the editor
     */
    fun handleSaveContent(content: String){
        GlobalScope.launch {
            saveContentToFile(currentPage, content)
        }
    }


    /**
     * Add media content to the document
     */
    suspend fun handleAddMediaContent(path: String, mimeType: String) {
        return addMediaContent(path, mimeType)
    }

    /**
     * Selec page to load
     */
    fun handleSelectedPage(selectedPage: String){
        currentPage = selectedPage
        loadCurrentPage()
    }

    /**
     * Remove all unused recources from the document
     */
    suspend fun handleRemoveUnUsedResources() :Boolean{
        return removeUnUsedResources()
    }

    /**
     * Handle Editor operations - Editing document
     */
    fun handleEditorActions(action: String, param: String?) {
        view.runOnUiThread(Runnable{
            when (action) {
                ContentEditorView.TEXT_FORMAT_TYPE_BOLD -> view.setContentBold()

                ContentEditorView.TEXT_FORMAT_TYPE_ITALIC -> view.setContentItalic()

                ContentEditorView.TEXT_FORMAT_TYPE_STRIKE -> view.setContentStrikeThrough()

                ContentEditorView.TEXT_FORMAT_TYPE_UNDERLINE -> view.setContentUnderlined()

                ContentEditorView.TEXT_FORMAT_TYPE_SUP -> view.setContentSuperscript()

                ContentEditorView.TEXT_FORMAT_TYPE_SUB -> view.setContentSubScript()

                ContentEditorView.TEXT_FORMAT_TYPE_FONT -> view.setContentFontSize(param!!)

                ContentEditorView.PARAGRAPH_FORMAT_ALIGN_CENTER -> view.setContentCenterAlign()

                ContentEditorView.PARAGRAPH_FORMAT_ALIGN_LEFT -> view.setContentLeftAlign()

                ContentEditorView.PARAGRAPH_FORMAT_ALIGN_RIGHT -> view.setContentRightAlign()

                ContentEditorView.PARAGRAPH_FORMAT_ALIGN_JUSTIFY -> view.setContentJustified()

                ContentEditorView.PARAGRAPH_FORMAT_LIST_ORDERED -> view.setContentOrderedList()

                ContentEditorView.PARAGRAPH_FORMAT_LIST_UNORDERED -> view.setContentUnOrderList()

                ContentEditorView.PARAGRAPH_FORMAT_INDENT_DECREASE -> view.setContentDecreaseIndent()

                ContentEditorView.PARAGRAPH_FORMAT_INDENT_INCREASE -> view.setContentIncreaseIndent()

                ContentEditorView.ACTION_REDO -> view.setContentRedo()

                ContentEditorView.ACTION_UNDO -> view.setContentUndo()

                ContentEditorView.ACTION_TEXT_DIRECTION_LTR -> view.setContentTextDirection(action)

                ContentEditorView.ACTION_TEXT_DIRECTION_RTL -> view.setContentTextDirection(action)

                ContentEditorView.CONTENT_INSERT_FILL_THE_BLANKS_QN -> view.insertFillTheBlanksQuestion()

                ContentEditorView.CONTENT_INSERT_MULTIPLE_CHOICE_QN -> view.insertMultipleChoiceQuestion()

                ContentEditorView.ACTION_INSERT_CONTENT -> view.insertContent(param!!)

                ContentEditorView.ACTION_SELECT_ALL -> view.selectAllContent()

                ContentEditorView.ACTION_CLEAR_ALL -> view.clearEditableSection()

                ContentEditorView.ACTION_FOCUS_NEXT_LINK -> view.focusNextLink()

                ContentEditorView.ACTION_SAVE_CONTENT -> view.saveContent()
            }
        })
    }


    companion object{
        const val EDITOR_BASE_DIR_NAME = "umEditor"

        const val CONTENT_OPF_FILE = "content.opf"

        const val PAGE_TEMPLATE = "template_page.html"

        const val MIME_TYPE_PAGE = "text/html"

        const val MIME_TYPE_DOCUMENT = "application/epub+zip"

        const val TEMP_FILE_PREFIX = "UmEditorFileHelper"

        const val PAGE_PREFIX = "page_"

        const val DEFAULT_NAVDOC_DEPTH = 1
    }


}