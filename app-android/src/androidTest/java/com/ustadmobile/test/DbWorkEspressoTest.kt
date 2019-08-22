package com.ustadmobile.test

import androidx.test.InstrumentationRegistry
import androidx.work.ListenableWorker
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.port.android.impl.DbInitialEntriesInserter
import com.ustadmobile.test.core.impl.PlatformTestUtil
import org.junit.Assert
import org.junit.Test

class DbWorkEspressoTest {

    //@Test
    fun givenAnExportWork_insertAllDataIntoDb_CheckIfWorkWasSuccessful() {
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
    fun givenBlankDatabase_whenInitialInsertDone_thenEntriesShouldBePresent() {
        UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext()).clearAllTables()
        val worker = DbInitialEntriesInserter(
                InstrumentationRegistry.getTargetContext())
        val result = worker.doWork()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    companion object {


        private const val TEST_WORK_TAG = "DbEspressoWorkTest"
    }

}
