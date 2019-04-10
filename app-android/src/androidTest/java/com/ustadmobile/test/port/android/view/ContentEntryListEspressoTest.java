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
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.android.view.ContentEntryDetailActivity;
import com.ustadmobile.port.android.view.ContentEntryListActivity;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
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
                String.valueOf(ROOT_CONTENT_ENTRY_UID));
        mActivityRule.launchActivity(launchActivityIntent);
    }

    public void initDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao pcjdao = repo.getContentEntryParentChildJoinDao();
        ContentEntryRelatedEntryJoinDao contentEntryRelatedEntryJoinDao = repo.getContentEntryRelatedEntryJoinDao();
        ContainerDao containerDao = repo.getContainerDao();
        LanguageDao langDao = repo.getLanguageDao();
        ContentCategorySchemaDao schemaDao = repo.getContentCategorySchemaDao();
        ContentCategoryDao categoryDao = repo.getContentCategoryDao();
        ContentEntryContentCategoryJoinDao categoryJoinDao = repo.getContentEntryContentCategoryJoinDao();

        ContentEntry entry = new ContentEntry();
        entry.setContentEntryUid(ROOT_CONTENT_ENTRY_UID);
        entry.setTitle("Ustad Mobile");
        contentDao.insert(entry);

        ContentEntry khanParent = new ContentEntry();
        khanParent.setContentEntryUid(2);
        khanParent.setTitle("Khan Academy");
        khanParent.setDescription("You can learn anything.\n" +
                "For free. For everyone. Forever.");
        khanParent.setThumbnailUrl("https://cdn.kastatic.org/images/khan-logo-dark-background.new.png");
        khanParent.setLeaf(false);
        contentDao.insert(khanParent);

        ContentEntry edraak = new ContentEntry();
        edraak.setContentEntryUid(20);
        edraak.setTitle("Edraak K12");
        edraak.setDescription("تعليم مجاني\nّ" +
                "إلكترونيّ باللغة العربيّة!" +
                "\nFree Online\n" +
                "Education, In Arabic!");
        edraak.setThumbnailUrl("https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png");
        edraak.setLeaf(false);
        contentDao.insert(edraak);

        ContentEntry prathamParent = new ContentEntry();
        prathamParent.setContentEntryUid(21);
        prathamParent.setTitle("Pratham Books");
        prathamParent.setDescription("Every Child in School & Learning Well");
        prathamParent.setThumbnailUrl("https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png");
        prathamParent.setLeaf(false);
        contentDao.insert(prathamParent);

        ContentEntry phetParent = new ContentEntry();
        phetParent.setContentEntryUid(22);
        phetParent.setTitle("Phet Interactive Simulations");
        phetParent.setDescription("INTERACTIVE SIMULATIONS\nFOR SCIENCE AND MATH");
        phetParent.setThumbnailUrl("https://phet.colorado.edu/images/phet-social-media-logo.png");
        phetParent.setLeaf(false);
        contentDao.insert(phetParent);


        ContentEntry voaParent = new ContentEntry();
        voaParent.setContentEntryUid(23);
        voaParent.setTitle("Voice of America - Learning English");
        voaParent.setDescription("Learn American English with English language lessons from Voice of America. " +
                "VOA Learning English helps you learn English with vocabulary, listening and " +
                "comprehension lessons through daily news and interactive English learning activities.");
        voaParent.setThumbnailUrl("https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png");
        voaParent.setLeaf(false);
        contentDao.insert(voaParent);


        ContentEntry folderParent = new ContentEntry();
        folderParent.setContentEntryUid(24);
        folderParent.setTitle("3asafeer");
        folderParent.setLeaf(false);
        contentDao.insert(folderParent);

        ContentEntry etekParent = new ContentEntry();
        etekParent.setContentEntryUid(25);
        etekParent.setTitle("eTekkatho");
        etekParent.setDescription("Educational resources for the Myanmar academic community");
        etekParent.setThumbnailUrl("http://www.etekkatho.org/img/logos/etekkatho-myanmar-lang.png");
        etekParent.setLeaf(false);
        contentDao.insert(etekParent);

        ContentEntry ddlParent = new ContentEntry();
        ddlParent.setContentEntryUid(26);
        ddlParent.setTitle("Darakht-e Danesh");
        ddlParent.setDescription("Free and open educational resources for Afghanistan");
        ddlParent.setThumbnailUrl("https://www.ddl.af/storage/files/logo-dd.png");
        ddlParent.setLeaf(false);
        contentDao.insert(ddlParent);

        ContentEntry ck12Parent = new ContentEntry();
        ck12Parent.setContentEntryUid(27);
        ck12Parent.setTitle("CK-12 Foundation");
        ck12Parent.setDescription("100% Free, Personalized Learning for Every Student");
        ck12Parent.setThumbnailUrl("https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png");
        ck12Parent.setLeaf(false);
        contentDao.insert(ck12Parent);

        ContentEntry asbParent = new ContentEntry();
        asbParent.setContentEntryUid(28);
        asbParent.setTitle("African Story Books");
        asbParent.setDescription("Open access to picture storybooks in the languages of Africa\nFor children’s literacy, enjoyment and imagination.");
        asbParent.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        asbParent.setLeaf(false);
        contentDao.insert(asbParent);

        ContentEntryParentChildJoin parentKhanJoin = new ContentEntryParentChildJoin();
        parentKhanJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentKhanJoin.setCepcjChildContentEntryUid(khanParent.getContentEntryUid());
        parentKhanJoin.setChildIndex(0);
        parentKhanJoin.setCepcjUid(1);
        pcjdao.insert(parentKhanJoin);

        ContentEntryParentChildJoin parentEdraakJoin = new ContentEntryParentChildJoin();
        parentEdraakJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentEdraakJoin.setCepcjChildContentEntryUid(edraak.getContentEntryUid());
        parentEdraakJoin.setChildIndex(1);
        parentEdraakJoin.setCepcjUid(29);
        pcjdao.insert(parentEdraakJoin);

        ContentEntryParentChildJoin parentPhetJoin = new ContentEntryParentChildJoin();
        parentPhetJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentPhetJoin.setCepcjChildContentEntryUid(phetParent.getContentEntryUid());
        parentPhetJoin.setChildIndex(2);
        parentPhetJoin.setCepcjUid(30);
        pcjdao.insert(parentPhetJoin);


        ContentEntryParentChildJoin parentAsbJoin = new ContentEntryParentChildJoin();
        parentAsbJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentAsbJoin.setCepcjChildContentEntryUid(asbParent.getContentEntryUid());
        parentAsbJoin.setChildIndex(3);
        parentAsbJoin.setCepcjUid(31);
        pcjdao.insert(parentAsbJoin);


        ContentEntryParentChildJoin parentCk12Join = new ContentEntryParentChildJoin();
        parentCk12Join.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentCk12Join.setCepcjChildContentEntryUid(ck12Parent.getContentEntryUid());
        parentCk12Join.setChildIndex(4);
        parentCk12Join.setCepcjUid(32);
        pcjdao.insert(parentCk12Join);


        ContentEntryParentChildJoin parentDdlJoin = new ContentEntryParentChildJoin();
        parentDdlJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentDdlJoin.setCepcjChildContentEntryUid(ddlParent.getContentEntryUid());
        parentDdlJoin.setChildIndex(5);
        parentDdlJoin.setCepcjUid(33);
        pcjdao.insert(parentDdlJoin);


        ContentEntryParentChildJoin parentEtekJoin = new ContentEntryParentChildJoin();
        parentEtekJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentEtekJoin.setCepcjChildContentEntryUid(etekParent.getContentEntryUid());
        parentEtekJoin.setChildIndex(6);
        parentEtekJoin.setCepcjUid(34);
        pcjdao.insert(parentEtekJoin);

        ContentEntryParentChildJoin parentFolderJoin = new ContentEntryParentChildJoin();
        parentFolderJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentFolderJoin.setCepcjChildContentEntryUid(folderParent.getContentEntryUid());
        parentFolderJoin.setChildIndex(7);
        parentFolderJoin.setCepcjUid(35);
        pcjdao.insert(parentFolderJoin);

        ContentEntryParentChildJoin parentPrathamJoin = new ContentEntryParentChildJoin();
        parentPrathamJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentPrathamJoin.setCepcjChildContentEntryUid(prathamParent.getContentEntryUid());
        parentPrathamJoin.setChildIndex(8);
        parentPrathamJoin.setCepcjUid(36);
        pcjdao.insert(parentPrathamJoin);

        ContentEntryParentChildJoin parentVoaJoin = new ContentEntryParentChildJoin();
        parentVoaJoin.setCepcjParentContentEntryUid(entry.getContentEntryUid());
        parentVoaJoin.setCepcjChildContentEntryUid(voaParent.getContentEntryUid());
        parentVoaJoin.setChildIndex(9);
        parentVoaJoin.setCepcjUid(37);
        pcjdao.insert(parentVoaJoin);

        ContentEntry grade5parent = new ContentEntry();
        grade5parent.setContentEntryUid(3);
        grade5parent.setTitle("Math");
        grade5parent.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png");
        grade5parent.setLeaf(false);
        contentDao.insert(grade5parent);

        ContentEntryParentChildJoin subjectgradejoin = new ContentEntryParentChildJoin();
        subjectgradejoin.setCepcjParentContentEntryUid(khanParent.getContentEntryUid());
        subjectgradejoin.setCepcjChildContentEntryUid(grade5parent.getContentEntryUid());
        subjectgradejoin.setChildIndex(0);
        subjectgradejoin.setCepcjUid(2);
        pcjdao.insert(subjectgradejoin);

        ContentEntry grade1child = new ContentEntry();
        grade1child.setContentEntryUid(4);
        grade1child.setTitle("Early Math");
        grade1child.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/early_math.png-314b80-128c.png");
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
        wholenumbers.setTitle("Counting");
        wholenumbers.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/counting.png-377815-128c.png");
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
        quiz.setTitle("Counting with small numbers");
        quiz.setDescription("Sal counts squirrels and horses.");
        quiz.setThumbnailUrl("https://cdn.kastatic.org/googleusercontent/NIWZLag0UtSxht8SlBeunPR6SxVmfNhkDCHoEobwSAqb3QAFMYTYvuna3yUiSYMoS_k4N10H6orz6hYJu7JzNeJ0Aw");
        quiz.setLeaf(true);
        contentDao.insert(quiz);

        ContentEntryParentChildJoin NumberQuizJoin = new ContentEntryParentChildJoin();
        NumberQuizJoin.setCepcjParentContentEntryUid(wholenumbers.getContentEntryUid());
        NumberQuizJoin.setCepcjChildContentEntryUid(quiz.getContentEntryUid());
        NumberQuizJoin.setChildIndex(0);
        NumberQuizJoin.setCepcjUid(7);
        pcjdao.insert(NumberQuizJoin);

        Container contentEntryFile = new Container();
        contentEntryFile.setMimeType("application/zip");
        contentEntryFile.setFileSize(10000);
        contentEntryFile.setLastModified(1540728217);
        contentEntryFile.setContainerContentEntryUid(8);
        containerDao.insert(contentEntryFile);

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

        Container updatedFile = new Container();
        updatedFile.setMimeType("application/zip");
        updatedFile.setFileSize(10);
        updatedFile.setLastModified(1540728218);
        updatedFile.setContainerContentEntryUid(11);
        containerDao.insert(updatedFile);

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

        Container spanishFile = new Container();
        spanishFile.setMimeType("application/zip");
        spanishFile.setFileSize(10000);
        spanishFile.setLastModified(1540728218);
        spanishFile.setContainerContentEntryUid(15);
        containerDao.insert(spanishFile);

        ContentEntryRelatedEntryJoin spanishEnglishJoin = new ContentEntryRelatedEntryJoin();
        spanishEnglishJoin.setCerejContentEntryUid(quiz.getContentEntryUid());
        spanishEnglishJoin.setCerejRelatedEntryUid(spanishQuiz.getContentEntryUid());
        spanishEnglishJoin.setCerejUid(17);
        spanishEnglishJoin.setRelType(ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);
        contentEntryRelatedEntryJoinDao.insert(spanishEnglishJoin);

        Language arabLang = new Language();
        arabLang.setName("Arabic");
        arabLang.setLangUid(14254);
        langDao.insert(arabLang);

        ContentEntry child2 = new ContentEntry();
        child2.setContentEntryUid(22542);
        child2.setTitle("Math Arabic");
        child2.setPrimaryLanguageUid(arabLang.getLangUid());
        child2.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png");
        child2.setLeaf(false);
        contentDao.insert(child2);

        Language spanishLang = new Language();
        spanishLang.setName("Spanish");
        spanishLang.setLangUid(14253);
        langDao.insert(spanishLang);

        ContentEntryParentChildJoin child2join = new ContentEntryParentChildJoin();
        child2join.setCepcjParentContentEntryUid(khanParent.getContentEntryUid());
        child2join.setCepcjChildContentEntryUid(child2.getContentEntryUid());
        child2join.setChildIndex(0);
        child2join.setCepcjUid(754);
        pcjdao.insert(child2join);

        ContentEntry child3 = new ContentEntry();
        child3.setContentEntryUid(324424);
        child3.setTitle("Math Spanish");
        child3.setPrimaryLanguageUid(spanishLang.getLangUid());
        child3.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png");
        child3.setLeaf(false);
        contentDao.insert(child3);

        ContentEntryParentChildJoin child3join = new ContentEntryParentChildJoin();
        child3join.setCepcjParentContentEntryUid(khanParent.getContentEntryUid());
        child3join.setCepcjChildContentEntryUid(child3.getContentEntryUid());
        child3join.setChildIndex(0);
        child3join.setCepcjUid(3537);
        pcjdao.insert(child3join);

        ContentEntry child4 = new ContentEntry();
        child4.setContentEntryUid(3535);
        child4.setTitle("Math");
        child4.setPrimaryLanguageUid(arabLang.getLangUid());
        child4.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png");
        child4.setLeaf(false);
        contentDao.insert(child4);

        ContentEntryParentChildJoin child4join = new ContentEntryParentChildJoin();
        child4join.setCepcjParentContentEntryUid(khanParent.getContentEntryUid());
        child4join.setCepcjChildContentEntryUid(child4.getContentEntryUid());
        child4join.setChildIndex(0);
        child4join.setCepcjUid(3532);
        pcjdao.insert(child4join);

        ContentEntry child5 = new ContentEntry();
        child5.setContentEntryUid(5353);
        child5.setTitle("Math");
        child5.setPrimaryLanguageUid(arabLang.getLangUid());
        child5.setThumbnailUrl("https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png");
        child5.setLeaf(false);
        contentDao.insert(child5);

        ContentEntryParentChildJoin child5join = new ContentEntryParentChildJoin();
        child5join.setCepcjParentContentEntryUid(khanParent.getContentEntryUid());
        child5join.setCepcjChildContentEntryUid(child5.getContentEntryUid());
        child5join.setChildIndex(0);
        child5join.setCepcjUid(464646);
        pcjdao.insert(child5join);

        ContentCategorySchema schema = new ContentCategorySchema();
        schema.setSchemaName("Reading Level");
        schema.setContentCategorySchemaUid(24353);
        schema.setSchemaUrl("/usb/test/reading");
        schemaDao.insert(schema);

        ContentCategory category = new ContentCategory();
        category.setContentCategoryUid(35464);
        category.setName("Reading Level 1");
        category.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
        categoryDao.insert(category);

        ContentCategory category2 = new ContentCategory();
        category2.setContentCategoryUid(354242);
        category2.setName("Reading Level 2");
        category2.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
        categoryDao.insert(category2);

        ContentCategory category3 = new ContentCategory();
        category3.setContentCategoryUid(352422);
        category3.setName("Reading Level 3");
        category3.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
        categoryDao.insert(category3);

        ContentEntryContentCategoryJoin category1join = new ContentEntryContentCategoryJoin();
        category1join.setCeccjContentCategoryUid(category.getContentCategoryUid());
        category1join.setCeccjContentEntryUid(child2.getContentEntryUid());
        category1join.setCeccjUid(35364674);
        categoryJoinDao.insert(category1join);

        ContentEntryContentCategoryJoin category2join = new ContentEntryContentCategoryJoin();
        category2join.setCeccjContentCategoryUid(category.getContentCategoryUid());
        category2join.setCeccjContentEntryUid(child3.getContentEntryUid());
        category2join.setCeccjUid(547337);
        categoryJoinDao.insert(category2join);

        ContentEntryContentCategoryJoin category3join = new ContentEntryContentCategoryJoin();
        category3join.setCeccjContentCategoryUid(category2.getContentCategoryUid());
        category3join.setCeccjContentEntryUid(child4.getContentEntryUid());
        category3join.setCeccjUid(454353);
        categoryJoinDao.insert(category3join);

        ContentEntryContentCategoryJoin category4join = new ContentEntryContentCategoryJoin();
        category4join.setCeccjContentCategoryUid(category3.getContentCategoryUid());
        category4join.setCeccjContentEntryUid(child5.getContentEntryUid());
        category4join.setCeccjUid(2457534);
        categoryJoinDao.insert(category4join);


    }

    @Test
    public void givenContentEntryPresent_whenOpened_entryIsDisplayed() {

        onView(allOf(withId(R.id.content_entry_list), isDisplayed()))
                .check(matches(hasDescendant(withText("Khan Academy"))));

    }


    @Test
    public void givenContentEntryWithChildrenPresent_whenEntryClicked_intentToViewListIsFired() {
        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(allOf(
                hasComponent(ContentEntryListActivity.class.getCanonicalName()),
                hasExtra(equalTo(ARG_CONTENT_ENTRY_UID),
                        equalTo(String.valueOf(2l))
                )));
    }

    @Test
    public void givenContentEntryLeafPresent_whenEntryClicked_intentToViewDetailIsFired() {
        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(Matchers.allOf(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(allOf(
                hasComponent(ContentEntryDetailActivity.class.getCanonicalName()),
                hasExtra(equalTo(ARG_CONTENT_ENTRY_UID),
                        equalTo(String.valueOf(6l))
                )));

    }


}
