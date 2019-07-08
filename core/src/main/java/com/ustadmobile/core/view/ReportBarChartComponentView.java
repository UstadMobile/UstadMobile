package com.ustadmobile.core.view;


import java.util.List;

/**
 * Component for presenter access
 */
public interface ReportBarChartComponentView extends UstadView{

    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ReportBarChartComponentView";

    //Any argument keys:

    void setChartData(List<Object> dataSet);

}

