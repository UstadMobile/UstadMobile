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

package com.ustadmobile.port.j2me.impl.zip;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import gnu.classpath.java.util.zip.ZipEntry;
import gnu.classpath.java.util.zip.ZipInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author mike
 */
public class ZipFileHandleJ2ME implements ZipFileHandle{

    private String zipFileURI;
        
    public ZipFileHandleJ2ME(String zipFileURI) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 583, zipFileURI);
        this.zipFileURI = zipFileURI;
    }
    
    
    
    public InputStream openInputStream(String name) throws IOException {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 585, zipFileURI);
        InputStream fin = null;
        ZipInputStream zin = null;
        try {
            fin = UstadMobileSystemImplJ2ME.getInstanceJ2ME().openFileInputStream(
                zipFileURI, name);
            zin = new ZipInputStream(fin);
            ZipEntry entry;
            while((entry = zin.getNextEntry()) != null) {
                if(entry.getName().equals(name)) {
                    return new UMZipEntryInputStream(zin, new ZipEntryHandleJ2ME(entry));
                }
            }
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 305, name, e);
            UMIOUtils.closeInputStream(zin);
            UMIOUtils.closeInputStream(fin);
        }
        
        UstadMobileSystemImpl.l(UMLog.ERROR, 413, name);
        return null;
    }

    public ZipEntryHandle getEntry(String name) throws IOException {
        return null;
    }

    public Enumeration entries() throws IOException {
        return null;
    }

    public void close() throws IOException {
        
    }
    
}
