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
package com.ustadmobile.core.util;

/**
 * Assorted cross platform file utility methods
 * 
 * @author mike
 */
public class UMFileUtil {
    
    public static final char FILE_SEP  = '/';
    
    /**
     * Join multiple paths - make sure there is just one FILE_SEP character 
     * between them.  Only handles situations where there could be a single extra
     * slash - e.g. "path1/" + "/somefile.txt" - does not look inside the 
     * path components and does not deal with double // inside a single component
     * 
     * @param paths Array of paths to join
     * @return path components joined with a single FILE_SEP character between
     */
    public static String joinPaths(String[] paths) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < paths.length; i++) {
            String pathComp = paths[i];
            
            //If not the first component in the path - remove leading slash
            if(i > 0 && pathComp.length() > 0 && pathComp.charAt(0) == FILE_SEP) {
                pathComp = pathComp.substring(1);
            }
            result.append(pathComp);
            
            //If not the final component - make sure it ends with a slash
            if(i < paths.length - 1 && pathComp.charAt(pathComp.length()-1) != FILE_SEP) {
                result.append(FILE_SEP);
            }
        }
        
        return result.toString();
    }
}
