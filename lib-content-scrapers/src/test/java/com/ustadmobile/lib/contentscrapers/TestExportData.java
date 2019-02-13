package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestExportData {

    public static final long ROOT_CONTENT_ENTRY_UID = 1L;

    @Before
    public void before() {
        try {
            initDb();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initDb() throws IOException {
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao pcjdao = repo.getContentEntryParentChildJoinDao();
        ContentEntryFileDao contentFileDao = repo.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao = repo.getContentEntryContentEntryFileJoinDao();
        ContentEntryRelatedEntryJoinDao contentEntryRelatedEntryJoinDao = repo.getContentEntryRelatedEntryJoinDao();

        new LanguageList().addAllLanguages();

        ContentEntry entry = new ContentEntry();
        entry.setContentEntryUid(ROOT_CONTENT_ENTRY_UID);
        entry.setTitle("Ustad Mobile");
        entry.setPublik(true);
        contentDao.insert(entry);

        ContentEntry khanParent = new ContentEntry();
        khanParent.setContentEntryUid(2);
        khanParent.setTitle("Khan Academy");
        khanParent.setDescription("You can learn anything.\n" +
                "For free. For everyone. Forever.");
        khanParent.setThumbnailUrl("https://cdn.kastatic.org/images/khan-logo-dark-background.new.png");
        khanParent.setLeaf(false);
        khanParent.setPublik(true);
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
        edraak.setPublik(true);
        contentDao.insert(edraak);

        ContentEntry prathamParent = new ContentEntry();
        prathamParent.setContentEntryUid(21);
        prathamParent.setTitle("Pratham Books");
        prathamParent.setDescription("Every Child in School & Learning Well");
        prathamParent.setThumbnailUrl("https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png");
        prathamParent.setLeaf(false);
        prathamParent.setPublik(true);
        contentDao.insert(prathamParent);

        ContentEntry phetParent = new ContentEntry();
        phetParent.setContentEntryUid(22);
        phetParent.setTitle("Phet Interactive Simulations");
        phetParent.setDescription("INTERACTIVE SIMULATIONS\nFOR SCIENCE AND MATH");
        phetParent.setThumbnailUrl("https://phet.colorado.edu/images/phet-social-media-logo.png");
        phetParent.setLeaf(false);
        phetParent.setPublik(true);
        contentDao.insert(phetParent);


        ContentEntry voaParent = new ContentEntry();
        voaParent.setContentEntryUid(23);
        voaParent.setTitle("Voice of America - Learning English");
        voaParent.setDescription("Learn American English with English language lessons from Voice of America. " +
                "VOA Learning English helps you learn English with vocabulary, listening and " +
                "comprehension lessons through daily news and interactive English learning activities.");
        voaParent.setThumbnailUrl("https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png");
        voaParent.setLeaf(false);
        voaParent.setPublik(true);
        contentDao.insert(voaParent);


        ContentEntry folderParent = new ContentEntry();
        folderParent.setContentEntryUid(24);
        folderParent.setTitle("3asafeer");
        folderParent.setLeaf(false);
        folderParent.setPublik(true);
        contentDao.insert(folderParent);

        ContentEntry etekParent = new ContentEntry();
        etekParent.setContentEntryUid(25);
        etekParent.setTitle("eTekkatho");
        etekParent.setDescription("Educational resources for the Myanmar academic community");
        etekParent.setThumbnailUrl("http://www.etekkatho.org/img/logos/etekkatho-myanmar-lang.png");
        etekParent.setLeaf(false);
        etekParent.setPublik(true);
        contentDao.insert(etekParent);

        ContentEntry ddlParent = new ContentEntry();
        ddlParent.setContentEntryUid(26);
        ddlParent.setTitle("Darakht-e Danesh");
        ddlParent.setDescription("Free and open educational resources for Afghanistan");
        ddlParent.setThumbnailUrl("https://www.ddl.af/storage/files/logo-dd.png");
        ddlParent.setLeaf(false);
        ddlParent.setPublik(true);
        contentDao.insert(ddlParent);

        ContentEntry ck12Parent = new ContentEntry();
        ck12Parent.setContentEntryUid(27);
        ck12Parent.setTitle("CK-12 Foundation");
        ck12Parent.setDescription("100% Free, Personalized Learning for Every Student");
        ck12Parent.setThumbnailUrl("https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png");
        ck12Parent.setLeaf(false);
        ck12Parent.setPublik(true);
        contentDao.insert(ck12Parent);

        ContentEntry asbParent = new ContentEntry();
        asbParent.setContentEntryUid(28);
        asbParent.setTitle("African Story Books");
        asbParent.setDescription("Open access to picture storybooks in the languages of Africa\nFor children’s literacy, enjoyment and imagination.");
        asbParent.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        asbParent.setLeaf(false);
        asbParent.setPublik(true);
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
        grade5parent.setPublik(true);
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
        grade1child.setPublik(true);
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
        wholenumbers.setPublik(true);
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
        quiz.setPublik(false);
        contentDao.insert(quiz);

        ContentEntryParentChildJoin NumberQuizJoin = new ContentEntryParentChildJoin();
        NumberQuizJoin.setCepcjParentContentEntryUid(wholenumbers.getContentEntryUid());
        NumberQuizJoin.setCepcjChildContentEntryUid(quiz.getContentEntryUid());
        NumberQuizJoin.setChildIndex(0);
        NumberQuizJoin.setCepcjUid(7);
        pcjdao.insert(NumberQuizJoin);

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setMimeType("application/zip");
        contentEntryFile.setFileSize(10000);
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
        arabicQuiz.setPublik(true);
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
        spanishQuiz.setPublik(true);
        contentDao.insert(spanishQuiz);

        ContentEntryFile spanishFile = new ContentEntryFile();
        spanishFile.setMimeType("application/zip");
        spanishFile.setFileSize(10000);
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
    public void givenADatabase_downloadAllThePublikEntries_SaveIntoFilesWithAnIndex() throws IOException {

        File tmpDir = Files.createTempDirectory("testExportData").toFile();

        ExportData data = new ExportData();
        data.export(tmpDir, 1500);

        File indexFile = new File(tmpDir, "index.json");
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(indexFile));

        File langFile = new File(tmpDir, "language.0.json");
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(langFile));

        File entryFile = new File(tmpDir, "contentEntry.0.json");
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(entryFile));

        String result = FileUtils.readFileToString(entryFile, ScraperConstants.UTF_ENCODING);
        boolean hasQuizNotPublik = result.contains("Counting with small numbers");

        Assert.assertEquals(false, hasQuizNotPublik);

    }


}
