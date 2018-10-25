package com.ustadmobile.test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.port.android.view.DummyActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ContentEntryEspressoTest {

    @Rule
    public IntentsTestRule<DummyActivity> mActivityRule =
            new IntentsTestRule<>(DummyActivity.class, false, false);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION);


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

        ContentEntry entry = new ContentEntry();
        entry.setContentEntryUid(1);
        entry.setTitle("Ustad Mobile");
        contentDao.insert(entry);


        ContentEntry ck12 = new ContentEntry();
        ck12.setContentEntryUid(2);
        ck12.setTitle("Ck-12 Foundation");
        contentDao.insert(ck12);
       // long ck12uuid = contentDao.insert(ck12);
        //ck12.setContentEntryUid(ck12uuid);

        ContentEntryParentChildJoin parentChildJoin = new ContentEntryParentChildJoin();
        parentChildJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentChildJoin.setCepcjChildContentEntryUid(ck12.getContentEntryUid());
        parentChildJoin.setChildIndex(0);
        parentChildJoin.setCepcjUid(1);
        pcjdao.insert(parentChildJoin);

        ContentEntry grade5parent = new ContentEntry();
        grade5parent.setContentEntryUid(3);
        grade5parent.setTitle("Grade 1-5");
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
        contentDao.insert(quiz);

        ContentEntryParentChildJoin NumberQuizJoin = new ContentEntryParentChildJoin();
        NumberQuizJoin.setCepcjParentContentEntryUid(wholenumbers.getContentEntryUid());
        NumberQuizJoin.setCepcjChildContentEntryUid(quiz.getContentEntryUid());
        NumberQuizJoin.setChildIndex(0);
        NumberQuizJoin.setCepcjUid(7);
        pcjdao.insert(NumberQuizJoin);

        List<ContentEntry> list = contentDao.getChildrenByParentUidTest(entry.getContentEntryUid());
        list.size();

        Intent launchActivityIntent = new Intent();
        mActivityRule.launchActivity(launchActivityIntent);


    }


}
