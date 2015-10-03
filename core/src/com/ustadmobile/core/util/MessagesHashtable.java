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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 *
 * @author mike
 */
public class MessagesHashtable extends Hashtable {
    
    static int nline = (int)'\n';
    
    static int cret = (int)'\r';

    static int eq = (int)'=';
    
    static int comment = (int)'#';
    
    public static MessagesHashtable load(InputStream in) throws IOException {
        int b;
        MessagesHashtable ht = new MessagesHashtable();
        
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        boolean inComment = false;
        String line;
        while((b = in.read()) != -1) {
            if(b == comment){
                inComment = true;
            }else if(b == nline ||b == cret) {
                if(inComment) {
                    inComment = false;
                }else {
                    line = new String(lineOut.toByteArray());
                    int eqPos = line.indexOf('=');
                    if(eqPos != -1) {
                        ht.put(
                            new Integer(Integer.parseInt(line.substring(0, eqPos))),
                            line.substring(eqPos+1));
                    }
                }
                
                lineOut = new ByteArrayOutputStream();
            }else {
                lineOut.write(b);
            }
            
            
        };
        
        return ht;
    }
    
    public String get(int code) {
        Object val = get(new Integer(code));
        return val != null ? (String)val : null;
    }
    
}
