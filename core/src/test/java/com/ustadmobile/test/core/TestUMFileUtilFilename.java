/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.test.core;


/* $if umplatform == 2  $
    import com.ustadmobile.test.port.j2me.TestCase;
 $else$ */

import com.ustadmobile.core.util.UMFileUtil;

import junit.framework.TestCase;

import java.util.Hashtable;

/* $endif$ */


/**
 *
 * @author mike
 */
public class TestUMFileUtilFilename extends TestCase{
    
    public void testFileUtilFilename() {
        assertEquals("Will return the same for a name only entry", 
            UMFileUtil.getFilename("testfile.txt"), "testfile.txt");
        assertEquals("Will return the the basename of a directory with trailing /",
            "somedir/", UMFileUtil.getFilename("/somepath/somedir/"));
        assertEquals("Can handle . as filename",
            ".", UMFileUtil.getFilename("."));
        assertEquals("Can handle ./ as filename",
            "./", UMFileUtil.getFilename("./"));
        
        assertEquals("Will return the same for a name only entry with trailing /", 
            UMFileUtil.getFilename("somedir/"), "somedir/");
        assertEquals("Will cut the path off and return filnemae", 
            UMFileUtil.getFilename("/somedir/file.txt"), "file.txt");
        assertEquals("Will cut off query string", 
            UMFileUtil.getFilename("http://someplace.com/somedir/file.txt"), 
            "file.txt");
        
        
        assertEquals("Will correctly find extension: mp3",
            "mp3", UMFileUtil.getExtension("http://server.com/dir/file.mp3"));
        assertEquals("Will return null in case of no extension",
            null, UMFileUtil.getExtension("http://server.com/some/dir"));
        
        assertEquals("Can strip file:// prefix as expected",
            "/path/to/file.mp3", 
            UMFileUtil.stripPrefixIfPresent("file://", "file:///path/to/file.mp3"));
        
        assertEquals("Can get the parent of a file name", "file:///some/path/",
            UMFileUtil.getParentFilename("file:///some/path/file.mp3"));
        assertTrue("Parent filename return nulls when there is no parent in path",
            UMFileUtil.getParentFilename("file.mp3") == null);
        assertTrue("Parent filename return nulls when path is one char long",
            UMFileUtil.getParentFilename(".") == null);
        
        
        //test mime type parsing (will replace getMimeTypeParameters
        UMFileUtil.TypeWithParamHeader header = UMFileUtil.parseTypeWithParamHeader(
            "application/atom+xml;profile=opds-catalog;kind=navigation");
        assertEquals("Correct type from header1", "application/atom+xml", 
            header.typeName);
        assertEquals("Correct profile parameter in header1", "opds-catalog", 
            header.params.get("profile"));
        assertEquals("Correct kind parameter in header1", "navigation",
            header.params.get("kind"));
        
        header = UMFileUtil.parseTypeWithParamHeader(
            "attachment; filename=\"some book.epub\"");
        assertEquals("parse content-disposition type", "attachment",
            header.typeName);
        assertEquals("parse content disposition filename", "some book.epub",
            header.params.get("filename"));
        
        header = UMFileUtil.parseTypeWithParamHeader("application/atom+xml");
        assertEquals("Can parse header with no params", "application/atom+xml",
            header.typeName);
        assertEquals("Header with no params results in null param ht", null,
            header.params);
        
        String cacheHeader = "private, community=UCI, maxage=600";
        Hashtable cacheTable = UMFileUtil.parseParams(cacheHeader, ',');
        
        assertEquals("Cache control parsed private", "", 
            cacheTable.get("private"));
        assertEquals("Cache control get community", "UCI", 
            cacheTable.get("community"));
        assertEquals("Cache control get maxage", "600", 
            cacheTable.get("maxage"));
            
        //test filtering nasty characters
        assertEquals("removes security hazard characters from filename", 
            "nastyname.so", UMFileUtil.filterFilename("/nastyname.*so"));
        
        int fileSize = 500;
        assertEquals("Format filename in bytes", "500.0 bytes",
            UMFileUtil.formatFileSize(500));
        fileSize *= 1024;
        assertEquals("Format filename in kB", "500.0 kB",
            UMFileUtil.formatFileSize(fileSize));
        fileSize *= 1024;
        assertEquals("Format filename in kB", "500.0 MB",
            UMFileUtil.formatFileSize(fileSize));
    }

    public void runTest(){
        this.testFileUtilFilename();
    }
    
    
}
