package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManager.FileEntrySource
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil.joinPaths
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.InputStream
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.Charsets.UTF_8


actual class ContentEditorPresenter actual constructor(context: Any, arguments: Map<String, String?>,
                                                       view: ContentEditorView, val storage: String?,
                                                       val database : UmAppDatabase,
                                                       private val repository : UmAppDatabase ,
                                                       mountContainer: suspend (Long) -> String)
    :ContentEditorPresenterCommon(context,arguments,view,storage,database,mountContainer){


    private var nextNavItem: EpubNavItem? = null

    private var documentPath : String? = null

    private var containerManager: ContainerManager? = null

    /**
     * {@inheritDoc}
     */
    actual override suspend fun createDocument(title: String, description: String): Boolean {
        var filePath = "/http/$EDITOR_BASE_DIR_NAME/templates"
        filePath = joinPaths(filePath, ContentEditorView.RESOURCE_BLANK_DOCUMENT)

        val emptyDocInputStream : InputStream = impl.getAssetInputStreamAsync(context,filePath)

        val container = Container()
        container.containerContentEntryUid = contentEntryUid
        container.lastModified = System.currentTimeMillis()
        container.mimeType = MIME_TYPE_DOCUMENT
        container.containerUid = repository.containerDao.insert(container)
        containerUid = container.containerUid

        var status = database.contentEntryStatusDao.findByUidAsync(contentEntryUid)
        if (status == null) {
            status = ContentEntryStatus(contentEntryUid, true,
                    container.fileSize)
            status.locallyAvailable = true
            database.contentEntryStatusDao.insert(status)
        }
        documentPath = getDocumentPath(storage)
        containerManager = ContainerManager(container, database, repository, documentPath)
        val tmpFile: File = File.createTempFile(TEMP_FILE_PREFIX, ".zip")
        try {
            FileOutputStream(tmpFile).use{
                outputStream -> UMIOUtils.readFully(emptyDocInputStream, outputStream)
            }
            addEntriesFromZipToContainer(tmpFile.absolutePath, containerManager!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }finally {
            if (!tmpFile.delete())
                tmpFile.deleteOnExit()
        }
        val updateDocument = updateDocumentMetaInfo(title, description, true)
        if(updateDocument != null){
            val pageTitle = impl.getString(MessageID.content_untitled_page, context)
            val pageAdded = addPageToDocument(pageTitle)
            if(pageAdded){
                val contentEntry = repository.containerDao.getMostRecentContainerForContentEntryAsync(contentEntryUid)
                if(contentEntry != null){
                    mountedFileAccessibleUrl = mountContainer(contentEntry.containerUid)
                }
            }
        }
        return  mountedFileAccessibleUrl != null
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun openExistingDocument(container: Container): Boolean {
        containerUid = container.containerUid
        documentPath = getDocumentPath(storage)
        mountedFileAccessibleUrl = mountContainer(containerUid)
        containerManager = ContainerManager(container, database, repository, documentPath)
        return  mountedFileAccessibleUrl != null
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun addMediaContent(path: String, mimetype: String):Boolean {
        val mediaFile = File(path)
        try {
            addManifestItem(mediaFile.name, mimetype)
            containerManager!!.addEntries(FileEntrySource(mediaFile,mediaFile.name))
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun saveContentToFile(filename: String, content: String) : Boolean {
        var inputStream: InputStream? = null
        var contentSaved = false;
        try {
            inputStream = containerManager?.getInputStream(containerManager?.getEntry(filename)!!)!!
            val tmpFile = File.createTempFile(TEMP_FILE_PREFIX, filename)
            copyFile(inputStream, tmpFile)

            val document = Jsoup.parse(readTextFile(tmpFile.absolutePath))
            tmpFile.delete()
            val contentContainer = document.select(".um-editor")
            contentContainer.first().html(content)
            contentSaved = addEntryWithContent(filename, document.html())

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeInputStream(inputStream)
        }
        return contentSaved
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun updateDocumentMetaInfo(documentTitle: String, description: String, isNewDocument: Boolean): String? {
        try {
            if (updateOpfMetadataInfo(documentTitle, description,
                            if (isNewDocument) UUID.randomUUID().toString() else null)) {
                val entry = database.contentEntryDao.findByEntryId(contentEntryUid)!!
                entry.title = documentTitle
                database.contentEntryDao.update(entry)
                return documentTitle
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun addPageToDocument(pageTitle: String): Boolean {
        var copied = false
        val href: String?
        var inputStream: InputStream? = null

        try {

            href = PAGE_PREFIX + System.currentTimeMillis() + ".html"
            inputStream = containerManager?.getInputStream(containerManager!!.getEntry(PAGE_TEMPLATE)!!)!!
            val tmpFile = File.createTempFile(TEMP_FILE_PREFIX, href)
            copied = copyFile(inputStream, tmpFile)
            copied = copied && (addNavItem(href, pageTitle) && addManifestItem(href, MIME_TYPE_PAGE)
                    && addSpineItem(href, MIME_TYPE_PAGE))

            containerManager!!.addEntries(FileEntrySource(tmpFile,href))

            if (!tmpFile.delete() && copied)
                tmpFile.deleteOnExit()

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeInputStream(inputStream)
        }

        return copied
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun updatePageInDocument(page: EpubNavItem): Boolean {
        val document = getEpubNavDocument()!!
        val parent = document.toc

        val navItem = getNavItemByHref(page.href!!, document.toc!!)
        val itemIndex = parent!!.getChildren()!!.indexOf(navItem)
        if (navItem != null) {
            navItem.title = page.title
        }
        parent.getChildren()!![itemIndex] = navItem!!

        var bout: ByteArrayOutputStream? = null
        var metaInfoUpdated = false
        try {
            bout = ByteArrayOutputStream()
            val serializer = impl.newXMLSerializer()
            serializer.setOutput(bout, UTF_8.name())
            document.serialize(serializer)
            bout.flush()
            bout.flush()
            val navContent = String(bout.toByteArray(), UTF_8)
            val filename = getEpubOpfDocument()?.navItem?.href!!
            addEntryWithContent(filename, navContent)
            metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeOutputStream(bout)
        }

        return getEpubNavDocument()!!.getNavById(page.href!!) == null && metaInfoUpdated
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun removePageFromDocument(href: String): String? {
        return if (removeNavItem(href) && removeSpineItem(href) && removeManifestItem(href)) {
            href
        } else null
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun changeDocumentPageOrder(pageList: MutableList<EpubNavItem>) {
        val document = getEpubNavDocument()
        document!!.toc?.getChildren()?.clear()
        document.toc!!.getChildren()!!.addAll(pageList)

        var bout: ByteArrayOutputStream? = null
        try {
            bout = ByteArrayOutputStream()
            val serializer = impl.newXMLSerializer()
            serializer.setOutput(bout, UTF_8.name())
            document.serialize(serializer)
            bout.flush()
            val navContent = String(bout.toByteArray(), UTF_8)
            val filename = getEpubOpfDocument()?.navItem?.href!!
            addEntryWithContent(filename, navContent)
            updateOpfMetadataInfo(null, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeOutputStream(bout)
        }
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun removeUnUsedResources(): Boolean {
        //TODO implement this
        return true
    }


    /**
     * Get navigation Item by its href.
     * @param href href to be fund
     * @param parentNavItem parent nav item
     * @return EpubNavItem if found otherwise NULL.
     */
    private fun getNavItemByHref(href: String, parentNavItem: EpubNavItem): EpubNavItem? {
        for (navItem in parentNavItem.getChildren()!!) {
            if (navItem.href == href) {
                return navItem
            }
        }
        return null
    }


    /**
     * Update opf document meta data iformation
     * @param title new opf document title
     * @param uuid epub pub-id
     * @return True if meta data were updated successfully otherwise they were not update.
     */
    private suspend fun updateOpfMetadataInfo(title: String?, description: String?, uuid: String?): Boolean {
        val opfDocument = getEpubOpfDocument()!!
        opfDocument.title = title ?: opfDocument.title
        opfDocument.description = description ?: opfDocument.description
        opfDocument.id = uuid ?: opfDocument.id
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val lastUpdateDateTime = formatter.format(Date(System.currentTimeMillis()))
        val bout = ByteArrayOutputStream()
        val serializer = impl.newXMLSerializer()
        serializer.setOutput(bout, UTF_8.name())
        opfDocument.serialize(serializer)
        bout.flush()
        val opfContent = String(bout.toByteArray(), UTF_8)
        addEntryWithContent(CONTENT_OPF_FILE, opfContent)
        UMIOUtils.closeOutputStream(bout)

        return title == null || getEpubOpfDocument()?.title.equals(title)
    }


    private suspend fun addEntryWithContent(filename: String, content: String) : Boolean {
        var entryAdded = false
        try {
            val tmpFile = File.createTempFile(TEMP_FILE_PREFIX,
                    if (filename != CONTENT_OPF_FILE)
                        filename
                    else
                        System.currentTimeMillis().toString())

            writeToFile(tmpFile, content)
            containerManager!!.addEntries(FileEntrySource(tmpFile,filename))
            entryAdded = true

            if (!tmpFile.delete())
                tmpFile.deleteOnExit()

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return entryAdded
    }


    private fun writeToFile(file: File, content: String) {
        try {
            val out = FileOutputStream(file)
            out.write(content.toByteArray())
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    /**
     * Add spine items on  opf file
     * @param href page index to be updates
     * @return true if updated otherwise false.
     */
    private suspend fun addSpineItem(href: String, mimeType: String): Boolean {
        val opfDocument = getEpubOpfDocument()
        val spineItem = OpfItem()
        spineItem.href = href
        spineItem.setMimeType(mimeType)
        spineItem.id = href
        opfDocument!!.getSpine().add(spineItem)
        var bout: ByteArrayOutputStream? = null
        var metaInfoUpdated = false
        try {
            bout = ByteArrayOutputStream()
            val serializer = impl.newXMLSerializer()
            serializer.setOutput(bout, UTF_8.name())
            opfDocument.serialize(serializer)
            bout.flush()
            val opfContent = String(bout.toByteArray(), UTF_8)
            addEntryWithContent(CONTENT_OPF_FILE, opfContent)

            metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeOutputStream(bout)
        }
        return getEpubOpfDocument()!!.getLinearSpinePositionByHREF(href) != -1 && metaInfoUpdated
    }

    /**
     * Remove spine item from the opf document
     * @param href spine href to be removed
     * @return True if a spine item was removed otherwise it wasn't
     */
    private suspend fun removeSpineItem(href: String): Boolean {
        val opfDocument = getEpubOpfDocument()

        if (opfDocument!!.getSpine().size == 1)
            return false
        opfDocument.getSpine()
                .removeAt(opfDocument.getLinearSpinePositionByHREF(href))
        var metaInfoUpdated = false
        var bout: ByteArrayOutputStream? = null
        try {
            bout = ByteArrayOutputStream()
            val serializer = impl.newXMLSerializer()
            serializer.setOutput(bout, "UTF-8")
            opfDocument.serialize(serializer)
            bout.flush()
            val opfContent = String(bout.toByteArray(), UTF_8)
            addEntryWithContent(CONTENT_OPF_FILE, opfContent)
            metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeOutputStream(bout)
        }
        return getEpubOpfDocument()!!.getLinearSpinePositionByHREF(href) == -1 && metaInfoUpdated
    }

    /**
     * Add an item to the navigation file.
     * @param href item ref
     * @param title item title.
     * @return true if added otherwise false.
     */
    private suspend fun addNavItem(href: String, title: String): Boolean {
        val document = getEpubNavDocument()!!
        val navItem = EpubNavItem(title, href, null, DEFAULT_NAVDOC_DEPTH)
        document.toc!!.addChild(navItem)
        var bout: ByteArrayOutputStream? = null
        var metaInfoUpdated = false
        try {
            bout = ByteArrayOutputStream()
            val serializer = impl.newXMLSerializer()
            serializer.setOutput(bout, "UTF-8")
            document.serialize(serializer)
            bout.flush()
            val navContent = String(bout.toByteArray(), UTF_8)
            val filename = getEpubOpfDocument()!!.navItem!!.href
            addEntryWithContent(filename!!, navContent)
            metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeOutputStream(bout)
        }
        return getNavItemByHref(href, getEpubNavDocument()?.toc!!) != null && metaInfoUpdated
    }

    /**
     * Remove navigation item from nav document
     * @param href href of an item to be removed
     * @return True if it was successfully removed otherwise it wasn't
     */
    private suspend fun removeNavItem(href: String): Boolean {
        val document = getEpubNavDocument()!!
        val navItem = getNavItemByHref(href, document.toc!!)
        val navItems = document.toc!!.getChildren()
        val tobeDeletedNavIndex = navItems!!.indexOf(navItem)
        val nextNavItemIndex = if (tobeDeletedNavIndex == navItems.size - 1)
            navItems.size - 2
        else
            tobeDeletedNavIndex + 1
        if (nextNavItemIndex == -1)
            return false

        nextNavItem = navItems[nextNavItemIndex]
        var metaInfoUpdated = false
        if (document.toc!!.getChildren()!!.remove(navItem)) {
            var bout: ByteArrayOutputStream? = null
            try {
                bout = ByteArrayOutputStream()
                val serializer = impl.newXMLSerializer()
                serializer.setOutput(bout, UTF_8.name())
                document.serialize(serializer)
                bout.flush()
                val navContent = String(bout.toByteArray(), UTF_8)
                val filename = getEpubOpfDocument()!!.navItem!!.href
                addEntryWithContent(filename!!, navContent)
                metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                UMIOUtils.closeOutputStream(bout)
            }
        }

        return getNavItemByHref(href, getEpubNavDocument()?.toc!!) == null && metaInfoUpdated
    }


    /**
     * Add manifest item on opf file
     * @param href manifest item ref
     * @param mimeType manifest item mime type
     * @return true if an item was added otherwise false.
     */
    private suspend fun addManifestItem(href: String, mimeType: String): Boolean {
        var metaInfoUpdated = false
        val opfDocument = getEpubOpfDocument()
        val manifestItem = OpfItem()
        manifestItem.href = href
        manifestItem.id = href
        manifestItem.setMimeType(mimeType)
        opfDocument!!.getManifestItems()[manifestItem.id!!] = manifestItem
        var bout: ByteArrayOutputStream? = null
        try {
            bout = ByteArrayOutputStream()
            val serializer = impl.newXMLSerializer()
            serializer.setOutput(bout, UTF_8.name())
            opfDocument.serialize(serializer)
            bout.flush()
            val opfContent = String(bout.toByteArray(), UTF_8)
            addEntryWithContent(CONTENT_OPF_FILE, opfContent)
            metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            UMIOUtils.closeOutputStream(bout)
        }
        return getEpubOpfDocument()!!.getManifestItems().containsKey(manifestItem.id!!) && metaInfoUpdated
    }

    /**
     * Remove manifest item
     * @param itemId Id of an item to be removed
     * @return True if an item was removed otherwise it wasn't
     */
    private suspend fun removeManifestItem(itemId: String): Boolean {
        val opfDocument = getEpubOpfDocument()
        val opfItem = opfDocument!!.getManifestItems().remove(itemId)
        var metaInfoUpdated = false
        if (opfItem != null) {
            var bout: ByteArrayOutputStream? = null
            try {
                bout = ByteArrayOutputStream()
                val serializer = impl.newXMLSerializer()
                serializer.setOutput(bout, UTF_8.name())
                opfDocument.serialize(serializer)
                bout.flush()
                val opfContent = String(bout.toByteArray(), UTF_8)
                addEntryWithContent(CONTENT_OPF_FILE, opfContent)
                metaInfoUpdated = updateOpfMetadataInfo(null, null, null)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                UMIOUtils.closeOutputStream(bout)
            }
        }
        return !getEpubOpfDocument()!!.getManifestItems().containsKey(itemId) && metaInfoUpdated
    }


    private fun getEpubOpfDocument(): OpfDocument ?{
        try {
            val inputStream = containerManager?.getInputStream(
                    containerManager?.getEntry(CONTENT_OPF_FILE)!!)!!
            val xpp = impl.newPullParser(inputStream)
            val opfDocument = OpfDocument()
            opfDocument.loadFromOPF(xpp)
            return opfDocument
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }


    private fun copyFile(`is`: InputStream, dest: File): Boolean {
        if (dest.exists()) dest.delete()
        FileOutputStream(dest).use { os ->
            val buffer = ByteArray(1024)
            var length: Int = -1
            while ({length = `is`.read(buffer); length}() > 0) {
                os.write(buffer, 0, length)
            }
        }
        return dest.exists()
    }


    private fun readTextFile(fileSource: String): String {
        var fileOutput: String? = ""
        BufferedReader(FileReader(fileSource)).use { br ->
            val sb = StringBuilder()
            var line = br.readLine()
            while (line != null) {
                sb.append(line)
                sb.append(System.getProperty("line.separator"))
                line = br.readLine()
            }
            fileOutput = sb.toString()
        }
        return fileOutput!!
    }

    /**
     * {@inheritDoc}
     */
    actual override suspend fun getDocumentPath(storage: String?): String {
        val documentPath: String?
        val documentsDir = "documents/"
        documentPath = if (storage != null && storage.isNotEmpty()) {
            val baseContentDir = File(storage)
            val documentsRootDir = File(baseContentDir, documentsDir)
            if (!documentsRootDir.exists()) documentsRootDir.mkdirs()

            //isTestExecution = baseContentDir.absolutePath.startsWith("/var/")

            val documentDir = File(documentsRootDir, contentEntryUid.toString())

            if (!documentDir.exists()) documentDir.mkdirs()

            documentDir.absolutePath
        } else {
            val baseDir: String  = UstadMobileSystemImpl.instance.getStorageDirsAsync(context)[0]!!.dirURI!!
            joinPaths(baseDir,documentsDir, contentEntryUid.toString())
        }
        return documentPath!!
    }


    /**
     * {@inheritDoc}
     */
    actual override fun getEpubNavDocument(): EpubNavDocument? {
        try {
            val inputStream = containerManager?.getInputStream(
                    containerManager?.getEntry(getEpubOpfDocument()!!.navItem!!.href!!)!!)!!

            val navDocument = EpubNavDocument()
            navDocument.load(impl.newPullParser(inputStream, UTF_8.name()))
            return navDocument
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return  null
    }
}