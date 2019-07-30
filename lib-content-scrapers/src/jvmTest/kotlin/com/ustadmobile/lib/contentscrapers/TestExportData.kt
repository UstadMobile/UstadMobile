package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

class TestExportData {

    @Before
    fun before() {
        try {
            initDb()
            ContentScraperUtil.checkIfPathsToDriversExist()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    fun initDb() {
        val db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()
        val repo = db//db.getRepository("https://localhost", "")

        val contentDao = repo.contentEntryDao
        val pcjdao = repo.contentEntryParentChildJoinDao
        val contentEntryRelatedEntryJoinDao = repo.contentEntryRelatedEntryJoinDao

        LanguageList().addAllLanguages()

        val entry = ContentEntry()
        entry.contentEntryUid = ROOT_CONTENT_ENTRY_UID
        entry.title = "Ustad Mobile"
        entry.publik = true
        contentDao.insert(entry)

        val khanParent = ContentEntry()
        khanParent.contentEntryUid = 2
        khanParent.title = "Khan Academy"
        khanParent.description = "You can learn anything.\n" + "For free. For everyone. Forever."
        khanParent.thumbnailUrl = "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png"
        khanParent.leaf = false
        khanParent.publik = true
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
        edraak.publik = true
        contentDao.insert(edraak)

        val prathamParent = ContentEntry()
        prathamParent.contentEntryUid = 21
        prathamParent.title = "Pratham Books"
        prathamParent.description = "Every Child in School & Learning Well"
        prathamParent.thumbnailUrl = "https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png"
        prathamParent.leaf = false
        prathamParent.publik = true
        contentDao.insert(prathamParent)

        val phetParent = ContentEntry()
        phetParent.contentEntryUid = 22
        phetParent.title = "Phet Interactive Simulations"
        phetParent.description = "INTERACTIVE SIMULATIONS\nFOR SCIENCE AND MATH"
        phetParent.thumbnailUrl = "https://phet.colorado.edu/images/phet-social-media-logo.png"
        phetParent.leaf = false
        phetParent.publik = true
        contentDao.insert(phetParent)


        val voaParent = ContentEntry()
        voaParent.contentEntryUid = 23
        voaParent.title = "Voice of America - Learning English"
        voaParent.description = "Learn American English with English language lessons from Voice of America. " +
                "VOA Learning English helps you learn English with vocabulary, listening and " +
                "comprehension lessons through daily news and interactive English learning activities."
        voaParent.thumbnailUrl = "https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png"
        voaParent.leaf = false
        voaParent.publik = true
        contentDao.insert(voaParent)


        val folderParent = ContentEntry()
        folderParent.contentEntryUid = 24
        folderParent.title = "3asafeer"
        folderParent.leaf = false
        folderParent.publik = true
        contentDao.insert(folderParent)

        val etekParent = ContentEntry()
        etekParent.contentEntryUid = 25
        etekParent.title = "eTekkatho"
        etekParent.description = "Educational resources for the Myanmar academic community"
        etekParent.thumbnailUrl = "http://www.etekkatho.org/img/logos/etekkatho-myanmar-lang.png"
        etekParent.leaf = false
        etekParent.publik = true
        contentDao.insert(etekParent)

        val ddlParent = ContentEntry()
        ddlParent.contentEntryUid = 26
        ddlParent.title = "Darakht-e Danesh"
        ddlParent.description = "Free and open educational resources for Afghanistan"
        ddlParent.thumbnailUrl = "https://www.ddl.af/storage/files/logo-dd.png"
        ddlParent.leaf = false
        ddlParent.publik = true
        contentDao.insert(ddlParent)

        val ck12Parent = ContentEntry()
        ck12Parent.contentEntryUid = 27
        ck12Parent.title = "CK-12 Foundation"
        ck12Parent.description = "100% Free, Personalized Learning for Every Student"
        ck12Parent.thumbnailUrl = "https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png"
        ck12Parent.leaf = false
        ck12Parent.publik = true
        contentDao.insert(ck12Parent)

        val asbParent = ContentEntry()
        asbParent.contentEntryUid = 28
        asbParent.title = "African Story Books"
        asbParent.description = "Open access to picture storybooks in the languages of Africa\nFor children’s literacy, enjoyment and imagination."
        asbParent.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        asbParent.leaf = false
        asbParent.publik = true
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
        grade5parent.publik = true
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
        grade1child.publik = true
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
        wholenumbers.publik = true
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
        quiz.publik = false
        contentDao.insert(quiz)

        val NumberQuizJoin = ContentEntryParentChildJoin()
        NumberQuizJoin.cepcjParentContentEntryUid = wholenumbers.contentEntryUid
        NumberQuizJoin.cepcjChildContentEntryUid = quiz.contentEntryUid
        NumberQuizJoin.childIndex = 0
        NumberQuizJoin.cepcjUid = 7
        pcjdao.insert(NumberQuizJoin)

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
        arabicQuiz.publik = true
        contentDao.insert(arabicQuiz)

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
        spanishQuiz.publik = true
        contentDao.insert(spanishQuiz)


        val spanishEnglishJoin = ContentEntryRelatedEntryJoin()
        spanishEnglishJoin.cerejContentEntryUid = quiz.contentEntryUid
        spanishEnglishJoin.cerejRelatedEntryUid = spanishQuiz.contentEntryUid
        spanishEnglishJoin.cerejUid = 17
        spanishEnglishJoin.relType = ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION
        contentEntryRelatedEntryJoinDao.insert(spanishEnglishJoin)

    }

    @Test
    @Throws(IOException::class)
    fun givenADatabase_downloadAllThePublikEntries_SaveIntoFilesWithAnIndex() {

        val tmpDir = Files.createTempDirectory("testExportData").toFile()

        val data = ExportData()
        data.export(tmpDir, 1500)

        val indexFile = File(tmpDir, "index.json")
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(indexFile))

        val langFile = File(tmpDir, "language.0.json")
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(langFile))

        val entryFile = File(tmpDir, "contentEntry.0.json")
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(entryFile))

        val result = FileUtils.readFileToString(entryFile, ScraperConstants.UTF_ENCODING)
        val hasQuizNotPublik = result.contains("Counting with small numbers")

        Assert.assertEquals(false, hasQuizNotPublik)

    }

    companion object {

        val ROOT_CONTENT_ENTRY_UID = 1L
    }


}
