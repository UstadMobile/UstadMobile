package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.contentscrapers.buildconfig.ScraperBuildConfig;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithFilePath;
import com.ustadmobile.port.sharedse.util.UmZipUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_CSS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JPG;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class ShrinkerUtil {

    public static final List<String> HTML_MIME_TYPES = Arrays.asList("application/xhtml+xml", "text/html");
    public static final List<String> IMAGE_MIME_TYPES = Arrays.asList(MIMETYPE_JPG, "image/png", "image/jpeg");

    public static void main(String[] args) {

        UMLogUtil.setLevel(args.length == 1 ? args[0] : "");

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContentEntryFileDao contentEntryFileDao = repository.getContentEntryFileDao();
        List<ContentEntryFileWithFilePath> epubFileList = contentEntryFileDao.findEpubsFiles();
        for (ContentEntryFileWithFilePath entryfile : epubFileList) {
            try {
                File epubFile = new File(entryfile.getFilePath());
                ShrinkerUtil.shrinkEpub(epubFile);
                contentEntryFileDao.updateEpubFiles(epubFile.length(), ContentScraperUtil.getMd5(epubFile), entryfile.getContentEntryFileUid());

            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Failed to shrink epub " + entryfile.getFilePath());
            }
        }
    }

    public static void shrinkEpub(File epub) throws IOException {

        File parentFolder = epub.getParentFile();
        File tmpFolder = new File(parentFolder, UMFileUtil.stripExtensionIfPresent(epub.getName()));
        tmpFolder.mkdirs();
        UmZipUtils.unzip(epub, tmpFolder);
        shrinkEpubFiles(tmpFolder);
        ContentScraperUtil.zipDirectory(tmpFolder, epub.getName(), epub.getParentFile());
        FileUtils.deleteDirectory(tmpFolder);

    }

    public static void cleanXml(File xmlFile) throws IOException {

        try (InputStream is = FileUtils.openInputStream(xmlFile)) {
            Document doc = Jsoup.parse(is, UTF_ENCODING, "", Parser.xmlParser());
            doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
            doc.outputSettings().prettyPrint(false);
            FileUtils.writeStringToFile(xmlFile, doc.toString(), UTF_ENCODING);
        }
    }


    private static boolean shrinkEpubFiles(File directory)  {
        FileInputStream opfFileInputStream = null;
        FileInputStream ocfFileInputStream = null;
        FileOutputStream opfFileOutputStream = null;
        try {
            OcfDocument ocfDoc = new OcfDocument();
            File ocfFile = new File(directory, "META-INF/container.xml");
            ocfFileInputStream = new FileInputStream(ocfFile);
            XmlPullParser ocfParser = UstadMobileSystemImpl.getInstance()
                    .newPullParser(ocfFileInputStream);
            ocfDoc.loadFromParser(ocfParser);

            File opfFile = new File(directory, ocfDoc.getRootFiles().get(0).getFullPath());
            File opfDir = opfFile.getParentFile();

            cleanXml(opfFile);
            OpfDocument document = new OpfDocument();
            opfFileInputStream = new FileInputStream(opfFile);
            XmlPullParser xmlPullParser = UstadMobileSystemImpl.getInstance()
                    .newPullParser(opfFileInputStream);
            document.loadFromOPF(xmlPullParser);

            Map<String, OpfItem> manifestList = document.getManifestItems();
            Map<File, File> replacedFiles = new HashMap<>();
            Map<String, String> styleMap = new HashMap<>();
            List<OpfItem> newOpfItems = new ArrayList<>();

            for (OpfItem itemValue : manifestList.values()) {

                if (IMAGE_MIME_TYPES.contains(itemValue.getMediaType())) {
                    String oldHrefValue = itemValue.getHref();
                    String newHref = UMFileUtil.stripExtensionIfPresent(oldHrefValue) +
                            ScraperConstants.WEBP_EXT;

                    File inputFile = new File(opfDir, oldHrefValue);
                    File outputFile = new File(opfDir, newHref);
                    replacedFiles.put(inputFile, outputFile);


                    convertImageToWebp(inputFile, outputFile);

                    if (ContentScraperUtil.fileHasContent(outputFile)) {
                        itemValue.setHref(newHref);
                        itemValue.setMimeType(ScraperConstants.MIMETYPE_WEBP);
                    }
                }
            }

            int countStyle = 0;
            for (OpfItem opfItem : manifestList.values()) {
                if (HTML_MIME_TYPES.contains(opfItem.getMediaType())) {

                    File htmlFile = new File(opfDir, opfItem.getHref());
                    try (FileInputStream htmlFileInputStream = new FileInputStream(htmlFile)) {
                        String html = UMIOUtils.readToString(htmlFileInputStream, UTF_ENCODING);
                        /*
                         * Pratham uses an entity code to map &nbsp; to &#160; - this confuses jsoup
                         */
                        html = html.replaceAll("&nbsp;", "&#160;");
                        html = html.replace("<!DOCTYPE html[<!ENTITY nbsp \"&#160;\">]>",
                                "<!DOCTYPE html>");
                        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
                        doc.outputSettings().prettyPrint(false);

                        if (replacedFiles.size() != 0) {
                            Elements elements = doc.select("[src]");
                            for (Element element : elements) {
                                List<Attribute> attrList = element.attributes().asList();

                                ArrayList<String> attrToDelete = new ArrayList<>();
                                for (Attribute attr : attrList) {

                                    if (attr.getKey().contains("src")) {

                                        String srcValue = attr.getValue();
                                        File srcFile = new File(htmlFile.getParentFile(), srcValue);

                                        if (replacedFiles.containsKey(srcFile)) {
                                            String newHref = Paths.get(htmlFile.getParentFile().toURI())
                                                    .relativize(Paths.get(replacedFiles.get(srcFile).toURI()))
                                                    .toString().replaceAll(Pattern.quote("\\"), "/");

                                            element.attr("src", newHref);
                                        } else {
                                            attrToDelete.add(attr.getKey());
                                        }
                                    }
                                }
                                for (String attr : attrToDelete) {
                                    if (!attr.equals("src")) {
                                        element.removeAttr(attr);
                                    }
                                }

                            }
                        }
                        Elements styleList = doc.select("style[type=text/css]");
                        for (Element style : styleList) {

                            String cssText = style.text();
                            if (cssText != null && !cssText.isEmpty()) {
                                String pathToStyleFile = styleMap.get(cssText);
                                if (pathToStyleFile == null) {
                                    File styleFile = new File(htmlFile.getParentFile(), "style_" + ++countStyle + ".css");
                                    FileUtils.writeStringToFile(styleFile, cssText, UTF_ENCODING);
                                    pathToStyleFile = Paths.get(htmlFile.getParentFile().toURI())
                                            .relativize(Paths.get(styleFile.toURI()))
                                            .toString().replaceAll(Pattern.quote("\\"), "/");

                                    String pathFromOpfToStyleFile = Paths.get(opfDir.toURI())
                                            .relativize(Paths.get(styleFile.toURI()))
                                            .toString().replaceAll(Pattern.quote("\\"), "/");

                                    OpfItem styleOpf = new OpfItem();
                                    styleOpf.href = pathFromOpfToStyleFile;
                                    styleOpf.mediaType = MIMETYPE_CSS;
                                    styleOpf.id = "style_" + countStyle;
                                    newOpfItems.add(styleOpf);

                                    styleMap.put(cssText, pathToStyleFile);
                                }
                                doc.head().append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + pathToStyleFile + "\"/>");
                            }

                        }
                        styleList.remove();
                        FileUtils.writeStringToFile(htmlFile, doc.html(), UTF_ENCODING);
                    }

                }
            }

            for (OpfItem item : newOpfItems) {
                manifestList.put(item.id, item);
            }

            if (newOpfItems.size() == 0 && replacedFiles.size() == 0 && styleMap.size() == 0) {
                return false;
            }

            XmlSerializer xmlSerializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            opfFileOutputStream = new FileOutputStream(opfFile);
            xmlSerializer.setOutput(opfFileOutputStream, "UTF-8");
            document.serialize(xmlSerializer);
            opfFileOutputStream.flush();

            for (File replacedFile : replacedFiles.keySet()) {
                if (!replacedFile.delete()) {
                    throw new IllegalStateException("Could not delete: " + replacedFile);
                }
            }
            return true;
        } catch (XmlPullParserException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Failed to xmlpullparse for directory " + directory.getPath());
        } catch (IOException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("IO Exception for directory " + directory.getPath());
        } finally {
            UMIOUtils.closeQuietly(opfFileInputStream);
            UMIOUtils.closeQuietly(opfFileOutputStream);
            UMIOUtils.closeQuietly(ocfFileInputStream);
        }

        return false;
    }

    /**
     * Given a source file and a destination file, convert the image(src) to webp(dest)
     *
     * @param src  file image path
     * @param dest webp file path
     */
    public static void convertImageToWebp(File src, File dest) throws IOException {
        if(!src.exists()) {
            throw new FileNotFoundException("convertImageToWebp: Source file: " + src.getAbsolutePath() + " does not exist");
        }

        Runtime runTime = Runtime.getRuntime();
        try {
            Process process = runTime.exec(ScraperBuildConfig.WEBP_PATH + " " + src.getPath() + " -o  " + dest.getPath());
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!dest.exists()) {
            throw new IOException("convertImaegToWebP: source existed, but output does not " +
                    dest.getPath());
        }

    }


}
