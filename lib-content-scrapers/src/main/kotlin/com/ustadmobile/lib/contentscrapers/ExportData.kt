package com.ustadmobile.lib.contentscrapers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentCategoryDao
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.db.dao.LanguageVariantDao
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentCategory
import com.ustadmobile.lib.db.entities.ContentCategorySchema
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.LanguageVariant

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.exception.ExceptionUtils

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

import com.ustadmobile.lib.contentscrapers.ScraperConstants.JSON_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING

class ExportData {

    private lateinit var destinationDirectory: File
    private lateinit var gson: Gson
    private var pathList: ArrayList<String>? = null

    @Throws(IOException::class)
    fun export(destination: File, size: Int) {

        destination.mkdirs()
        destinationDirectory = destination

        gson = GsonBuilder().create()

        pathList = ArrayList()

        val db = UmAppDatabase.getInstance(Any())
        val repository = db //db.getRepository("https://localhost", "")
        val contentEntryDao = repository.contentEntryDao
        val contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
        val relatedDao = repository.contentEntryRelatedEntryJoinDao

        val schemaDao = repository.contentCategorySchemaDao
        val categoryDao = repository.contentCategoryDao
        val categoryJoinDao = repository.contentEntryContentCategoryJoinDao

        val languageDao = repository.languageDao
        val variantDao = repository.languageVariantDao

        val contentEntryList = contentEntryDao.publicContentEntries()
        val parentChildJoinList = contentParentChildJoinDao.publicContentEntryParentChildJoins()
        val relatedJoinList = relatedDao.publicContentEntryRelatedEntryJoins()

        UMLogUtil.logDebug("size of contentEntryList is " + contentEntryList.size)
        UMLogUtil.logDebug("size of parentChildJoinList is " + parentChildJoinList.size)
        UMLogUtil.logDebug("size of relatedJoinList is " + relatedJoinList.size)

        val schemaList = schemaDao.publicContentCategorySchemas()
        val categoryList = categoryDao.publicContentCategories()
        val categoryJoinList = categoryJoinDao.publicContentEntryContentCategoryJoins()

        UMLogUtil.logDebug("size of schemaList is " + schemaList.size)
        UMLogUtil.logDebug("size of categoryList is " + categoryList.size)
        UMLogUtil.logDebug("size of categoryJoinList is " + categoryJoinList.size)

        val langList = languageDao.publicLanguages()
        val langVariantList = variantDao.publicLanguageVariants()

        UMLogUtil.logDebug("size of langList is " + langList.size)
        UMLogUtil.logDebug("size of langVariantList is " + langVariantList.size)

        val containerList = repository.containerDao.findAllPublikContainers()

        UMLogUtil.logDebug("size of container is " + containerList.size)

        saveListToJson(split<ContentEntry>(contentEntryList, size), "contentEntry.", destinationDirectory)
        saveListToJson(split<ContentEntryParentChildJoin>(parentChildJoinList, size), "contentEntryParentChildJoin.", destinationDirectory)
        saveListToJson(split<ContentEntryRelatedEntryJoin>(relatedJoinList, size), "contentEntryRelatedEntryJoin.", destinationDirectory)

        saveListToJson(split<ContentCategorySchema>(schemaList, size), "contentCategorySchema.", destinationDirectory)
        saveListToJson(split<ContentCategory>(categoryList, size), "contentCategory.", destinationDirectory)
        saveListToJson(split<ContentEntryContentCategoryJoin>(categoryJoinList, size), "contentEntryContentCategoryJoin.", destinationDirectory)

        saveListToJson(split<Language>(langList, size), "language.", destinationDirectory)
        saveListToJson(split<LanguageVariant>(langVariantList, size), "languageVariant.", destinationDirectory)

        saveListToJson(split<Container>(containerList, size), "container.", destinationDirectory)

        FileUtils.writeStringToFile(File(destination, "index.json"), gson!!.toJson(pathList), UTF_ENCODING)

    }

    private fun <T> saveListToJson(listOfLists: Collection<List<T>>, nameOfFile: String, destinationDirectory: File): Collection<List<T>> {

        val iterator = listOfLists.iterator()
        UMLogUtil.logDebug(nameOfFile + " was split into " + listOfLists.size)
        var count = 0
        while (iterator.hasNext()) {

            val fileName = nameOfFile + count++ + JSON_EXT
            val file = File(destinationDirectory, fileName)
            pathList!!.add(fileName)
            try {
                FileUtils.writeStringToFile(file, gson!!.toJson(iterator.next()), UTF_ENCODING)
            } catch (e: IOException) {
                UMLogUtil.logError("Error saving file $nameOfFile$count")
            }

        }
        return listOfLists
    }

    private fun <T> split(list: List<T>, size: Int): Collection<List<T>> {
        val counter = AtomicInteger(0)

        return list.stream().collect<Map<Int, List<T>>, Any>(Collectors.groupingBy { it -> counter.getAndIncrement() / size })
                .values

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                System.err.println("Usage: <file destination><optional max size of entry><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }

            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo(args[0])
            val size = if (args.size == 2) Integer.parseInt(args[1]) else 1000

            try {
                ExportData().export(File(args[0]), size)
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            }


        }
    }


}
