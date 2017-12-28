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

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Enumeration;
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
        
        //Check if this is actually a data: link which should not be resolved
        if(link.startsWith("data:")) {
            return link;
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

        //Check if this is not a relative link but has no actual folder structure in the base. E.g.
        // base = somefile.txt href=path/to/somewhere.text . As there is no folder structure there is
        // nothing to resolve against
        if(charFoundIndex == -1) {
            return link;
        }

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
     * Ensure that the given filename has the correct extension on it. If the filename given already
     * includes the correct file extension for the given mime type it will be returned as is. Otherewise
     * the correct extension will be added. If the appropriate extension is unknown, the filename
     * will not be changed.
     *
     * @param filename The filename as given
     * @param mimeType The mimetype of the file
     *
     * @return The filename with the correct extension for the mime type as above.
     */
    public static String appendExtensionToFilenameIfNeeded(String filename, String mimeType) {
        String expectedExtension = UstadMobileSystemImpl.getInstance().getExtensionFromMimeType(
            mimeType);

        if(expectedExtension == null)
            return filename;

        if(!filename.endsWith('.' + expectedExtension)) {
            filename += '.' + expectedExtension;
        }

        return filename;
    }

    /**
     * Parse a deliminated string with keys and values like Content-Type parameters
     * and cache-control headers.  Keys can be present on their own e.g.
     * no-cache in which case the no-cache key will be in the hashtable with a
     * blank string value.  It can also have an = sign with quoted or unquoted
     * text e.g. maxage=600 or maxage="600"
     * 
     * @param str String to parse
     * @param deliminator deliminator character 
     * @return Hashtable of parameters and values found
     */
    public static Hashtable parseParams(String str, char deliminator) {
        String paramName = null;
        Hashtable params = new Hashtable();
        boolean inQuotes = false;
        
        int strLen = str.length();
        StringBuffer sb = new StringBuffer();
        char c;
        
        char lastChar = 0;
        for(int i = 0; i < strLen; i++) {
            c = str.charAt(i);
            if(c == '"') {
                if(!inQuotes) {
                    inQuotes = true;
                }else if(inQuotes && lastChar != '\\') {
                    inQuotes = false;
                }
                
            }

            if((isWhiteSpace(c) && !inQuotes) || (c == '"' && i < strLen-1)) {
                //do nothing more
            }else if((c == deliminator || i == strLen-1)){
                //check if we are here because it's the end... then we add this to bufer
                if(i == strLen-1 && c != '"') {
                    sb.append(c);
                }
                
                if(paramName != null) {
                    //this is a parameter with a value
                    params.put(paramName, sb.toString());
                }else {
                    //this is a parameter on its own
                    params.put(sb.toString(), "");
                }
                
                sb = new StringBuffer();
                paramName = null;
            }else if(c == '='){
                paramName = sb.toString();
                sb = new StringBuffer();
            }else {
                sb.append(c);
            }
            
            lastChar = c;
        }
        
        return params;
    }
    
    /**
     * 
     * @param urlQuery
     * @return 
     */
    public static Hashtable parseURLQueryString(String urlQuery) {
        int queryPos = urlQuery.indexOf('?');
        if(queryPos != -1) {
            urlQuery = urlQuery.substring(queryPos+1);
        }
        
        Hashtable parsedParams = parseParams(urlQuery, '&');
        Hashtable decodedParams = new Hashtable();
        Enumeration e = parsedParams.keys();
        String key;
        while(e.hasMoreElements()) {
            key = (String)e.nextElement();
            decodedParams.put(URLTextUtil.urlDecodeUTF8(key), 
                URLTextUtil.urlDecodeUTF8((String)parsedParams.get(key)));
        }
        
        return decodedParams;
    }
    
    /**
     * Turns a hashtable into a URL encoded query string
     * 
     * @param ht Hashtable of param keys to values (keys and values must be strings)
     * 
     * @return String in the form of foo=bar&foo2=bar2 ... (URL Encoded)
     */
    public static String hashtableToQueryString(Hashtable ht) {
        StringBuffer sb = new StringBuffer();
        
        Enumeration keys = ht.keys();
        String key;
        boolean firstEl = true;
        while(keys.hasMoreElements()) {
            if(!firstEl) {
                sb.append('&');
            }else {
                firstEl = false;
            }
            
            key = (String)keys.nextElement();
            sb.append(URLTextUtil.urlEncodeUTF8(key)).append('=');
            sb.append(URLTextUtil.urlEncodeUTF8((String)ht.get(key)));
        }
        
        return sb.toString();
    }
    
    
    /**
     * Parse type with params header fields (Content-Disposition; Content-Type etc). E.g. given
     *  application/atom+xml;type=entry;profile=opds-catalog
     *
     *  It will return an object with the mime type "application/atom+xml" and a hashtable of parameters
     *  with type=entry and profile=opds-catalog .
     * 
     * TODO: Support params with *paramname and encoding e.g. http://tools.ietf.org/html/rfc6266 section 5 example 2
     * 
     * @return 
     */
    public static TypeWithParamHeader parseTypeWithParamHeader(String header) {
        TypeWithParamHeader result = null;
        
        int semiPos = header.indexOf(';');
        String typeStr = null;
        Hashtable params = null;
        
        if(semiPos == -1) {
            typeStr = header.trim();
        }else {
            typeStr = header.substring(0, semiPos).trim();
        }
        
        if(semiPos != -1 && semiPos < header.length()-1) {
            params = parseParams(header.substring(semiPos), ';');
        }
        
        return new TypeWithParamHeader(typeStr, params);
    }
    
    /**
     * Filter filenames for characters that could be nasty attacks (e.g. /sdcard/absolutepath etc)
     * 
     * @param filename Filename from an untrusted source (e.g. http header)
     * 
     * @return Filename with sensitive characters (: / \ * > < ? ) removed
     */
    public static String filterFilename(String filename) {
        StringBuffer newStr = new StringBuffer(filename.length());
        char c;
        
        for(int i = 0; i < filename.length(); i++) {
            c = filename.charAt(i);
            if(!(c == ':' || c == '/' || c == '\\' || c == '*' || c == '>' || c == '<' || c == '?')) {
                newStr.append(c);
            }
        }
        
        return newStr.toString();
    }
    
    /**
     * Simple wrapper class that represents a haeder field with a type
     * and parameters.
     */
    public static class TypeWithParamHeader {
        
       /**
        * The first parameter: e.g. the mime type; content disposition etc.
        */
       public String typeName;

       /**
        * Hashtable of parameters found (case sensitive)
        */
       public Hashtable params;

       public TypeWithParamHeader(String typeName, Hashtable params) {
           this.typeName = typeName;
           this.params = params;
       }
       
       public String getParam(String paramName) {
           if(params != null && params.containsKey(paramName)) {
               return (String)params.get(paramName);
           }else {
               return null;
           }
       }
    }
    
    public static final boolean isWhiteSpace(char c) {
        if(c == ' ' || c == '\n' || c == '\t' || c == '\r') {
            return true;
        }else {
            return false;
        }
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
     * Split a filename into it's basename and extension.
     *
     * @param filename e.g. file.jpg
     * @return A two component String array e.g. {"file", "jpg"}
     */
    public static String[] splitFilename(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if(dotIndex != -1)
            return new String[]{filename.substring(0, dotIndex),
                filename.substring(dotIndex+1)};
        else
            return new String[]{filename, null};
    }


    /**
     * Remove the extension from a filename. The input filename is expected to be only a filename,
     * e.g. without the path or url query strings. This can be obtained using getFilename if needed.
     *
     * @param filename Input filename without path or query string components e.g. file.txt
     *
     * @return filename without the extension, e.g. file
     */
    public static String removeExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');

        if(lastDot != -1 && lastDot != filename.length() -1) {
            return filename.substring(0, lastDot);
        }else {
            return filename;
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
     * Remove the anchor section of a link if present (e.g. for index.html#foo
     * remove #foo)
     * 
     * @param uri The complete URI e.g. some/path.html#foo
     * 
     * @return the given uri without the anchor if it was found in the uri
     */
    public static String stripAnchorIfPresent(String uri) {
        int charPos = uri.lastIndexOf('#');
        if(charPos != -1) {
            return uri.substring(0, charPos);
        }else {
            return uri;
        }
    }
    
    /**
     * Make sure that the given path has the given suffix; if it doesn't
     * add the suffix.
     * 
     * @param suffix the suffix that the path must end with
     * @param path The path to add the suffix to if missing
     * 
     * @return The path with the suffix added if it was originally missing
     */
    public static String ensurePathHasSuffix(String suffix, String path) {
        if(!path.endsWith(suffix)) {
            return path + suffix;
        }else {
            return path;
        }
    }
    
    /**
     * Strip out mime type parameters if they are present 
     * 
     * @param mimeType Mime type e.g. application/atom+xml;profile=opds
     * @return Mime type without any params e.g. application/atom+xml
     */
    public static String stripMimeParams(String mimeType) {
        int i = mimeType.indexOf(';');
        return i != -1 ? mimeType.substring(0, i).trim() : mimeType;
    }


    private static final long UNIT_GB = (long)Math.pow(1024, 3);

    private static final long UNIT_MB = (long)Math.pow(1024, 2);

    private static final long UNIT_KB = 1024;

    /**
     * Return a String formatted to show the file size in a user friendly format
     *
     * If < 1024 (kb) : size 'bytes'
     * if 1024 < size < 1024^2 : size/1024 kB
     * if 1024^ < size < 1023^3 : size/1024^2 MB
     *
     * @param fileSize Size of the file in bytes
     *
     * @return Formatted string as above
     */
    public static String formatFileSize(long fileSize) {
        String unit;
        long factor;
        if(fileSize > UNIT_GB){
            factor = UNIT_GB;
            unit = "GB";
        }else if(fileSize > UNIT_MB){
            factor = UNIT_MB;
            unit = "MB";
        }else if (fileSize > UNIT_KB){
            factor = UNIT_KB;
            unit = "kB";
        }else {
            factor = 1;
            unit = "bytes";
        }

        double unitSize = (double)fileSize / (double)factor;
        unitSize = Math.round(unitSize * 100) / 100d;
        return unitSize + " " + unit;
    }

    /**
     *
     * @param args
     * @param prefix
     * @return
     */
    public static Vector splitCombinedViewArguments(Hashtable args, String prefix, char argDelmininator) {
        Vector result = new Vector();
        Enumeration allArgsKeys = args.keys();

        String currentKey, argName;
        int index, indexStart, indexEnd;
        Hashtable indexArgs;
        while(allArgsKeys.hasMoreElements()) {
            currentKey = (String)allArgsKeys.nextElement();
            if(currentKey.startsWith(prefix)) {
                indexStart = currentKey.indexOf(argDelmininator) + 1;
                indexEnd = currentKey.indexOf(argDelmininator, indexStart + 1);
                try {
                    index = Integer.parseInt(currentKey.substring(indexStart, indexEnd));
                    if(result.size() < index + 1)
                        result.setSize(index + 1);

                    argName = currentKey.substring(indexEnd + 1);
                    if(result.elementAt(index) != null) {
                        indexArgs = (Hashtable)result.elementAt(index);
                    }else {
                        indexArgs = new Hashtable();
                        result.setElementAt(indexArgs, index);
                    }

                    indexArgs.put(argName, args.get(currentKey));
                }catch(NumberFormatException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 680, currentKey, e);
                }
            }
        }

        return result;
    }

    
    
}
