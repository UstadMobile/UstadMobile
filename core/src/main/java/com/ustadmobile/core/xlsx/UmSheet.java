package com.ustadmobile.core.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class UmSheet{
    String title;
    List<TableValue> sheetValues;
    LinkedHashMap<Integer, LinkedHashMap<Integer, String>> sheetMap;

    public class TableValue{
        int rowIndex;
        int colIndex;
        String value;

        TableValue(int r, int c, String v){
            this.rowIndex = r;
            this.colIndex = c;
            this.value = v;
        }
    }

    public UmSheet(String newTitle){
        this.title = newTitle;
        this.sheetValues = new ArrayList<>();
        this.sheetMap = new LinkedHashMap<>();
    }

    public UmSheet(String newTitle, List<TableValue> sheetValues,
                   LinkedHashMap<Integer, LinkedHashMap<Integer, String>> sheetMap){

        this.title = newTitle;
        this.sheetValues = sheetValues;
        this.sheetMap = sheetMap;

    }

    public List<TableValue> getTableValueList(){
        if(sheetMap == null){
            return sheetValues;
        }
        List<TableValue> returnMe = new ArrayList<>();
        Iterator<Integer> sheetIterator = sheetMap.keySet().iterator();
        while(sheetIterator.hasNext()){
            int r = sheetIterator.next();
            LinkedHashMap<Integer, String> coMap = sheetMap.get(r);
            Iterator<Integer> colIterator = coMap.keySet().iterator();
            while(colIterator.hasNext()){
                int c = colIterator.next();
                String v = coMap.get(c);
                returnMe.add(new TableValue(r,c,v));
            }
        }
        return returnMe;
    }

    public void addValueToSheet(int r, int c, String value){
        TableValue newTableValue = new TableValue(r,c,value);
        LinkedHashMap<Integer, String> insideMap = new LinkedHashMap<>();
        //replace
        if(sheetMap.containsKey(r)){
            insideMap = sheetMap.get(r);
            if(insideMap.containsKey(c)){
                insideMap.put(c, value);
            }else{
                insideMap.put(c, value);
            }
        }else{
            insideMap.put(c, value);
        }

        sheetMap.put(r, insideMap);
        sheetValues.add(newTableValue);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TableValue> getSheetValues() {
        return sheetValues;
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, String>> getSheetMap() {
        return sheetMap;
    }
}
