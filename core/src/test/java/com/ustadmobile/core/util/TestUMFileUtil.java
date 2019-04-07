package com.ustadmobile.core.util;
import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by mike on 6/18/17.
 *
 * TODO: Refactor this to use test name conventions
 */
public class TestUMFileUtil {

    @Test
    public void testAppendExtensionToFilenameIfNeeded() {
        assertEquals("Will append file extension when needed", "foo.bar.epub",
                UMFileUtil.INSTANCE.appendExtensionToFilenameIfNeeded("foo.bar",
                        EPUBTypePlugin.Companion.getMIME_TYPES()[0]));
        assertEquals("Will leave filename when extension is already there", "foo.epub",
                UMFileUtil.INSTANCE.appendExtensionToFilenameIfNeeded("foo.epub",
                        EPUBTypePlugin.Companion.getMIME_TYPES()[0]));
        assertEquals("Will leave filename when extension is unknown", "foo.bar",
                UMFileUtil.INSTANCE.appendExtensionToFilenameIfNeeded("foo.bar",
                "application/x-foo-bar"));

    }

    @Test
    public void testRemoveExtension() {
        assertEquals("Can remove extension", UMFileUtil.INSTANCE.removeExtension("filename.txt"),
                "filename");
        assertEquals("If file has no extension, same value is returned",
                UMFileUtil.INSTANCE.removeExtension("filename"), "filename");
    }


    @Test
    public void testSplitCombinedViewArguments() {
        Map<String, String> combinedArgs = new HashMap<>();
        String catalogUrl0 = "http://www.ustadmobile.com/files/s4s/index.testnewcat.opds";
        combinedArgs.put("catalog-0-url", catalogUrl0);
        combinedArgs.put("catalog-1-url", "bar");

        Vector splitArgs = UMFileUtil.INSTANCE.splitCombinedViewArguments(combinedArgs, "catalog", '-');
        Map<String, String> args0 = (Map<String, String>) splitArgs.get(0);
        assertEquals("Catalog 0 arg is as expected", args0.get("url"), catalogUrl0);
    }

    @Test
    public void testFileUtilFilename() {
        assertEquals("Will return the same for a name only entry",
                UMFileUtil.INSTANCE.getFilename("testfile.txt"), "testfile.txt");
        assertEquals("Will return the the basename of a directory with trailing /",
                "somedir/", UMFileUtil.INSTANCE.getFilename("/somepath/somedir/"));
        assertEquals("Can handle . as filename",
                ".", UMFileUtil.INSTANCE.getFilename("."));
        assertEquals("Can handle ./ as filename",
                "./", UMFileUtil.INSTANCE.getFilename("./"));

        assertEquals("Will return the same for a name only entry with trailing /",
                UMFileUtil.INSTANCE.getFilename("somedir/"), "somedir/");
        assertEquals("Will cut the path off and return filnemae",
                UMFileUtil.INSTANCE.getFilename("/somedir/file.txt"), "file.txt");
        assertEquals("Will cut off query string",
                UMFileUtil.INSTANCE.getFilename("http://someplace.com/somedir/file.txt"),
                "file.txt");


        assertEquals("Will correctly find extension: mp3",
                "mp3", UMFileUtil.INSTANCE.getExtension("http://server.com/dir/file.mp3"));
        assertEquals("Will return null in case of no extension",
                null, UMFileUtil.INSTANCE.getExtension("http://server.com/some/dir"));

        assertEquals("Can strip file:// prefix as expected",
                "/path/to/file.mp3",
                UMFileUtil.INSTANCE.stripPrefixIfPresent("file://", "file:///path/to/file.mp3"));

        assertEquals("Can get the parent of a file name", "file:///some/path/",
                UMFileUtil.INSTANCE.getParentFilename("file:///some/path/file.mp3"));
        assertTrue("Parent filename return nulls when there is no parent in path",
                UMFileUtil.INSTANCE.getParentFilename("file.mp3") == null);
        assertTrue("Parent filename return nulls when path is one char long",
                UMFileUtil.INSTANCE.getParentFilename(".") == null);


        //test mime type parsing (will replace getMimeTypeParameters
        UMFileUtil.TypeWithParamHeader header = UMFileUtil.INSTANCE.parseTypeWithParamHeader(
                "application/atom+xml;profile=opds-catalog;kind=navigation");
        assertEquals("Correct type from header1", "application/atom+xml",
                header.getTypeName());
        assertEquals("Correct profile parameter in header1", "opds-catalog",
                header.getParams().get("profile"));
        assertEquals("Correct kind parameter in header1", "navigation",
                header.getParams().get("kind"));

        header = UMFileUtil.INSTANCE.parseTypeWithParamHeader(
                "attachment; filename=\"some book.epub\"");
        assertEquals("parse content-disposition type", "attachment",
                header.getTypeName());
        assertEquals("parse content disposition filename", "some book.epub",
                header.getParams().get("filename"));

        header = UMFileUtil.INSTANCE.parseTypeWithParamHeader("application/atom+xml");
        assertEquals("Can parse header with no params", "application/atom+xml",
                header.getTypeName());
        assertEquals("Header with no params results in null param ht", null,
                header.getParams());

        String cacheHeader = "private, community=UCI, maxage=600";
        Map<String, String> cacheTable = UMFileUtil.INSTANCE.parseParams(cacheHeader, ',');

        assertEquals("Cache control parsed private", "",
                cacheTable.get("private"));
        assertEquals("Cache control get community", "UCI",
                cacheTable.get("community"));
        assertEquals("Cache control get maxage", "600",
                cacheTable.get("maxage"));

        //test filtering nasty characters
        assertEquals("removes security hazard characters from filename",
                "nastyname.so", UMFileUtil.INSTANCE.filterFilename("/nastyname.*so"));

        int fileSize = 500;
        assertEquals("Format filename in bytes", "500.0 bytes",
                UMFileUtil.INSTANCE.formatFileSize(500));
        fileSize *= 1024;
        assertEquals("Format filename in kB", "500.0 kB",
                UMFileUtil.INSTANCE.formatFileSize(fileSize));
        fileSize *= 1024;
        assertEquals("Format filename in kB", "500.0 MB",
                UMFileUtil.INSTANCE.formatFileSize(fileSize));
    }

