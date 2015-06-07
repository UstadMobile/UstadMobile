/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app;

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
