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
package com.ustadmobile.port.j2me.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author varuna
 */
public class SerializedHashtable {

    public SerializedHashtable() {
    }
    
    /* Convert an input stream (could be from file, RMS, HTTP, etc)
     * to a hash table. 
     * eg: To update the default settings of the app.
     */
    public static Hashtable streamToHashtable(byte[] byteArray){
         Hashtable hashTable = new Hashtable();
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(byteArray);
            DataInputStream din = new DataInputStream(bin);
            int numElements = din.readInt();
            for(int i = 0; i < numElements; i++) {
                String key = din.readUTF();
                String val = din.readUTF();
                hashTable.put(key, val);
            }
            
            din.close();
            bin.close();
            return hashTable;
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /* Convert a hashtable to an byte outputstream (that can be used to make a file,
     * update RMS, http, etc)
     * eg: To Update the set app settings to RMS.
     */
    public static byte[] hashTabletoStream(Hashtable hashTable){
        
        //InputStream is = getClass().getResourceAsStream();
        
        int numElements = hashTable.size();
        byte[] byteArray = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            dout.writeInt(numElements);
            Enumeration e = hashTable.keys();
            while(e.hasMoreElements()) {
                Object key = e.nextElement();
                Object val = hashTable.get(key);
                dout.writeUTF(key.toString());
                dout.writeUTF(val.toString());
            }
            byteArray = bout.toByteArray();
            bout.close();
            dout.close();
            
            return byteArray;
        }catch(IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
}
