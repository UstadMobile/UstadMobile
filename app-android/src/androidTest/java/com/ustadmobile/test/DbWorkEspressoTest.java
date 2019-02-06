package com.ustadmobile.test;

import android.support.test.InstrumentationRegistry;

import com.google.common.util.concurrent.ListenableFuture;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.port.android.impl.DbInitialEntriesInserter;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

public class DbWorkEspressoTest {


    private static final String TEST_WORK_TAG = "DbEspressoWorkTest";

    //@Test
    public void givenAnExportWork_insertAllDataIntoDb_CheckIfWorkWasSuccessful(){
//        UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext()).clearAllTables();
//        WorkManager workManager = WorkManager.getInstance();
//        workManager.cancelAllWork();
//
//
//        OneTimeWorkRequest dbWork =
//                new OneTimeWorkRequest.Builder(DbInitialEntriesInserter.class)
//                        .addTag(TEST_WORK_TAG)
//                        .build();
//
//        workManager.enqueue(dbWork);
//
//        ListenableFuture<List<WorkInfo>> future = workManager.getWorkInfosByTag(TEST_WORK_TAG);
//        try {
//            List<WorkInfo> list = future.get();
//            WorkInfo work = list.get(0);
//            boolean isCompleted = work.getState().isFinished();
//            Assert.assertEquals(true, isCompleted);
//            workManager.cancelAllWorkByTag(TEST_WORK_TAG);
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    @Test
    public void givenBlankDatabase_whenInitialInsertDone_thenEntriesShouldBePresent() {
        UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext()).clearAllTables();
        DbInitialEntriesInserter worker = new DbInitialEntriesInserter(
                InstrumentationRegistry.getTargetContext());
        ListenableWorker.Result result = worker.doWork();
        Assert.assertEquals(ListenableWorker.Result.success(), result);
    }

}