    @Test
    public void testUMFileUtilJoin() {
        assertEquals("Can handle basic join with to single slash",
                "testpath/somefile.txt",
                UMFileUtil.INSTANCE.joinPaths(new String[]{"testpath/", "/somefile.txt"}));
        assertEquals("Will not remove first slash", "/testpath/somefile.txt",
                UMFileUtil.INSTANCE.joinPaths(new String[]{"/testpath/", "/somefile.txt"}));
        assertEquals("Will not remove trailing slash", "/testpath/somedir/",
                UMFileUtil.INSTANCE.joinPaths(new String[]{"/testpath/", "/somedir/"}));
    }

    @Test
    public void testUMFileUtilResolveLink() {
        assertEquals("Absolute path returns same path back",
                "http://www.server2.com/somewhere",
                UMFileUtil.INSTANCE.resolveLink("http://server1.com/some/place",
                        "http://www.server2.com/somewhere"));
        assertEquals("Can resolve prtocol only link",
                "http://www.server2.com/somewhere",
                UMFileUtil.INSTANCE.resolveLink("http://server1.com/some/place",
                        "//www.server2.com/somewhere"));
        assertEquals("Can resolve relative to server link",
                "http://server1.com/somewhere",
                UMFileUtil.INSTANCE.resolveLink("http://server1.com/some/place",
                        "/somewhere"));
        assertEquals("Can handle basic relative path",
                "http://server1.com/some/file.jpg",
                UMFileUtil.INSTANCE.resolveLink("http://server1.com/some/other.html",
                        "file.jpg"));
        assertEquals("Can handle .. in relative path",
                "http://server1.com/file.jpg",
                UMFileUtil.INSTANCE.resolveLink("http://server1.com/some/other.html",
                        "../file.jpg"));

        assertEquals("Can handle base link with no folder", "images/thumb.png",
                UMFileUtil.INSTANCE.resolveLink("content.opf", "images/thumb.png"));

    }

    @Test
    public void givenLastReferrerPath_whenGetLastReferrerCalled_thenArgsShouldMatch() {
        String referralPath = "/DummyView?/ContentEntryList?entryid=40/ContentEntryList?entryid=41/" +
                "ContentEntryDetail?entryid=42/ContentEntryDetail?entryid=43";
        String lastListArgs = UMFileUtil.INSTANCE.getLastReferrerArgsByViewname("ContentEntryList",
                referralPath);
        Assert.assertEquals("Last entry list id = 41", "entryid=41", lastListArgs);

    }

}
