package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY_NC;

public class IndexKhanContentScraper {


    public static final String TABLE_OF_CONTENTS_ROW = "TableOfContentsRow";
    public static final String SUBJECT_PAGE_TOPIC_CARD = "SubjectPageTopicCard";
    public static final String SUBJECT_CHALLENGE = "SubjectChallenge";
    private URL url;
    private File destinationDirectory;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private LanguageDao languageDao;
    private Language englishLang;

    String KHAN = "Khan Academy";
    private Gson gson;

    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.setMaster(true);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = repository.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();

        gson = new GsonBuilder().disableHtmlEscaping().create();

        // new LanguageList().addAllLanguages();

        englishLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "English");


        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry khanAcademyEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.khanacademy.org/", "Khan Academy",
                "https://www.khanacademy.org/", KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null,
                "You can learn anything.\n" +
                        "For free. For everyone. Forever.", false, EMPTY_STRING,
                "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, khanAcademyEntry, 3);

        File englishFolder = new File(destinationDirectory, "en");
        englishFolder.mkdirs();

        browseTopics(khanAcademyEntry, url, englishFolder);

    }

    private String getJsonStringFromScript(URL url) throws IOException {

        Document document = Jsoup.connect(url.toString()).maxBodySize(9437184).get();

        Elements scriptList = document.getElementsByTag("script");
        for (Element script : scriptList) {

            for (DataNode node : script.dataNodes()) {

                if (node.getWholeData().contains("ReactComponent(")) {

                    String data = node.getWholeData();

                    int index = data.indexOf("ReactComponent(") + 15;
                    int end = data.indexOf("})") + 1;
                    return data.substring(index, end);
                }
            }
        }
        return EMPTY_STRING;

    }


    public void browseTopics(ContentEntry parent, URL url, File fileLocation) throws IOException {

        String jsonString = getJsonStringFromScript(url);

        TopicListResponse response = gson.fromJson(jsonString, TopicListResponse.class);

        List<TopicListResponse.ComponentData.Modules> modulesList = response.componentProps.modules;

        for (TopicListResponse.ComponentData.Modules module : modulesList) {

            if (module.domains != null && !module.domains.isEmpty()) {

                List<TopicListResponse.ComponentData.Modules.Domains> domainList = module.domains;

                int topicCount = 0;
                for (TopicListResponse.ComponentData.Modules.Domains domain : domainList) {

                    URL topicUrl = new URL(url, domain.href);
                    File topicFolder = new File(fileLocation, domain.identifier);
                    topicFolder.mkdirs();
                    ContentEntry topicEntry = ContentScraperUtil.createOrUpdateContentEntry(domain.identifier,
                            domain.translatedTitle, topicUrl.toString(), KHAN,
                            LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null, EMPTY_STRING, false,
                            EMPTY_STRING, domain.icon, EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry,
                            topicCount++);

                    browseSubjects(topicEntry, topicUrl, topicFolder);

                }

            }

        }
    }

    private void browseSubjects(ContentEntry topicEntry, URL topicUrl, File topicFolder) throws IOException {

        String subjectJson = getJsonStringFromScript(topicUrl);

        SubjectListResponse response = gson.fromJson(subjectJson, SubjectListResponse.class);

        List<SubjectListResponse.ComponentData.Curation.Tab> tabList = response.componentProps.curation.tabs;

        for (SubjectListResponse.ComponentData.Curation.Tab tab : tabList) {

            if (tab.modules != null && !tab.modules.isEmpty()) {

                List<ModuleResponse> moduleList = tab.modules;

                int subjectCount = 0;
                for (ModuleResponse module : moduleList) {

                    if (TABLE_OF_CONTENTS_ROW.equals(module.kind) || SUBJECT_PAGE_TOPIC_CARD.equals(module.kind)) {

                        URL subjectUrl = new URL(topicUrl, module.url);
                        File subjectFolder = new File(topicFolder, module.slug);
                        subjectFolder.mkdirs();

                        ContentEntry subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(module.slug, module.title, subjectUrl.toString(),
                                KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null,
                                module.description, false, EMPTY_STRING, module.icon, EMPTY_STRING
                                , EMPTY_STRING, contentEntryDao);

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subjectEntry, subjectCount++);

                        browseSubjects(subjectEntry, subjectUrl, subjectFolder);

                    } else if (SUBJECT_CHALLENGE.equals(module.kind)) {

                        // TODO

                    } else if (module.tutorials != null && !module.tutorials.isEmpty()) {

                        List<ModuleResponse.Tutorial> tutorialList = module.tutorials;

                        int tutorialCount = 0;
                        for (ModuleResponse.Tutorial tutorial : tutorialList) {

                            URL tutorialUrl = new URL(topicUrl, tutorial.url);
                            File subjectFolder = new File(topicFolder, tutorial.slug);
                            subjectFolder.mkdirs();

                            ContentEntry tutorialEntry = ContentScraperUtil.createOrUpdateContentEntry(tutorial.slug, tutorial.title,
                                    tutorialUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(),
                                    null, tutorial.description, false, EMPTY_STRING, EMPTY_STRING,
                                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry,
                                    tutorialEntry, tutorialCount++);

                            List<ModuleResponse.Tutorial.ContentItem> contentList = tutorial.contentItems;

                            browseContent(contentList, tutorialEntry, tutorialUrl, subjectFolder);


                        }


                    }


                }


            }


        }


    }

    private void browseContent(List<ModuleResponse.Tutorial.ContentItem> contentList, ContentEntry tutorialEntry, URL tutorialUrl, File subjectFolder) throws MalformedURLException {

        if (contentList != null && !contentList.isEmpty()) {

            int contentCount = 0;
            for (ModuleResponse.Tutorial.ContentItem contentItem : contentList) {


                URL url = new URL(tutorialUrl, contentItem.nodeUrl);
                File contentFolder = new File(subjectFolder, contentItem.slug);
                contentFolder.mkdirs();

                ContentEntry entry = ContentScraperUtil.createOrUpdateContentEntry(contentItem.slug, contentItem.title,
                        url.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(),
                        null, contentItem.description, true, EMPTY_STRING, contentItem.thumbnailUrl,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, tutorialEntry, entry
                        , contentCount++);

                KhanContentScraper scraper = new KhanContentScraper(contentFolder);
                switch (contentItem.kind){

                    case "Video":
                        scraper.scrapeVideoContent(contentItem.downloadUrl.mp4Low);
                        break;
                    case "Exercise":


                        break;

                }


            }
        }


    }

}
