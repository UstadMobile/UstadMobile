package com.ustadmobile.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEntryListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.view.ContentEntryView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.android.view.ContentEntryDetailActivity;
import com.ustadmobile.port.android.view.ContentEntryListActivity;
import com.ustadmobile.port.android.view.DummyActivity;

import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class ContentEntryListEspressoTest {

    @Rule
    public IntentsTestRule<ContentEntryListActivity> mActivityRule =
            new IntentsTestRule<>(ContentEntryListActivity.class, false, false);

    public static final long ROOT_CONTENT_ENTRY_UID = 1L;

    @Before
    public void before() {
        initDb();
        launchActivity();
    }

    public void launchActivity() {
        Intent launchActivityIntent = new Intent();
        launchActivityIntent.putExtra(ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID,
                ROOT_CONTENT_ENTRY_UID);
        mActivityRule.launchActivity(launchActivityIntent);
    }

    public void initDb(){
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao pcjdao = repo.getContentEntryParentChildJoinDao();
        ContentEntryFileDao contentFileDao = repo.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao = repo.getContentEntryContentEntryFileJoinDao();
        ContentEntryRelatedEntryJoinDao contentEntryRelatedEntryJoinDao = repo.getContentEntryRelatedEntryJoinDao();

        ContentEntry entry = new ContentEntry();
        entry.setContentEntryUid(ROOT_CONTENT_ENTRY_UID);
        entry.setTitle("Ustad Mobile");
        contentDao.insert(entry);

        ContentEntry ck12 = new ContentEntry();
        ck12.setContentEntryUid(2);
        ck12.setTitle("Ck-12 Foundation");
        ck12.setDescription("All content");
        ck12.setThumbnailUrl("https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png");
        ck12.setLeaf(false);
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
        grade5parent.setLeaf(false);
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
        grade1child.setLeaf(false);
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
        wholenumbers.setLeaf(false);
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
        quiz.setLeaf(true);
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

        ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
        fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        fileJoin.setCecefjContentEntryUid(quiz.getContentEntryUid());
        fileJoin.setCecefjUid(9);
        contentEntryFileJoinDao.insert(fileJoin);

        ContentEntryRelatedEntryJoin englishEnglishJoin = new ContentEntryRelatedEntryJoin();
        englishEnglishJoin.setCerejContentEntryUid(quiz.getContentEntryUid());
        englishEnglishJoin.setCerejRelatedEntryUid(quiz.getContentEntryUid());
        englishEnglishJoin.setCerejUid(18);
        englishEnglishJoin.setRelType(ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);
        contentEntryRelatedEntryJoinDao.insert(englishEnglishJoin);

        // arabic
        ContentEntry arabicQuiz = new ContentEntry();
        arabicQuiz.setContentEntryUid(10);
        arabicQuiz.setTitle("وقت الاختبار");
        arabicQuiz.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        arabicQuiz.setDescription("كل المحتوى");
        arabicQuiz.setPublisher("CK12");
        arabicQuiz.setAuthor("حفلة");
        contentDao.insert(arabicQuiz);

        ContentEntryFile updatedFile = new ContentEntryFile();
        updatedFile.setMimeType("application/zip");
        updatedFile.setFileSize(10);
        updatedFile.setLastModified(1540728218);
        updatedFile.setContentEntryFileUid(11);
        contentFileDao.insert(updatedFile);

        ContentEntryContentEntryFileJoin sameFileJoin = new ContentEntryContentEntryFileJoin();
        sameFileJoin.setCecefjContentEntryFileUid(updatedFile.getContentEntryFileUid());
        sameFileJoin.setCecefjContentEntryUid(arabicQuiz.getContentEntryUid());
        sameFileJoin.setCecefjUid(12);
        contentEntryFileJoinDao.insert(sameFileJoin);

        ContentEntryRelatedEntryJoin arabicEnglishJoin = new ContentEntryRelatedEntryJoin();
        arabicEnglishJoin.setCerejContentEntryUid(quiz.getContentEntryUid());
        arabicEnglishJoin.setCerejRelatedEntryUid(arabicQuiz.getContentEntryUid());
        arabicEnglishJoin.setCerejUid(13);
        arabicEnglishJoin.setRelType(ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);
        contentEntryRelatedEntryJoinDao.insert(arabicEnglishJoin);


        ContentEntry spanishQuiz = new ContentEntry();
        spanishQuiz.setContentEntryUid(14);
        spanishQuiz.setTitle("tiempo de prueba");
        spanishQuiz.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        spanishQuiz.setDescription("todo el contenido");
        spanishQuiz.setPublisher("CK12");
        spanishQuiz.setAuthor("borrachera");
        contentDao.insert(spanishQuiz);

        ContentEntryFile spanishFile = new ContentEntryFile();
        spanishFile.setMimeType("application/zip");
        spanishFile.setFileSize(10);
        spanishFile.setLastModified(1540728218);
        spanishFile.setContentEntryFileUid(15);
        contentFileDao.insert(spanishFile);

        ContentEntryContentEntryFileJoin spanishFileJoin = new ContentEntryContentEntryFileJoin();
        spanishFileJoin.setCecefjContentEntryFileUid(spanishFile.getContentEntryFileUid());
        spanishFileJoin.setCecefjContentEntryUid(spanishQuiz.getContentEntryUid());
        spanishFileJoin.setCecefjUid(16);
        contentEntryFileJoinDao.insert(spanishFileJoin);

        ContentEntryRelatedEntryJoin spanishEnglishJoin = new ContentEntryRelatedEntryJoin();
        spanishEnglishJoin.setCerejContentEntryUid(quiz.getContentEntryUid());
        spanishEnglishJoin.setCerejRelatedEntryUid(spanishQuiz.getContentEntryUid());
        spanishEnglishJoin.setCerejUid(17);
        spanishEnglishJoin.setRelType(ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);
        contentEntryRelatedEntryJoinDao.insert(spanishEnglishJoin);



    }

    @Test
    public void givenContentEntryPresent_whenOpened_entryIsDisplayed() {

        onView(allOf(withId(R.id.content_entry_list),isDisplayed()))
                .check(matches(hasDescendant(withText("Ck-12 Foundation"))));

    }


    @Test
    public void givenContentEntryWithChildrenPresent_whenEntryClicked_intentToViewListIsFired() {
        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        intended(allOf(
                hasComponent(ContentEntryListActivity.class.getCanonicalName()),
                hasExtra(equalTo(ARG_CONTENT_ENTRY_UID),
                        equalTo(2l)
        )));
    }

    @Test
    public void givenContentEntryLeafPresent_whenEntryClicked_intentToViewDetailIsFired() {
        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        intended(allOf(
                hasComponent(ContentEntryDetailActivity.class.getCanonicalName()),
                hasExtra(equalTo(ARG_CONTENT_ENTRY_UID),
                        equalTo(6l)
                )));

    }


}
