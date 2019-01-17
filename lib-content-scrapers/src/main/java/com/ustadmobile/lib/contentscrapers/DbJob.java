package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.List;

public class DbJob {

    public static void main(String[] args) {

        UMLogUtil.setLevel("info");
        try {
            new DbJob().doJob();
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Main method exception catch do Job");
        }
    }


    public void doJob() {

        UmAppDatabase db = UmAppDatabase.getInstance(null);

        ContentEntryFileStatusDao statusDao = db.getContentEntryFileStatusDao();
        List<ContentEntryFileStatus> khanList = statusDao.findKhan();
        UMLogUtil.logInfo("Size " + khanList.size());
        for (ContentEntryFileStatus khanFile : khanList) {

            StringBuilder khanFilePath = new StringBuilder(khanFile.getFilePath());

            UMLogUtil.logInfo("Before " + khanFilePath.toString());

            int startIndex = khanFilePath.indexOf("/en/") + 4;
            String[] split = khanFile.getFilePath().split("/");
            int lastIndex = khanFilePath.indexOf(split[split.length - 2]);
            khanFilePath.delete(startIndex, lastIndex);

            String finalString = khanFilePath.toString();

            UMLogUtil.logInfo("After " + finalString);

            khanFile.setFilePath(finalString);
            statusDao.update(khanFile);
        }

    }

}
