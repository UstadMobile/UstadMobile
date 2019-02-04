package com.ustadmobile.core.xlsx;

import java.util.ArrayList;
import java.util.List;

public class UmSheet{
    String title;
    List<TableValue> sheetValues;

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

    public UmSheet(){
        title = "Sheet";
        sheetValues = new ArrayList<>();
    }

    public UmSheet(String newTitle){
        this.title = newTitle;
        sheetValues = new ArrayList<>();
    }

    public UmSheet(String newTitle, List<TableValue> sheetValues){
        this.title = newTitle;
        this.sheetValues = sheetValues;
    }

    public void addValueToSheet(int r, int c, String value){
        TableValue newTableValue = new TableValue(r,c,value);
        sheetValues.add(newTableValue);
    }

}
