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

import java.util.Hashtable;
import java.util.Vector;

/**
 * Assorted cross platform file utility methods
 * 
 * @author mike
 */
public class UMFileUtil {
    
    public static final char FILE_SEP  = '/';
    
    /**
     * Constant string - the file:/// protocol
     */
    public static final String PROTOCOL_FILE = "file:///";
    
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
    
    /**
     * Join an array of Strings 
     * e.g. 
     * joinString(new String[]{"a", "b", "c"}, '/') returns "a/b/c"
     * 
     * @param strArr An array of Strings
     * @param joinChar the character to use to join them
     * @return A single string with each element of the array joined by joinChar
     */
    public static String joinString(String[] strArr, char joinChar) {
        //TODO: Make this more efficient by calculating size first
        StringBuffer resultSB = new StringBuffer();
        
        int numElements = strArr.length;
        for(int i = 0; i < numElements; i++) {
            resultSB.append(strArr[i]);
            if(i < numElements -1) {
                resultSB.append(joinChar);
            }
        }
        
        return resultSB.toString();
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
    
    /**
     * Gets the end filename (e.g. basename) from a url or path string.  Will chop off query 
     * and preceeding directories: e.g
     * "/some/path/file.ext" returns "file.ext"
     * "http://server.com/path/thing.php?foo=bar" returns "thing.php"
     * 
     * @param url
     * @return 
     */
    public static String getFilename(String url) {
        if(url.length() == 1) {
            return url.equals("/") ? "" : url;
        }
        
        int charPos = url.lastIndexOf('/', url.length() -2);
        
        if(charPos != -1) {
            url = url.substring(charPos+1);
        }
        
        charPos = url.indexOf("?");
        if(charPos != -1) {
            url = url.substring(0, charPos);
        }
        
        return url;
    }
    
    /**
     * Returns the parent filename of a given string uri 
     * 
     * @param uri e.g. /some/file/path or http://server.com/some/file.txt
     * @return The parent e.g. /some/file or http://server.com/some/, null in case of no parent in the path
     */
    public static String getParentFilename(String uri) {
        if(uri.length() == 1) {
            return null;
        }
        
        int charPos = uri.lastIndexOf('/', uri.length() -2);
        if(charPos != -1) {
            return uri.substring(0, charPos + 1);
        }else {
            return null;
        }
    }
    
    
    /**
     * Gets the extension from a url or path string.  Will chop off the query
     * and preceeding directories, and then get the file extension.  Is returned
     * without the .
     * 
     * @param uri the path or URL that we want the extension of 
     * @return the extension - the last characters after the last . if there is a . in the name
     * null if no extension is found
     */
    public static String getExtension(String uri) {
        String filename = getFilename(uri);
        int lastDot = filename.lastIndexOf('.');
        if(lastDot != -1 && lastDot != filename.length() -1) {
            return filename.substring(lastDot+1);
        }else {
            return null;
        }
    }
    
    /**
     * Ensure a given path has a given prefix (e.g. file:///) - if it doesn't
     * then join the prefix to the string, otherwise return it as is
     * 
     * @param 
     */
    public static String ensurePathHasPrefix(String prefix, String path) {
        if(path.startsWith(prefix)) {
            return path;
        }else {
            return joinPaths(new String[]{prefix, path});
        }
    }
    
    /**
     * Remove a prefix if it is present (e.g. starting file:// in the case
     * of android)
     */
    public static String stripPrefixIfPresent(String prefix, String path) {
        if(!path.startsWith(prefix)) {
            return path;
        }else {
            return path.substring(prefix.length());
        }
    }
    
    /**
     * 
     * Get a list of mime type parameters e.g. where you have
     * application/atom+xml;profile=opds-catalog;kind=acquisition
     * 
     * return hashtable with profile=opds-catalog and kind=acquisition set
     * 
     * @param mimeType The mime type string to examine
     * 
     * @return Hashtable with mime parameters or null if there are no parameters
     */
    public static Hashtable getMimeTypeParameters(String mimeType) {
        int semiPos = mimeType.indexOf(';');
        
        if(semiPos == -1) {
            return null;
        }
        
        Hashtable results = new Hashtable();
        char c;
        int eqPos = -1;
        
        String key;
        String value;
        for(int i = semiPos + 1; i < mimeType.length(); i++) {
            c= mimeType.charAt(i);
            
            if(c == '=') {
                eqPos = i;
            }else if(c == ';' || i == mimeType.length()-1) {
                key = mimeType.substring(semiPos+1, eqPos);
                value = mimeType.substring(eqPos+1, c == ';' ? i : i + 1);
                results.put(key, value);
                semiPos = i;
            }
        }
        
        return results;
    }
}
