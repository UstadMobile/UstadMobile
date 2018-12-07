package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.UMCalendar;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_STUDENT_IDENTIFIER_NUMBER;
import static com.ustadmobile.core.view.ReportEditView.ARG_STUDENT_IDENTIFIER_PERCENTAGE;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;


/**
 * The ReportNumberOfDaysClassesOpen Presenter.
 */
public class ReportNumberOfDaysClassesOpenPresenter
        extends UstadBaseController<ReportNumberOfDaysClassesOpenView> {

    private long fromDate;
    private long toDate;
    private Long[] locations;
    private Long[] clazzes;


    LinkedHashMap<Float, Float> dataMaps;
    LinkedHashMap<String, Float> tableData;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);


    public ReportNumberOfDaysClassesOpenPresenter(Object context, Hashtable arguments, ReportNumberOfDaysClassesOpenView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }
        if(arguments.containsKey(ARG_LOCATION_LIST)){
            locations = (Long[]) arguments.get(ARG_LOCATION_LIST);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            clazzes = (Long[]) arguments.get(ARG_CLAZZ_LIST);
        }


    }

    public void dataToCSV(){

    }

    public void dataToXLS(){

    }

    public void dataToJSON(){

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getNumberOfDaysOpenDataAndUpdateCharts();

    }


    public void getNumberOfDaysOpenDataAndUpdateCharts(){


        LinkedHashMap<Float, Float> dataMap = new LinkedHashMap<>();


        for (int i=0; i<7;  i++){
            Long iDate = UMCalendarUtil.getDateInMilliPlusDays(-i);
            dataMap.put(iDate.floatValue(), (float) (4 + i));
        }




        view.updateBarChart(dataMap);

    }

    @Override
    public void setUIStrings() {

    }

}
