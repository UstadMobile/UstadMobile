package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEntryListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.android.view.ContentEntryDetailActivity;

import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.core.controller.ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class ContentEntryDetailEspressoTest {

    @Rule
    public IntentsTestRule<ContentEntryDetailActivity> mActivityRule =
            new IntentsTestRule<>(ContentEntryDetailActivity.class, false, false);

    public UmAppDatabase getDb(){
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        return db.getRepository("https://localhost", "");
    }

    public void createDummyContent(){
        UmAppDatabase repo = getDb();

        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContentEntryFileDao contentFileDao = repo.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao = repo.getContentEntryContentEntryFileJoinDao();
        ContentEntryRelatedEntryJoinDao contentEntryRelatedEntryJoinDao = repo.getContentEntryRelatedEntryJoinDao();
        LanguageDao languageDao = repo.getLanguageDao();

        Language englishLang = new Language();
        englishLang.setLangUid(1);
        englishLang.setName("English");
        englishLang.setIso_639_1_standard("en");
        englishLang.setIso_639_2_standard("eng");
        englishLang.setIso_639_3_standard("eng");
        languageDao.insert(englishLang);

        Language arabicLang = new Language();
        arabicLang.setLangUid(2);
        arabicLang.setName("Arabic");
        arabicLang.setIso_639_1_standard("ar");
        arabicLang.setIso_639_2_standard("ara");
        arabicLang.setIso_639_3_standard("ara");
        languageDao.insert(arabicLang);

        Language spanishLang = new Language();
        spanishLang.setLangUid(3);
        spanishLang.setName("Spanish");
        spanishLang.setIso_639_1_standard("es");
        spanishLang.setIso_639_2_standard("esp");
        spanishLang.setIso_639_3_standard("esp");
        languageDao.insert(spanishLang);


        ContentEntry quiz = new ContentEntry();
        quiz.setContentEntryUid(6);
        quiz.setTitle("Quiz Time");
        quiz.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        quiz.setDescription("All content");
        quiz.setPublisher("CK12");
        quiz.setAuthor("Binge");
        quiz.setPrimaryLanguageUid(1);
        quiz.setLeaf(true);
        contentDao.insert(quiz);

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

        // arabic
        ContentEntry arabicQuiz = new ContentEntry();
        arabicQuiz.setContentEntryUid(10);
        arabicQuiz.setTitle("وقت الاختبار");
        arabicQuiz.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        arabicQuiz.setDescription("كل المحتوى");
        arabicQuiz.setPublisher("CK12");
        arabicQuiz.setAuthor("حفلة");
        arabicQuiz.setPrimaryLanguageUid(2);
        arabicQuiz.setLeaf(true);
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
        arabicEnglishJoin.setCerejRelLanguageUid(2);
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
        spanishQuiz.setPrimaryLanguageUid(3);
        spanishQuiz.setLeaf(true);
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
        spanishEnglishJoin.setCerejRelLanguageUid(3);
        spanishEnglishJoin.setCerejUid(17);
        spanishEnglishJoin.setRelType(ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);
        contentEntryRelatedEntryJoinDao.insert(spanishEnglishJoin);

    }

    @Test
    public void givenContentEntryDetailPresent_whenOpened_entryIsDisplayed() {
        createDummyContent();

        Intent launchActivityIntent = new Intent();
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 6l);
        mActivityRule.launchActivity(launchActivityIntent);

        onView(allOf(withId(R.id.entry_detail_title), withText("Quiz Time")));

        onView(allOf(withId(R.id.entry_detail_description), withText("All content")));

        onView(allOf(withId(R.id.entry_detail_author), withText("Binge")));

    }

    @Test
    public void givenContentEntryDetailPresent_whenTranslatedIsSelected_arabicEntryIsDisplayed() {
        createDummyContent();

        Intent launchActivityIntent = new Intent();
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 6l);
        mActivityRule.launchActivity(launchActivityIntent);

        onView(Matchers.allOf(isDisplayed(), withId(R.id.entry_detail_flex)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        intended(AllOf.allOf(
                hasComponent(ContentEntryDetailActivity.class.getCanonicalName()),
                hasExtra(equalTo(ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID),
                        equalTo(10l)
                )));

        onView(allOf(withId(R.id.entry_detail_title), withText("وقت الاختبار")));

    }



}
