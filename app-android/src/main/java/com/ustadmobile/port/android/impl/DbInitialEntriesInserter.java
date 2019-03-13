package com.ustadmobile.port.android.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DbInitialEntriesInserter {

    private Context context;

    public static class DbInitialEntriesInserterWorker extends Worker{

        public DbInitialEntriesInserterWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            return new DbInitialEntriesInserter(getApplicationContext()).doWork();
        }
    }



    public DbInitialEntriesInserter(@NonNull Context context) {
        this.context = context;
    }

    public ListenableWorker.Result doWork() {
        try {
            Log.d("Db", "Inserting");
            UmAppDatabase appDatabase = UmAppDatabase.getInstance(context);

            if (appDatabase.getContainerDao().findAllPublikContainers().size() > 0) {
                return ListenableWorker.Result.success();
            }

            Gson gson = new GsonBuilder().create();
            InputStream result = context.getAssets().open("db/index.json");
            String data = UMIOUtils.readStreamToString(result);
            List<String> entryDataList = gson.fromJson(data, new TypeToken<List<String>>() {
            }.getType());

            for (String entryData : entryDataList) {

                InputStream fileResult = context.getAssets().open("db/" + entryData);
                String contentData = UMIOUtils.readStreamToString(fileResult);

                if (entryData.startsWith("languageVariant.")) {
                    List<LanguageVariant> resultList = gson.fromJson(contentData, new TypeToken<List<LanguageVariant>>() {
                    }.getType());
                    appDatabase.getLanguageVariantDao().insertList(resultList);
                } else if (entryData.startsWith("language.")) {
                    List<Language> resultList = gson.fromJson(contentData, new TypeToken<List<Language>>() {
                    }.getType());
                    appDatabase.getLanguageDao().insertList(resultList);
                } else if (entryData.startsWith("contentCategorySchema.")) {
                    List<ContentCategorySchema> resultList = gson.fromJson(contentData, new TypeToken<List<ContentCategorySchema>>() {
                    }.getType());
                    appDatabase.getContentCategorySchemaDao().insertList(resultList);
                } else if (entryData.startsWith("contentEntryContentCategoryJoin.")) {
                    List<ContentEntryContentCategoryJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryContentCategoryJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryContentCategoryJoinDao().insertList(resultList);
                } else if (entryData.startsWith("contentCategory.")) {
                    List<ContentCategory> resultList = gson.fromJson(contentData, new TypeToken<List<ContentCategory>>() {
                    }.getType());
                    appDatabase.getContentCategoryDao().insertList(resultList);
                } else if (entryData.startsWith("container.")) {
                    List<Container> resultList = gson.fromJson(contentData, new TypeToken<List<Container>>() {
                    }.getType());
                    appDatabase.getContainerDao().insertList(resultList);
                } else if (entryData.startsWith("contentEntryRelatedEntryJoin.")) {
                    List<ContentEntryRelatedEntryJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryRelatedEntryJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryRelatedEntryJoinDao().insertList(resultList);
                } else if (entryData.startsWith("contentEntryParentChildJoin.")) {
                    List<ContentEntryParentChildJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryParentChildJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryParentChildJoinDao().insertList(resultList);
                } else if (entryData.startsWith("contentEntry.")) {
                    List<ContentEntry> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntry>>() {
                    }.getType());
                    HashSet<Long> uniques = new HashSet<>();
                    for (ContentEntry entry : resultList) {
                        boolean isAdded = uniques.add(entry.getContentEntryUid());
                        if (!isAdded) {
                            Log.d("Export", "content entry id was found again " + entry.getContentEntryUid());
                        }
                    }
                    appDatabase.getContentEntryDao().insertList(resultList);
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
            return ListenableWorker.Result.failure();
        }


        return ListenableWorker.Result.success();
    }
}
