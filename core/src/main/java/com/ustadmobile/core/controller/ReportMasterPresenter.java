package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ReportMasterView;
import com.ustadmobile.core.xlsx.UmSheet;
import com.ustadmobile.core.xlsx.UmXLSX;
import com.ustadmobile.core.xlsx.ZipUtil;
import com.ustadmobile.lib.db.entities.ReportMasterItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOverallAttendancePresenter.convertLongArray;
import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;

public class ReportMasterPresenter extends UstadBaseController<ReportMasterView> {

    private long fromDate, toDate;
    private List<Long> clazzList, locationList;
    private boolean genderDisaggregated = false;

    private List<ReportMasterItem> dataMap;

    UmAppDatabase repository;


    public ReportMasterPresenter(Object context, Hashtable arguments, ReportMasterView view) {
        super(context, arguments, view);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        dataMap = new ArrayList<>();
        clazzList = new ArrayList<>();
        locationList = new ArrayList<>();

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }

        if(arguments.containsKey(ARG_LOCATION_LIST)){
            long[] locations = (long[]) arguments.get(ARG_LOCATION_LIST);
            locationList = convertLongArray(locations);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            long[] clazzes = (long[]) arguments.get(ARG_CLAZZ_LIST);
            clazzList = convertLongArray(clazzes);
        }

        if(arguments.containsKey(ARG_GENDER_DISAGGREGATE)){
            genderDisaggregated = (Boolean) arguments.get(ARG_GENDER_DISAGGREGATE);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getDataAndUpdateTable();
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private void getDataAndUpdateTable() {

        long currentTime = System.currentTimeMillis();

        ClazzLogAttendanceRecordDao attendanceRecordDao =
                repository.getClazzLogAttendanceRecordDao();

        attendanceRecordDao.findMasterReportDataForAllAsync(fromDate, toDate,
                new UmCallback<List<ReportMasterItem>>() {
            @Override
            public void onSuccess(List<ReportMasterItem> result) {
                view.runOnUiThread(() -> view.updateTables(result));
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });



    }

    public void dataToXLSX(String title, String xlsxReportPath, String workingDir,
                           List<String[]> tableTextData) {

        try {
            ZipUtil.createEmptyZipFile(xlsxReportPath);

            UmXLSX umXLSX = new UmXLSX(title, xlsxReportPath, workingDir);

            UmSheet reportSheet = new UmSheet("Report");
            reportSheet.addValueToSheet(0,0, "Class ID");
            reportSheet.addValueToSheet(0,1, "First name");
            reportSheet.addValueToSheet(0,2, "Last name");
            reportSheet.addValueToSheet(0,3, "Student ID");
            reportSheet.addValueToSheet(0,4, "Number days present");
            reportSheet.addValueToSheet(0,5, "Number absent");
            reportSheet.addValueToSheet(0,6, "Number partial");
            reportSheet.addValueToSheet(0,7, "Total class days");
            reportSheet.addValueToSheet(0,8, "Date left");
            reportSheet.addValueToSheet(0,9, "Active");
            reportSheet.addValueToSheet(0,10, "Gender");
            reportSheet.addValueToSheet(0,11, "Birthday");

            //Remove already put headers
            tableTextData.remove(0);

            //Loop over tableTextData
            int r = 1;
            for (String[] tableTextDatum : tableTextData) {
                int c = 0;
                for (int i = 0; i < tableTextDatum.length; i++) {
                    String value = tableTextDatum[i];
                    reportSheet.addValueToSheet(r, c, value);
                    c++;
                }
                r++;
            }
            umXLSX.addSheet(reportSheet);

            //Generate the xlsx report from the xlsx object.
            umXLSX.createXLSX();
            view.generateXLSXReport(xlsxReportPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
