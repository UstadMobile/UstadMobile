package com.ustadmobile.core.view;


import java.util.List;

/**
 * For presenter's access to Custom views (that implement this)
 */
public interface ReportTableListComponentView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ReportTableListComponentView";

    //Any argument keys:

    void setSalesLogData(List<Object> dataSet);

    void setTopLEsData(List<Object> dataSet);
}

