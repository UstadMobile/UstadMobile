package com.ustadmobile.test;

import com.google.common.util.concurrent.ListenableFuture;
import com.ustadmobile.port.android.impl.DbWork;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class DbWorkEspressoTest {


    @Test
    public void givenAnExportWork_insertAllDataIntoDb_CheckIfWorkWasSuccessful(){
        OneTimeWorkRequest dbWork =
                new OneTimeWorkRequest.Builder(DbWork.class)
                        .addTag("Espresso")
                        .build();
        WorkManager workManager = WorkManager.getInstance();
        workManager.enqueue(dbWork);

        ListenableFuture<List<WorkInfo>> future = workManager.getWorkInfosByTag("Espresso");
        try {
            List<WorkInfo> list = future.get();
            WorkInfo work = list.get(0);
            boolean isCompleted = work.getState().isFinished();
            Assert.assertEquals(true, isCompleted);
            workManager.cancelAllWorkByTag("Espresso");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
