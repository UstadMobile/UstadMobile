package com.ustadmobile.test.port.android.view

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.runner.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ContentEntryDetailActivity
import com.ustadmobile.port.android.view.ContentEntryListActivity
import com.ustadmobile.sharedse.network.NetworkManagerBleAndroidService
import org.hamcrest.Matchers
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentEntryListEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(ContentEntryListActivity::class.java, false, false)

    @get:Rule
    val mServiceRule = ServiceTestRule()

    private var context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun before() {
        initDb()
        mServiceRule.startService(Intent(context, NetworkManagerBleAndroidService::class.java))
        mServiceRule.bindService(
                Intent(context, NetworkManagerBleAndroidService::class.java))

        launchActivity()
    }

    fun launchActivity() {
        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ContentEntryListFragmentPresenter.ARG_CONTENT_ENTRY_UID,
                ROOT_CONTENT_ENTRY_UID.toString())
        mActivityRule.launchActivity(launchActivityIntent)
    }

    fun initDb() {
        val context = context
        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()
        val repo = db// db.getUmRepository("https://localhost", "")

        val contentDao = repo.contentEntryDao
        val pcjdao = repo.contentEntryParentChildJoinDao
        val contentEntryRelatedEntryJoinDao = repo.contentEntryRelatedEntryJoinDao
        val containerDao = repo.containerDao
        val langDao = repo.languageDao
        val schemaDao = repo.contentCategorySchemaDao
        val categoryDao = repo.contentCategoryDao
        val categoryJoinDao = repo.contentEntryContentCategoryJoinDao

        val entry = ContentEntry()
        entry.contentEntryUid = ROOT_CONTENT_ENTRY_UID
        entry.title = "Ustad Mobile"
        contentDao.insert(entry)

        val khanParent = ContentEntry()
        khanParent.contentEntryUid = 2
        khanParent.title = "Khan Academy"
        khanParent.description = "You can learn anything.\n" + "For free. For everyone. Forever."
        khanParent.thumbnailUrl = "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png"
        khanParent.leaf = false
        contentDao.insert(khanParent)

        val edraak = ContentEntry()
        edraak.contentEntryUid = 20
        edraak.title = "Edraak K12"
        edraak.description = "تعليم مجاني\nّ" +
                "إلكترونيّ باللغة العربيّة!" +
                "\nFree Online\n" +
                "Education, In Arabic!"
        edraak.thumbnailUrl = "https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png"
        edraak.leaf = false
        contentDao.insert(edraak)

        val prathamParent = ContentEntry()
        prathamParent.contentEntryUid = 21
        prathamParent.title = "Pratham Books"
        prathamParent.description = "Every Child in School & Learning Well"
        prathamParent.thumbnailUrl = "https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png"
        prathamParent.leaf = false
        contentDao.insert(prathamParent)

        val phetParent = ContentEntry()
        phetParent.contentEntryUid = 22
        phetParent.title = "Phet Interactive Simulations"
        phetParent.description = "INTERACTIVE SIMULATIONS\nFOR SCIENCE AND MATH"
        phetParent.thumbnailUrl = "https://phet.colorado.edu/images/phet-social-media-logo.png"
        phetParent.leaf = false
        contentDao.insert(phetParent)


        val voaParent = ContentEntry()
        voaParent.contentEntryUid = 23
        voaParent.title = "Voice of America - Learning English"
        voaParent.description = "Learn American English with English language lessons from Voice of America. " +
                "VOA Learning English helps you learn English with vocabulary, listening and " +
                "comprehension lessons through daily news and interactive English learning activities."
        voaParent.thumbnailUrl = "https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png"
        voaParent.leaf = false
        contentDao.insert(voaParent)


        val folderParent = ContentEntry()
        folderParent.contentEntryUid = 24
        folderParent.title = "3asafeer"
        folderParent.leaf = false
        contentDao.insert(folderParent)

        val etekParent = ContentEntry()
        etekParent.contentEntryUid = 25
        etekParent.title = "eTekkatho"
        etekParent.description = "Educational resources for the Myanmar academic community"
        etekParent.thumbnailUrl = "http://www.etekkatho.org/img/logos/etekkatho-myanmar-lang.png"
        etekParent.leaf = false
        contentDao.insert(etekParent)

        val ddlParent = ContentEntry()
        ddlParent.contentEntryUid = 26
        ddlParent.title = "Darakht-e Danesh"
        ddlParent.description = "Free and open educational resources for Afghanistan"
        ddlParent.thumbnailUrl = "https://www.ddl.af/storage/files/logo-dd.png"
        ddlParent.leaf = false
        contentDao.insert(ddlParent)

        val ck12Parent = ContentEntry()
        ck12Parent.contentEntryUid = 27
        ck12Parent.title = "CK-12 Foundation"
        ck12Parent.description = "100% Free, Personalized Learning for Every Student"
        ck12Parent.thumbnailUrl = "https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png"
        ck12Parent.leaf = false
        contentDao.insert(ck12Parent)

        val asbParent = ContentEntry()
        asbParent.contentEntryUid = 28
        asbParent.title = "African Story Books"
        asbParent.description = "Open access to picture storybooks in the languages of Africa\nFor children’s literacy, enjoyment and imagination."
        asbParent.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        asbParent.leaf = false
        contentDao.insert(asbParent)

        val parentKhanJoin = ContentEntryParentChildJoin()
        parentKhanJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentKhanJoin.cepcjChildContentEntryUid = khanParent.contentEntryUid
        parentKhanJoin.childIndex = 0
        parentKhanJoin.cepcjUid = 1
        pcjdao.insert(parentKhanJoin)

        val parentEdraakJoin = ContentEntryParentChildJoin()
        parentEdraakJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentEdraakJoin.cepcjChildContentEntryUid = edraak.contentEntryUid
        parentEdraakJoin.childIndex = 1
        parentEdraakJoin.cepcjUid = 29
        pcjdao.insert(parentEdraakJoin)

        val parentPhetJoin = ContentEntryParentChildJoin()
        parentPhetJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentPhetJoin.cepcjChildContentEntryUid = phetParent.contentEntryUid
        parentPhetJoin.childIndex = 2
        parentPhetJoin.cepcjUid = 30
        pcjdao.insert(parentPhetJoin)


        val parentAsbJoin = ContentEntryParentChildJoin()
        parentAsbJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentAsbJoin.cepcjChildContentEntryUid = asbParent.contentEntryUid
        parentAsbJoin.childIndex = 3
        parentAsbJoin.cepcjUid = 31
        pcjdao.insert(parentAsbJoin)


        val parentCk12Join = ContentEntryParentChildJoin()
        parentCk12Join.cepcjParentContentEntryUid = entry.contentEntryUid
        parentCk12Join.cepcjChildContentEntryUid = ck12Parent.contentEntryUid
        parentCk12Join.childIndex = 4
        parentCk12Join.cepcjUid = 32
        pcjdao.insert(parentCk12Join)


        val parentDdlJoin = ContentEntryParentChildJoin()
        parentDdlJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentDdlJoin.cepcjChildContentEntryUid = ddlParent.contentEntryUid
        parentDdlJoin.childIndex = 5
        parentDdlJoin.cepcjUid = 33
        pcjdao.insert(parentDdlJoin)


        val parentEtekJoin = ContentEntryParentChildJoin()
        parentEtekJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentEtekJoin.cepcjChildContentEntryUid = etekParent.contentEntryUid
        parentEtekJoin.childIndex = 6
        parentEtekJoin.cepcjUid = 34
        pcjdao.insert(parentEtekJoin)

        val parentFolderJoin = ContentEntryParentChildJoin()
        parentFolderJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentFolderJoin.cepcjChildContentEntryUid = folderParent.contentEntryUid
        parentFolderJoin.childIndex = 7
        parentFolderJoin.cepcjUid = 35
        pcjdao.insert(parentFolderJoin)

        val parentPrathamJoin = ContentEntryParentChildJoin()
        parentPrathamJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentPrathamJoin.cepcjChildContentEntryUid = prathamParent.contentEntryUid
        parentPrathamJoin.childIndex = 8
        parentPrathamJoin.cepcjUid = 36
        pcjdao.insert(parentPrathamJoin)

        val parentVoaJoin = ContentEntryParentChildJoin()
        parentVoaJoin.cepcjParentContentEntryUid = entry.contentEntryUid
        parentVoaJoin.cepcjChildContentEntryUid = voaParent.contentEntryUid
        parentVoaJoin.childIndex = 9
        parentVoaJoin.cepcjUid = 37
        pcjdao.insert(parentVoaJoin)

        val grade5parent = ContentEntry()
        grade5parent.contentEntryUid = 3
        grade5parent.title = "Math"
        grade5parent.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png"
        grade5parent.leaf = false
        contentDao.insert(grade5parent)

        val subjectgradejoin = ContentEntryParentChildJoin()
        subjectgradejoin.cepcjParentContentEntryUid = khanParent.contentEntryUid
        subjectgradejoin.cepcjChildContentEntryUid = grade5parent.contentEntryUid
        subjectgradejoin.childIndex = 0
        subjectgradejoin.cepcjUid = 2
        pcjdao.insert(subjectgradejoin)

        val grade1child = ContentEntry()
        grade1child.contentEntryUid = 4
        grade1child.title = "Early Math"
        grade1child.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/early_math.png-314b80-128c.png"
        grade1child.leaf = false
        contentDao.insert(grade1child)

        val gradeChildJoin = ContentEntryParentChildJoin()
        gradeChildJoin.cepcjParentContentEntryUid = grade5parent.contentEntryUid
        gradeChildJoin.cepcjChildContentEntryUid = grade1child.contentEntryUid
        gradeChildJoin.childIndex = 0
        gradeChildJoin.cepcjUid = 3
        pcjdao.insert(gradeChildJoin)

        val wholenumbers = ContentEntry()
        wholenumbers.contentEntryUid = 5
        wholenumbers.title = "Counting"
        wholenumbers.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/counting.png-377815-128c.png"
        wholenumbers.leaf = false
        contentDao.insert(wholenumbers)

        val GradeNumberJoin = ContentEntryParentChildJoin()
        GradeNumberJoin.cepcjParentContentEntryUid = grade1child.contentEntryUid
        GradeNumberJoin.cepcjChildContentEntryUid = wholenumbers.contentEntryUid
        GradeNumberJoin.childIndex = 0
        GradeNumberJoin.cepcjUid = 4
        pcjdao.insert(GradeNumberJoin)

        val quiz = ContentEntry()
        quiz.contentEntryUid = 6
        quiz.title = "Counting with small numbers"
        quiz.description = "Sal counts squirrels and horses."
        quiz.thumbnailUrl = "https://cdn.kastatic.org/googleusercontent/NIWZLag0UtSxht8SlBeunPR6SxVmfNhkDCHoEobwSAqb3QAFMYTYvuna3yUiSYMoS_k4N10H6orz6hYJu7JzNeJ0Aw"
        quiz.leaf = true
        contentDao.insert(quiz)

        val NumberQuizJoin = ContentEntryParentChildJoin()
        NumberQuizJoin.cepcjParentContentEntryUid = wholenumbers.contentEntryUid
        NumberQuizJoin.cepcjChildContentEntryUid = quiz.contentEntryUid
        NumberQuizJoin.childIndex = 0
        NumberQuizJoin.cepcjUid = 7
        pcjdao.insert(NumberQuizJoin)

        val contentEntryFile = Container()
        contentEntryFile.mimeType = "application/zip"
        contentEntryFile.fileSize = 10000
        contentEntryFile.lastModified = 1540728217
        contentEntryFile.containerContentEntryUid = 8
        containerDao.insert(contentEntryFile)

        val englishEnglishJoin = ContentEntryRelatedEntryJoin()
        englishEnglishJoin.cerejContentEntryUid = quiz.contentEntryUid
        englishEnglishJoin.cerejRelatedEntryUid = quiz.contentEntryUid
        englishEnglishJoin.cerejUid = 18
        englishEnglishJoin.relType = ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION
        contentEntryRelatedEntryJoinDao.insert(englishEnglishJoin)

        // arabic
        val arabicQuiz = ContentEntry()
        arabicQuiz.contentEntryUid = 10
        arabicQuiz.title = "وقت الاختبار"
        arabicQuiz.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        arabicQuiz.description = "كل المحتوى"
        arabicQuiz.publisher = "CK12"
        arabicQuiz.author = "حفلة"
        contentDao.insert(arabicQuiz)

        val updatedFile = Container()
        updatedFile.mimeType = "application/zip"
        updatedFile.fileSize = 10
        updatedFile.lastModified = 1540728218
        updatedFile.containerContentEntryUid = 11
        containerDao.insert(updatedFile)

        val arabicEnglishJoin = ContentEntryRelatedEntryJoin()
        arabicEnglishJoin.cerejContentEntryUid = quiz.contentEntryUid
        arabicEnglishJoin.cerejRelatedEntryUid = arabicQuiz.contentEntryUid
        arabicEnglishJoin.cerejUid = 13
        arabicEnglishJoin.relType = ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION
        contentEntryRelatedEntryJoinDao.insert(arabicEnglishJoin)


        val spanishQuiz = ContentEntry()
        spanishQuiz.contentEntryUid = 14
        spanishQuiz.title = "tiempo de prueba"
        spanishQuiz.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        spanishQuiz.description = "todo el contenido"
        spanishQuiz.publisher = "CK12"
        spanishQuiz.author = "borrachera"
        contentDao.insert(spanishQuiz)

        val spanishFile = Container()
        spanishFile.mimeType = "application/zip"
        spanishFile.fileSize = 10000
        spanishFile.lastModified = 1540728218
        spanishFile.containerContentEntryUid = 15
        containerDao.insert(spanishFile)

        val spanishEnglishJoin = ContentEntryRelatedEntryJoin()
        spanishEnglishJoin.cerejContentEntryUid = quiz.contentEntryUid
        spanishEnglishJoin.cerejRelatedEntryUid = spanishQuiz.contentEntryUid
        spanishEnglishJoin.cerejUid = 17
        spanishEnglishJoin.relType = ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION
        contentEntryRelatedEntryJoinDao.insert(spanishEnglishJoin)

        val arabLang = Language()
        arabLang.name = "Arabic"
        arabLang.langUid = 14254
        langDao.insert(arabLang)

        val child2 = ContentEntry()
        child2.contentEntryUid = 22542
        child2.title = "Math Arabic"
        child2.primaryLanguageUid = arabLang.langUid
        child2.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png"
        child2.leaf = false
        contentDao.insert(child2)

        val spanishLang = Language()
        spanishLang.name = "Spanish"
        spanishLang.langUid = 14253
        langDao.insert(spanishLang)

        val child2join = ContentEntryParentChildJoin()
        child2join.cepcjParentContentEntryUid = khanParent.contentEntryUid
        child2join.cepcjChildContentEntryUid = child2.contentEntryUid
        child2join.childIndex = 0
        child2join.cepcjUid = 754
        pcjdao.insert(child2join)

        val child3 = ContentEntry()
        child3.contentEntryUid = 324424
        child3.title = "Math Spanish"
        child3.primaryLanguageUid = spanishLang.langUid
        child3.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png"
        child3.leaf = false
        contentDao.insert(child3)

        val child3join = ContentEntryParentChildJoin()
        child3join.cepcjParentContentEntryUid = khanParent.contentEntryUid
        child3join.cepcjChildContentEntryUid = child3.contentEntryUid
        child3join.childIndex = 0
        child3join.cepcjUid = 3537
        pcjdao.insert(child3join)

        val child4 = ContentEntry()
        child4.contentEntryUid = 3535
        child4.title = "Math"
        child4.primaryLanguageUid = arabLang.langUid
        child4.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png"
        child4.leaf = false
        contentDao.insert(child4)

        val child4join = ContentEntryParentChildJoin()
        child4join.cepcjParentContentEntryUid = khanParent.contentEntryUid
        child4join.cepcjChildContentEntryUid = child4.contentEntryUid
        child4join.childIndex = 0
        child4join.cepcjUid = 3532
        pcjdao.insert(child4join)

        val child5 = ContentEntry()
        child5.contentEntryUid = 5353
        child5.title = "Math"
        child5.primaryLanguageUid = arabLang.langUid
        child5.thumbnailUrl = "https://cdn.kastatic.org/genfiles/topic-icons/icons/arithmetic.png-af7472-128c.png"
        child5.leaf = false
        contentDao.insert(child5)

        val child5join = ContentEntryParentChildJoin()
        child5join.cepcjParentContentEntryUid = khanParent.contentEntryUid
        child5join.cepcjChildContentEntryUid = child5.contentEntryUid
        child5join.childIndex = 0
        child5join.cepcjUid = 464646
        pcjdao.insert(child5join)

        val schema = ContentCategorySchema()
        schema.schemaName = "Reading Level"
        schema.contentCategorySchemaUid = 24353
        schema.schemaUrl = "/usb/test/reading"
        schemaDao.insert(schema)

        val category = ContentCategory()
        category.contentCategoryUid = 35464
        category.name = "Reading Level 1"
        category.ctnCatContentCategorySchemaUid = schema.contentCategorySchemaUid
        categoryDao.insert(category)

        val category2 = ContentCategory()
        category2.contentCategoryUid = 354242
        category2.name = "Reading Level 2"
        category2.ctnCatContentCategorySchemaUid = schema.contentCategorySchemaUid
        categoryDao.insert(category2)

        val category3 = ContentCategory()
        category3.contentCategoryUid = 352422
        category3.name = "Reading Level 3"
        category3.ctnCatContentCategorySchemaUid = schema.contentCategorySchemaUid
        categoryDao.insert(category3)

        val category1join = ContentEntryContentCategoryJoin()
        category1join.ceccjContentCategoryUid = category.contentCategoryUid
        category1join.ceccjContentEntryUid = child2.contentEntryUid
        category1join.ceccjUid = 35364674
        categoryJoinDao.insert(category1join)

        val category2join = ContentEntryContentCategoryJoin()
        category2join.ceccjContentCategoryUid = category.contentCategoryUid
        category2join.ceccjContentEntryUid = child3.contentEntryUid
        category2join.ceccjUid = 547337
        categoryJoinDao.insert(category2join)

        val category3join = ContentEntryContentCategoryJoin()
        category3join.ceccjContentCategoryUid = category2.contentCategoryUid
        category3join.ceccjContentEntryUid = child4.contentEntryUid
        category3join.ceccjUid = 454353
        categoryJoinDao.insert(category3join)

        val category4join = ContentEntryContentCategoryJoin()
        category4join.ceccjContentCategoryUid = category3.contentCategoryUid
        category4join.ceccjContentEntryUid = child5.contentEntryUid
        category4join.ceccjUid = 2457534
        categoryJoinDao.insert(category4join)


    }

    @Test
    fun givenContentEntryPresent_whenOpened_entryIsDisplayed() {

        onView(allOf<View>(withId(R.id.content_entry_list), isDisplayed()))
                .check(matches(hasDescendant(withText("Khan Academy"))))

    }


    @Test
    fun givenContentEntryWithChildrenPresent_whenEntryClicked_intentToViewListIsFired() {
        onView(Matchers.allOf<View>(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        intended(allOf(
                hasComponent(ContentEntryListActivity::class.java.canonicalName),
                hasExtra(equalTo(ARG_CONTENT_ENTRY_UID),
                        equalTo(2.toString())
                )))
    }

    @Test
    fun givenContentEntryLeafPresent_whenEntryClicked_intentToViewDetailIsFired() {

        onView(Matchers.allOf<View>(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(Matchers.allOf<View>(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(Matchers.allOf<View>(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(Matchers.allOf<View>(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(Matchers.allOf<View>(isDisplayed(), withId(R.id.content_entry_list)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        intended(allOf(
                hasComponent(ContentEntryDetailActivity::class.java.canonicalName),
                hasExtra(equalTo(ARG_CONTENT_ENTRY_UID),
                        equalTo(6.toString())
                )))

    }

    companion object {

        const val ROOT_CONTENT_ENTRY_UID = 1L
    }


}
