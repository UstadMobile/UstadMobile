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
package com.ustadmobile.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * A ZipHandle represents an "open" zip file.  Zip Files have headers before each
 * entry in the zip - thus there is an overhead to read the zip file the first 
 * time.  
 * 
 * Different libraries are used on each platform - java.util.ZipFile
 * 
 * @author mike
 */
public interface ZipFileHandle {
    
    /**
     * Get an inputstream for a given entry within the zip
     * 
     * @param name The name of the entry within the zip e.g. 
     * @return InputStream for a particular entry
     * @throws IOException 
     */
    public InputStream openInputStream(String name) throws IOException;
    
    /**
     * Get an Entry object for a given file within the zip
     * 
     * @param name Name of the entry within the zip
     * 
     */
    public ZipEntryHandle getEntry(String name) throws IOException;
    
    
    /**
     * Gets an enumeration that provides ZipEntryHandle 
     * 
     * @return Enumeration of all entries within the zip
     * 
     * @throws IOException 
     */
    public Enumeration entries() throws IOException;
    
    /**
     * Close the Zip File - any open input streams for particular entries should
     * be closed first
     * @throws IOException 
     */
    public void close() throws IOException;
    
}
