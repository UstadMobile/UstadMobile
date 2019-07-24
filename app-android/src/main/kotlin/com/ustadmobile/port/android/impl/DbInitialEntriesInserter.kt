package com.ustadmobile.port.android.impl

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.*
import java.util.*

class DbInitialEntriesInserter(private val context: Context) {

    class DbInitialEntriesInserterWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        override fun doWork(): Result {
            return DbInitialEntriesInserter(applicationContext).doWork()
        }
    }

    fun doWork(): ListenableWorker.Result {
        try {
            Log.d("Db", "Inserting")
            val appDatabase = UmAppDatabase.getInstance(context)

            if (appDatabase.containerDao.findAllPublikContainers().isNotEmpty()) {
                return ListenableWorker.Result.success()
            }

            val gson = GsonBuilder().create()
            val result = context.assets.open("db/index.json")
            val data = UMIOUtils.readStreamToString(result)
            val entryDataList = gson.fromJson<List<String>>(data, object : TypeToken<List<String>>() {

            }.type)

            for (entryData in entryDataList) {

                val fileResult = context.assets.open("db/$entryData")
                val contentData = UMIOUtils.readStreamToString(fileResult)

                when {
                    entryData.startsWith("languageVariant.") -> {
                        val resultList = gson.fromJson<List<LanguageVariant>>(contentData, object : TypeToken<List<LanguageVariant>>() {

                        }.type)
                        appDatabase.languageVariantDao.insertList(resultList)
                    }
                    entryData.startsWith("language.") -> {
                        val resultList = gson.fromJson<List<Language>>(contentData, object : TypeToken<List<Language>>() {

                        }.type)
                        appDatabase.languageDao.insertList(resultList)
                    }
                    entryData.startsWith("contentCategorySchema.") -> {
                        val resultList = gson.fromJson<List<ContentCategorySchema>>(contentData, object : TypeToken<List<ContentCategorySchema>>() {

                        }.type)
                        appDatabase.contentCategorySchemaDao.insertList(resultList)
                    }
                    entryData.startsWith("contentEntryContentCategoryJoin.") -> {
                        val resultList = gson.fromJson<List<ContentEntryContentCategoryJoin>>(contentData, object : TypeToken<List<ContentEntryContentCategoryJoin>>() {

                        }.type)
                        appDatabase.contentEntryContentCategoryJoinDao.insertList(resultList)
                    }
                    entryData.startsWith("contentCategory.") -> {
                        val resultList = gson.fromJson<List<ContentCategory>>(contentData, object : TypeToken<List<ContentCategory>>() {

                        }.type)
                        appDatabase.contentCategoryDao.insertList(resultList)
                    }
                    entryData.startsWith("container.") -> {
                        val resultList = gson.fromJson<List<Container>>(contentData, object : TypeToken<List<Container>>() {

                        }.type)
                        appDatabase.containerDao.insertList(resultList)
                    }
                    entryData.startsWith("contentEntryRelatedEntryJoin.") -> {
                        val resultList = gson.fromJson<List<ContentEntryRelatedEntryJoin>>(contentData, object : TypeToken<List<ContentEntryRelatedEntryJoin>>() {

                        }.type)
                        appDatabase.contentEntryRelatedEntryJoinDao.insertList(resultList)
                    }
                    entryData.startsWith("contentEntryParentChildJoin.") -> {
                        val resultList = gson.fromJson<List<ContentEntryParentChildJoin>>(contentData, object : TypeToken<List<ContentEntryParentChildJoin>>() {

                        }.type)
                        appDatabase.contentEntryParentChildJoinDao.insertList(resultList)
                    }
                    entryData.startsWith("contentEntry.") -> {
                        val resultList = gson.fromJson<List<ContentEntry>>(contentData, object : TypeToken<List<ContentEntry>>() {

                        }.type)
                        val uniques = HashSet<Long>()
                        for (entry in resultList) {
                            val isAdded = uniques.add(entry.contentEntryUid)
                            if (!isAdded) {
                                Log.d("Export", "content entry id was found again " + entry.contentEntryUid)
                            }
                        }
                        appDatabase.contentEntryDao.insertList(resultList)
                    }
                }


            }

        } catch (e: Exception) {
            e.printStackTrace()
            return ListenableWorker.Result.failure()
        }


        return ListenableWorker.Result.success()
    }
}
