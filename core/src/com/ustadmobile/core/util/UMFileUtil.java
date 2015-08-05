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

import java.util.Vector;

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
    
    /**
     * Resolve a link relative to an absolute base.  The path to resolve could
     * itself be absolute or relative.
     * 
     * e.g.
     * resolvePath("http://www.server.com/some/dir", "../img.jpg");
     *  returns http://www.server.com/some/img.jpg
     * 
     * @param base The absolute base path
     * @param link The link given relative to the base
     * @return 
     */
    public static String resolveLink(String base, String link) {
        String linkLower = link.toLowerCase();
        int charFoundIndex;
        
        charFoundIndex = linkLower.indexOf("://");
        if(charFoundIndex != -1) {
            boolean isAllChars = true;
            char cc;
            for(int i = 0; i < charFoundIndex; i++) {
                cc = linkLower.charAt(i);
                isAllChars &= ((cc > 'a' && cc < 'z') || (cc > '0' && cc < '9') || cc == '+' || cc == '.' || cc == '-');
            }
            
            //we found :// and all valid scheme name characters before; path itself is absolute
            if(isAllChars) {
                return link;
            }
        }
        
        if(link.length() > 2 && link.charAt(0) == '/' && link.charAt(1) == '/'){
            //we want the protocol only from the base
            String resolvedURL = base.substring(0, base.indexOf(':')+1) + link;
            return resolvedURL;
        }
        
        if(link.length() > 1 && link.charAt(0) == '/') {
            //we should start from the end of the server
            int serverStartPos = base.indexOf("://")+3;
            int serverFinishPos = base.indexOf('/', serverStartPos+1);
            return base.substring(0, serverFinishPos) + link;
        }
        
        //get rid of query if it's present in the base path
        charFoundIndex = base.indexOf('?');
        if(charFoundIndex != -1) {
            base = base.substring(0, charFoundIndex);
        }
        
        //remove the filename component if present in base path
        //if the base path ends with a /, remove that, because it will be joined to the path using a /
        charFoundIndex = base.lastIndexOf(FILE_SEP);
        base = base.substring(0, charFoundIndex);
        
        
        String[] baseParts = splitString(base, FILE_SEP);
        String[] linkParts = splitString(link, FILE_SEP);
        
        Vector resultVector = new Vector();
        for(int i = 0; i < baseParts.length; i++) {
            resultVector.addElement(baseParts[i]);
        }
        
        for(int i = 0; i < linkParts.length; i++) {
            if(linkParts[i].equals(".")) {
                continue;
            }
            
            if(linkParts[i].equals("..")) {
                resultVector.removeElementAt(resultVector.size()-1);
            }else {
                resultVector.addElement(linkParts[i]);
            }
        }
        
        StringBuffer resultSB = new StringBuffer();
        int numElements = resultVector.size();
        for(int i = 0; i < numElements; i++) {
            resultSB.append(resultVector.elementAt(i));
            if(i < numElements -1) {
                resultSB.append(FILE_SEP);
            }
        }
        
        return resultSB.toString();
    }
    
    /**
     * Split a string into an array of Strings at each instance of splitChar
     * 
     * This is roughly the same as using String.split : Unfortunately 
     * String.split is not available in J2ME
     * 
     * @param str Whole string e.g. some/path/file.jpg
     * @param splitChar Character to split by - e.g. /
     * @return Array of Strings split e.g. "some", "path", "file.jpg"
     */
    public static String[] splitString(String str, char splitChar) {
        int numParts = countChar(str, splitChar);
        String[] splitStr = new String[numParts + 1];
        StringBuffer buffer = new StringBuffer();
        int partCounter = 0;
        
        char currentChar;
        for(int i = 0; i < str.length(); i++) {
            currentChar = str.charAt(i);
            if(currentChar == splitChar) {
                splitStr[partCounter] = buffer.toString();
                partCounter++;
                buffer = new StringBuffer();
            }else {
                buffer.append(currentChar);
            }
        }
        
        //catch the last part
        splitStr[partCounter] = buffer.toString();
        
        return splitStr;
    }
    
    private static int countChar(String str, char c) {
        int count = 0;
        int strLen = str.length();
        for(int i = 0; i < strLen; i++) {
            if(str.charAt(i) == c) {
                count++;
            }
        }
        
        return count;
    }
    
}
