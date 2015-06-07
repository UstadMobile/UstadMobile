/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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