package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.sharedse.util.UmZipUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.lib.contentscrapers.ContentScraperUtil.deleteETagOrModified;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ZIP_EXT;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * Storyweaver has an api for all their epub books.
 * To download each book, i need to have a cookie session id
 * I get session by logging in the website and entering the credentials and retrieving the cookie
 * To get the total number of books,
 * hit the api with just 1 book request and in the json, the total number of books is stored in metadata.hits
 * Call the api again with the request for all books
 * create the url to get the epub, open the url connection and add the cookie session
 * <p>
 * If IOException is thrown, might be because the session expired so login again.
 * otherwise file is downloaded in its folder
 */
public class IndexPrathamContentScraper {

    private static final String PRATHAM = "Pratham";
    private static final String GMAIL = "samihmustafa@gmail.com";
    private static final String PASS = "reading123";
    String prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page=";

    String prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/";

    String signIn = "https://storyweaver.org.in/api/v1/users/sign_in";

    private Gson gson;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntry prathamParentEntry;
    private LanguageDao languageDao;
    private ContainerDao containerDao;
    private UmAppDatabase db;
    private UmAppDatabase repository;
    private File containerDir;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        try {
            new IndexPrathamContentScraper().findContent(new File(args[0]), new File(args[1]));
        } catch (IOException | URISyntaxException e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent pratham");
        }
    }

    public void findContent(File destinationDir, File containerDir) throws IOException, URISyntaxException {

        destinationDir.mkdirs();
        containerDir.mkdirs();
        this.containerDir = containerDir;
        ContentScraperUtil.setChromeDriverLocation();
        String cookie = loginPratham();

        db = UmAppDatabase.getInstance(null);
        repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        containerDao = repository.getContainerDao();
        languageDao = repository.getLanguageDao();

        new LanguageList().addAllLanguages();

        Language englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");


        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        prathamParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://storyweaver.org.in/", "Pratham Books",
                "https://storyweaver.org.in/", PRATHAM, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                "Every Child in School & Learning Well", false, EMPTY_STRING,
                "https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png", EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, prathamParentEntry, 3);

        gson = new GsonBuilder().disableHtmlEscaping().create();

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(1)), cookie, destinationDir);

    }


    private void downloadPrathamContentList(URL contentUrl, String cookie, File destinationDir) throws URISyntaxException, IOException {

        BooksResponse contentBooksList = gson.fromJson(IOUtils.toString(contentUrl.toURI(), UTF_ENCODING), BooksResponse.class);

        if (contentBooksList.data.size() == 0) {
            return;
        }

        int retry = 0;
        UMLogUtil.logTrace("Found a new list of items: " + contentBooksList.data.size());
        for (int contentCount = 0; contentCount < contentBooksList.data.size(); contentCount++) {
            HttpURLConnection connection = null;
            File resourceFolder = null;
            try {

                BooksResponse.Data data = contentBooksList.data.get(contentCount);

                URL epubUrl = generatePrathamEPubFileUrl(data.slug);

                UMLogUtil.logTrace("Start scrape for " + data.slug);

                String lang = getLangCode(data.language);
                Language langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, lang);
                resourceFolder = new File(destinationDir, String.valueOf(data.id));
                resourceFolder.mkdirs();
                ContentEntry contentEntry = ContentScraperUtil.createOrUpdateContentEntry(data.slug, data.title,
                        epubUrl.toString(), PRATHAM, LICENSE_TYPE_CC_BY, langEntity.getLangUid(), null,
                        data.description, true, EMPTY_STRING, data.coverImage.sizes.get(0).url,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao,
                        prathamParentEntry, contentEntry, contentCount);

                connection = (HttpURLConnection) epubUrl.openConnection();
                connection.setRequestProperty("Cookie", cookie);
                connection.connect();

                File content = new File(resourceFolder, data.slug + ZIP_EXT);
                boolean isUpdated = ContentScraperUtil.isFileModified(connection, resourceFolder, String.valueOf(data.id));

                if (!isUpdated) {
                    continue;
                }

                File tmpDir = new File(UMFileUtil.INSTANCE.stripExtensionIfPresent(content.getPath()));
                if (ContentScraperUtil.fileHasContent(tmpDir)) {
                    FileUtils.deleteDirectory(tmpDir);
                }

                try {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), content);

                    UMLogUtil.logTrace("downloaded the zip: " + content.getPath());

                    UmZipUtils.unzip(content, resourceFolder);

                    UMLogUtil.logTrace("UnZipped the zip ");

                    File epub = new File(resourceFolder, data.slug + EPUB_EXT);
                    ShrinkerUtil.EpubShrinkerOptions options = new ShrinkerUtil.EpubShrinkerOptions();
                    options.styleElementHelper = styleElement -> {
                        String text = styleElement.text();
                        if (text.startsWith("@font-face") || text.startsWith(".english")) {
                            return ShrinkerUtil.STYLE_OUTSOURCE_TO_LINKED_CSS;
                        } else {
                            return ShrinkerUtil.STYLE_DROP;
                        }
                    };
                    options.editor = document -> {
                        Elements elements = document.select("p");
                        List<Element> elementsToRemove = new ArrayList<>();
                        for (Element element : elements) {
                            if (element.text().isEmpty()) {
                                elementsToRemove.add(element);
                            }
                        }
                        elementsToRemove.forEach(Node::remove);
                        document.head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable-no\" />");
                        return document;
                    };
                    options.linkHelper = () -> {
                        try {
                            return IOUtils.toString(getClass().getResourceAsStream(ScraperConstants.PRATHAM_CSS_HELPER), UTF_ENCODING);
                        } catch (IOException e) {
                            return null;
                        }
                    };
                    File tmpFolder = ShrinkerUtil.shrinkEpub(epub, options);
                    UMLogUtil.logTrace("Shrunk the Epub");
                    ContentScraperUtil.insertContainer(containerDao, contentEntry,
                            true, ScraperConstants.MIMETYPE_EPUB,
                            tmpFolder.lastModified(), tmpFolder,
                            db, repository, containerDir);
                    UMLogUtil.logTrace("Completed: Created Container");
                    ContentScraperUtil.deleteFile(content);
                    ContentScraperUtil.deleteFile(epub);

                } catch (IOException io) {
                    cookie = loginPratham();
                    retry++;
                    deleteETagOrModified(resourceFolder, String.valueOf(data.id));
                    if (retry == 2) {
                        UMLogUtil.logError("Error for book " + data.title + " with id " + data.slug);
                        UMLogUtil.logInfo(ExceptionUtils.getStackTrace(io));
                        retry = 0;
                        continue;
                    }
                    contentCount--;
                    continue;
                } finally {
                    connection.disconnect();
                }
                retry = 0;


            } catch (Exception e) {
                UMLogUtil.logError("Error saving book " + contentBooksList.data.get(contentCount).slug);
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                if (resourceFolder != null) {
                    deleteETagOrModified(resourceFolder, resourceFolder.getName());
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(++contentBooksList.metadata.page)), cookie, destinationDir);

    }


    private String getLangCode(String language) {
        String[] list = language.split("-");
        return list[0];
    }

    public URL generatePrathamEPubFileUrl(String resourceId) throws MalformedURLException {
        return new URL(prefixEPub + resourceId + EPUB_EXT);
    }

    public URL generatePrathamUrl(String number) throws MalformedURLException {
        return new URL(prefixUrl + number + "&per_page=24");
    }

    public String loginPratham() {
        HttpURLConnection conn = null;
        DataOutputStream out = null;
        try {
            Map<String, String> selectedParams = new HashMap<>();
            selectedParams.put("api_v1_user[email]", GMAIL);
            selectedParams.put("api_v1_user[password]", PASS);
            selectedParams.put("api_v1_user[remember_me]", String.valueOf(false));
            StringBuffer selectedRequestParams = ContentScraperUtil.convertMapToStringBuffer(selectedParams);
            URL url = new URL(signIn);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(selectedRequestParams.toString());
            out.flush();
            out.close();
            conn.connect();
            String cookie = conn.getHeaderField("Set-Cookie");
            return cookie.substring(cookie.indexOf("_session"), cookie.indexOf(";"));
        } catch (ProtocolException e) {
            UMLogUtil.logError("Protocol Error for login to Pratham");
        } catch (IOException e) {
            UMLogUtil.logError("IO Error for login to Pratham");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            UMIOUtils.INSTANCE.closeQuietly(out);
        }

        return "";
    }


}
