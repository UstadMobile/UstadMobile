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
package com.ustadmobile.core.controller;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.URLTextUtil;

/**
 *
 * @author mike
 */
public class CatalogEntryInfo {
    public int acquisitionStatus;

    public String[] srcURLs;

    public String fileURI;

    public String mimeType;
    
    public long downloadID = -1;

    public int downloadTotalSize = -1;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(acquisitionStatus).append(':');
        if(srcURLs != null) {
            for(int i = 0; i < srcURLs.length; i++) {
                sb.append(URLTextUtil.urlEncodeUTF8(srcURLs[i]));
                if(i < srcURLs.length-1) {
                    sb.append(',');
                }
            }
        }
        sb.append(':');
        sb.append(URLTextUtil.urlEncodeUTF8(fileURI)).append(':');
        sb.append(mimeType);
        if(acquisitionStatus == CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS) {
            sb.append(':').append(String.valueOf(downloadID)).append(':').append(
                    downloadTotalSize);
        }
        return sb.toString();
    }

    public static CatalogEntryInfo fromString(String str) {
        String[] strComps = UMFileUtil.splitString(str, ':');
        CatalogEntryInfo entryInfo = new CatalogEntryInfo();
        entryInfo.acquisitionStatus = Integer.parseInt(strComps[0]);
        String[] urls = UMFileUtil.splitString(strComps[1], ',');
        for(int i = 0; i < urls.length; i++) {
            urls[i] = URLTextUtil.urlDecodeUTF8(urls[i]);
        }
        entryInfo.srcURLs = urls;
        entryInfo.fileURI = URLTextUtil.urlDecodeUTF8(strComps[2]);
        entryInfo.mimeType = strComps[3];
        entryInfo.downloadID = strComps.length >= 5 && strComps[4].length() > 0
                ? Integer.parseInt(strComps[4]) : -1L;
        entryInfo.downloadTotalSize = strComps.length >= 6 ? Integer.parseInt(strComps[5])
                : -1;
        return entryInfo;
    }



}
