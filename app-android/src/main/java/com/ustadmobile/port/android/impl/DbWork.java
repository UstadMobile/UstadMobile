package com.ustadmobile.port.android.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DbWork extends Worker {

    public DbWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            UmAppDatabase appDatabase = UmAppDatabase.getInstance(getApplicationContext());

            if(appDatabase.getLanguageDao().totalLanguageCount() > 0){
                return Result.success();
            }

            Gson gson = new GsonBuilder().create();
            InputStream result = getApplicationContext().getAssets().open("db/index.json");
            String data = UMIOUtils.readStreamToString(result);
            List<String> entryDataList = gson.fromJson(data, new TypeToken<List<String>>() {
            }.getType());

            for (String entryData : entryDataList) {

                InputStream fileResult = getApplicationContext().getAssets().open("db/" + entryData);
                String contentData = UMIOUtils.readStreamToString(fileResult);

                if (entryData.startsWith("languageVariant.")) {
                    List<LanguageVariant> resultList = gson.fromJson(contentData, new TypeToken<List<LanguageVariant>>() {
                    }.getType());
                    appDatabase.getLanguageVariantDao().insertList(resultList);
                } else if (entryData.startsWith("language.")) {
                    List<Language> resultList = gson.fromJson(contentData, new TypeToken<List<Language>>() {
                    }.getType());
                    appDatabase.getLanguageDao().insertList(resultList);
                }else if (entryData.startsWith("contentCategorySchema.")) {
                    List<ContentCategorySchema> resultList = gson.fromJson(contentData, new TypeToken<List<ContentCategorySchema>>() {
                    }.getType());
                    appDatabase.getContentCategorySchemaDao().insertList(resultList);
                }else if (entryData.startsWith("contentEntryContentCategoryJoin.")) {
                    List<ContentEntryContentCategoryJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryContentCategoryJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryContentCategoryJoinDao().insertList(resultList);
                }else if (entryData.startsWith("contentCategory.")) {
                    List<ContentCategory> resultList = gson.fromJson(contentData, new TypeToken<List<ContentCategory>>() {
                    }.getType());
                    appDatabase.getContentCategoryDao().insertList(resultList);
                }else if (entryData.startsWith("contentEntryContentEntryFileJoin.")) {
                    List<ContentEntryContentEntryFileJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryContentEntryFileJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryContentEntryFileJoinDao().insertList(resultList);
                }else if (entryData.startsWith("contentEntryFile.")) {
                    List<ContentEntryFile> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryFile>>() {
                    }.getType());
                    appDatabase.getContentEntryFileDao().insertList(resultList);
                }else if (entryData.startsWith("contentEntryRelatedEntryJoin.")) {
                    List<ContentEntryRelatedEntryJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryRelatedEntryJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryRelatedEntryJoinDao().insertList(resultList);
                }else if (entryData.startsWith("contentEntryParentChildJoin.")) {
                    List<ContentEntryParentChildJoin> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntryParentChildJoin>>() {
                    }.getType());
                    appDatabase.getContentEntryParentChildJoinDao().insertList(resultList);
                }else if (entryData.startsWith("contentEntry.")) {
                    List<ContentEntry> resultList = gson.fromJson(contentData, new TypeToken<List<ContentEntry>>() {
                    }.getType());
                    appDatabase.getContentEntryDao().insertList(resultList);
                }


            }

        } catch (IOException e) {
            return Result.failure();
        }


        return Result.success();
    }
}
