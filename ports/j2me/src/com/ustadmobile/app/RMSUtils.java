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
package com.ustadmobile.app;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Hashtable;
import javax.microedition.rms.*;

/**
 *
 * @author varuna
 */
public class RMSUtils {
    
    static String REC_STORE;
    //static String REC_STORE="UstadMobileApp";
    
    public RMSUtils(String recordName) {
        REC_STORE = recordName;
    }
    
    private RecordStore rs = null;
    
        
    public void openRMS(){
        try{
            rs = RecordStore.openRecordStore(REC_STORE, true);
        }catch (Exception e){}
    }
    
    public void closeRMS(){
        try{
            rs.closeRecordStore();
        }catch (Exception e){}
    }
    
    public void deleteRMS(){
        if(RecordStore.listRecordStores() != null ){
            try{
                RecordStore.deleteRecordStore(REC_STORE);
            }catch (Exception e){}
        }
    }
    
    public void writeRMS(String str){
        byte[] records = str.getBytes();
        try{
            rs.addRecord(records, 0, records.length);
        }catch (Exception e){}
    }
    
    public Hashtable readRMS(){
        try{
            Hashtable ht = new Hashtable();
            byte[] records = new byte[5];
            int length;
            for (int i = 1; i<= rs.getNumRecords(); i++){
                if (rs.getRecordSize(i) > records.length){
                    records = new byte[rs.getRecordSize(i)];
                }
                length = rs.getRecord(i, records, 0);
                ht.put(String.valueOf(i),new String(records, 0, length));
            }
            return ht;
        }catch (Exception e){}
        return null;
    }
    
    public void deleteRecord(String keyword){
        try{
            RecordEnumeration re = rs.enumerateRecords(null, null, true);
            int found = -1;
            while (re.hasNextElement()){
                int recordId = re.nextRecordId();
                String nextRecord = new String(re.nextRecord());
                if (nextRecord.startsWith(keyword)){
                    found = recordId;
                }
                if (found != -1 ){
                    rs.deleteRecord(found);
                }
            }
            
        }catch (Exception e){}
        
    }
    
    public void insertRecord(String record){
        byte[] recordBytes = record.getBytes();
        try{
            rs.addRecord(recordBytes, 0, recordBytes.length);
        }catch (Exception e){}
    }
    
    public void insertBytes(byte[] recordBytes){
        //byte[] recordBytes = record.getBytes();
        
        try{
            rs.addRecord(recordBytes, 0, recordBytes.length);
        }catch(Exception e) {}
        
        /*
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(recordBytes.length);
            dos.write(recordBytes);
            dos.write(recordBytes);
            rs.addRecord(recordBytes, 0, recordBytes.length);
        }catch (Exception e){}*/
    }
    
    public byte[] readBytes(){
        try{
            byte[] records = new byte[5];
            int length;
            for (int i = 1; i<= rs.getNumRecords(); i++){
                if (rs.getRecordSize(i) > records.length){
                    records = new byte[rs.getRecordSize(i)];
                }
                length = rs.getRecord(i, records, 0);
                //ht.put(String.valueOf(i),new String(records, 0, length));
            }
            //return ht;
            return records;
        }catch (Exception e){}
        return null;
    }
    
     
}
