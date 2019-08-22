package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.LANGUAGE_LIST_LOCATION;


/**
 * A list of all the ISO-639-3 language code can be found at https://iso639-3.sil.org/code_tables/639/data
 * and downloaded at https://iso639-3.sil.org/code_tables/download_tables
 *
 * The data is in .tab format that can be converted to JSON format( i converted to CSV first to modify fields)
 *
 */
public class LanguageList {

    public void addAllLanguages() throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        ArrayList<Language> langList = gson.fromJson(UMIOUtils.readStreamToString(
                getClass().getResourceAsStream(LANGUAGE_LIST_LOCATION)), new TypeToken<List<Language>>() {}.getType());

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        LanguageDao langDao = repository.getLanguageDao();
        if(langDao.totalLanguageCount() < 7000){
            langDao.insertList(langList);
        }
    }

}
