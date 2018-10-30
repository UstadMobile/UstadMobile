package com.ustadmobile.test;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.port.android.view.DummyActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ContentEntryEspressoTest {

    @Rule
    public IntentsTestRule<DummyActivity> mActivityRule =
            new IntentsTestRule<>(DummyActivity.class, false, false);

    @Before
    public void beforeTest() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        //Start the activity


    }

    @Test
    public void givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);

        db.clearAllTables();

        ContentEntryDao contentDao = db.getContentEntryDao();
        ContentEntryParentChildJoinDao pcjdao = db.getContentEntryParentChildJoinDao();
        ContentEntryFileDao contentFileDao = db.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao = db.getContentEntryContentEntryFileJoinDao();

        ContentEntry entry = new ContentEntry();
        entry.setContentEntryUid(1);
        entry.setTitle("Ustad Mobile");
        contentDao.insert(entry);


        ContentEntry ck12 = new ContentEntry();
        ck12.setContentEntryUid(2);
        ck12.setTitle("Ck-12 Foundation");
        ck12.setDescription("All content");
        ck12.setThumbnailUrl("https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png");
        contentDao.insert(ck12);

        ContentEntryParentChildJoin parentChildJoin = new ContentEntryParentChildJoin();
        parentChildJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentChildJoin.setCepcjChildContentEntryUid(ck12.getContentEntryUid());
        parentChildJoin.setChildIndex(0);
        parentChildJoin.setCepcjUid(1);
        pcjdao.insert(parentChildJoin);

        ContentEntry grade5parent = new ContentEntry();
        grade5parent.setContentEntryUid(3);
        grade5parent.setTitle("Grade 1-5");
        grade5parent.setThumbnailUrl("https://phet.colorado.edu/images/phet-social-media-logo.png");
        contentDao.insert(grade5parent);

        ContentEntryParentChildJoin subjectgradejoin = new ContentEntryParentChildJoin();
        subjectgradejoin.setCepcjParentContentEntryUid(ck12.getContentEntryUid());
        subjectgradejoin.setCepcjChildContentEntryUid(grade5parent.getContentEntryUid());
        subjectgradejoin.setChildIndex(0);
        subjectgradejoin.setCepcjUid(2);
        pcjdao.insert(subjectgradejoin);

        ContentEntry grade1child = new ContentEntry();
        grade1child.setContentEntryUid(4);
        grade1child.setTitle("Grade 1");
        grade1child.setThumbnailUrl("https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png");
        contentDao.insert(grade1child);

        ContentEntryParentChildJoin gradeChildJoin = new ContentEntryParentChildJoin();
        gradeChildJoin.setCepcjParentContentEntryUid(grade5parent.getContentEntryUid());
        gradeChildJoin.setCepcjChildContentEntryUid(grade1child.getContentEntryUid());
        gradeChildJoin.setChildIndex(0);
        gradeChildJoin.setCepcjUid(3);
        pcjdao.insert(gradeChildJoin);

        ContentEntry wholenumbers = new ContentEntry();
        wholenumbers.setContentEntryUid(5);
        wholenumbers.setTitle("Whole Numbers");
        wholenumbers.setThumbnailUrl("https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png");
        contentDao.insert(wholenumbers);

        ContentEntryParentChildJoin GradeNumberJoin = new ContentEntryParentChildJoin();
        GradeNumberJoin.setCepcjParentContentEntryUid(grade1child.getContentEntryUid());
        GradeNumberJoin.setCepcjChildContentEntryUid(wholenumbers.getContentEntryUid());
        GradeNumberJoin.setChildIndex(0);
        GradeNumberJoin.setCepcjUid(4);
        pcjdao.insert(GradeNumberJoin);

        ContentEntry quiz = new ContentEntry();
        quiz.setContentEntryUid(6);
        quiz.setTitle("Quiz Time");
        quiz.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        quiz.setDescription("All content");
        quiz.setPublisher("CK12");
        quiz.setAuthor("Binge");
        contentDao.insert(quiz);

        ContentEntryParentChildJoin NumberQuizJoin = new ContentEntryParentChildJoin();
        NumberQuizJoin.setCepcjParentContentEntryUid(wholenumbers.getContentEntryUid());
        NumberQuizJoin.setCepcjChildContentEntryUid(quiz.getContentEntryUid());
        NumberQuizJoin.setChildIndex(0);
        NumberQuizJoin.setCepcjUid(7);
        pcjdao.insert(NumberQuizJoin);

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setMimeType("application/zip");
        contentEntryFile.setFileSize(10);
        contentEntryFile.setLastModified(1540728217);
        contentEntryFile.setContentEntryFileUid(8);
        contentFileDao.insert(contentEntryFile);

        ContentEntryFile updatedFile = new ContentEntryFile();
        updatedFile.setMimeType("application/zip");
        updatedFile.setFileSize(10);
        updatedFile.setLastModified(1540728218);
        updatedFile.setContentEntryFileUid(9);
        contentFileDao.insert(updatedFile);

        ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
        fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        fileJoin.setCecefjContentEntryUid(quiz.getContentEntryUid());
        fileJoin.setCecefjUid(10);
        contentEntryFileJoinDao.insert(fileJoin);

        ContentEntryContentEntryFileJoin sameFileJoin = new ContentEntryContentEntryFileJoin();
        sameFileJoin.setCecefjContentEntryFileUid(updatedFile.getContentEntryFileUid());
        sameFileJoin.setCecefjContentEntryUid(quiz.getContentEntryUid());
        sameFileJoin.setCecefjUid(11);
        contentEntryFileJoinDao.insert(sameFileJoin);

        Intent launchActivityIntent = new Intent();
        mActivityRule.launchActivity(launchActivityIntent);

    }


}
