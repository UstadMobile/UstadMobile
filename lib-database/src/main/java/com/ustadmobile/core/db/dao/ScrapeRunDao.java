package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ScrapeRun;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

@UmDao
public abstract class ScrapeRunDao implements BaseDao<ScrapeRun> {

    public static final String SCRAPE_TYPE_KHAN = "khan";

    public static final String SCRAPE_TYPE_VOA = "voa";

    public static final String SCRAPE_TYPE_EDRAAK = "edraak";

    @UmQuery("SELECT scrapeRunUid From ScrapeRun WHERE scrapeType = :scrapeType LIMIT 1")
    public abstract int findPendingRunIdByScraperType(String scrapeType);
}
