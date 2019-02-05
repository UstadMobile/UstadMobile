package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseNominationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ReportSELView;
import com.ustadmobile.core.xlsx.UmSheet;
import com.ustadmobile.core.xlsx.UmXLSX;
import com.ustadmobile.core.xlsx.ZipUtil;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.SELNominationItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.controller.ReportOverallAttendancePresenter.convertLongArray;
import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;

public class ReportSELPresenter extends UstadBaseController<ReportSELView> {

    private long fromDate, toDate;
    private List<Long> clazzList;
    UmAppDatabase repository;
    LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap;
    HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents;
    HashMap<String, UmSheet> clazzSheetTemplate;


    public ReportSELPresenter(Object context, Hashtable arguments, ReportSELView view) {
        super(context, arguments, view);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        clazzList = new ArrayList<>();

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            long[] clazzes = (long[]) arguments.get(ARG_CLAZZ_LIST);
            clazzList = convertLongArray(clazzes);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getRawData();
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     *
     * The SEL Raw data has the following format:
     * [SEL Raw Data] is:
     *  [Clazz Name, [Question Data Map]]
     *      [ClazzName, [Question Name, [Question Nominations]]]
     *          [ClazzName, [Question Name, [Nominator ClazzMember Uid, List<Nominee Uids>]]]
     *
     * eg:
     * [ Class A, ["Who let the dogs out?", [Nominator 1 Uid, <Nominee1Uid, Nominee2Uid, ...>]]]
     * [ Class A, ["Who let the dogs out?", [Nominator 2 Uid, <Nominee3Uid, Nominee4Uid, ...>]]]
     * [ Class A, ["Who are your friends?", [Nominator 1 Uid, <...>]]]
     * [Class B, ["Who let the dogs out?", ...]]]
     * ...
     *
     */
    private void getRawData() {
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();

        clazzToStudents = new HashMap<>();
        clazzMap = new LinkedHashMap<>();

        SocialNominationQuestionResponseNominationDao nominationDao =
                repository.getSocialNominationQuestionResponseNominationDao();

        //Get all nominations
        nominationDao.getAllNominationReportAsync(fromDate, toDate, clazzList,
                new UmCallback<List<SELNominationItem>>() {
            @Override
            public void onSuccess(List<SELNominationItem> allNominations) {

                //TODO: Handle QuestionSet grouping ? (Not implemented in Prototypes)
                int index = 0;
                for(SELNominationItem everyNomination: allNominations){ //For every nomination / all
                    index++;
                    long thisClazzUid = everyNomination.getClazzUid();
                    String thisClazzName = everyNomination.getClazzName();
                    String thisQuestionTitle = everyNomination.getQuestionText();
                    long nomineeUid = everyNomination.getNomineeUid();
                    long nominatorUid = everyNomination.getNominatorUid();

                    LinkedHashMap<String, Map<Long, List<Long>>> questionMap;
                    Map<Long, List<Long>> nominationMap;
                    List<Long> nominations;

                    if(!clazzMap.containsKey(thisClazzName)){
                        //New Clazz starts. Add question Map and nominations map to every Question.
                        questionMap = new LinkedHashMap<>();
                        nominationMap = new HashMap<>();
                        nominations = new ArrayList<>();

                    }else{
                        questionMap = clazzMap.get(thisClazzName);

                        if(!questionMap.containsKey(thisQuestionTitle)){
                            nominationMap = new HashMap<>();
                            nominations = new ArrayList<>();
                        }else{
                            nominationMap = questionMap.get(thisQuestionTitle);
                            if(!nominationMap.containsKey(nominatorUid)){
                                nominations = new ArrayList<>();
                            }else{
                                nominations = nominationMap.get(nominatorUid);
                            }
                        }
                    }

                    nominations.add(nomineeUid);
                    nominationMap.put(nominatorUid, nominations);
                    questionMap.put(thisQuestionTitle, nominationMap);

                    clazzMap.put(thisClazzName, questionMap);

                    //Build students map - Add students
                    if(!clazzToStudents.containsKey(thisClazzName)) {

                        int finalIndex = index;
                        clazzMemberDao.findClazzMemberWithPersonByRoleForClazzUid(thisClazzUid,
                            ClazzMember.ROLE_STUDENT, new UmCallback<List<ClazzMemberWithPerson>>() {
                                @Override
                                public void onSuccess(List<ClazzMemberWithPerson> result) {
                                    clazzToStudents.put(thisClazzName, result);

                                    //If end of the loop
                                    if(finalIndex >= allNominations.size()){
                                        createTablesOnView();
                                        createClassSheetTemplates();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });
                    }else{
                        //If end of the loop
                        if(index >= allNominations.size()){
                            createTablesOnView();
                            createClassSheetTemplates();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Send sel raw data to view for table updating.
     */
    private void createTablesOnView(){
        view.runOnUiThread(() -> view.createTables(clazzMap, clazzToStudents));
    }

    private void createClassSheetTemplates(){
        clazzSheetTemplate = new HashMap<>();
        for (String clazzName : clazzToStudents.keySet()) {
            List<ClazzMemberWithPerson> students = clazzToStudents.get(clazzName);

            UmSheet clazzSheet = new UmSheet(clazzName);
            int r = 0;
            int c = 0;
            String nominating = "Nominating";
            clazzSheet.addValueToSheet(0, 0, nominating);
            for (ClazzMemberWithPerson everyStudent : students) {

                String studentName = everyStudent.getPerson().getFirstNames() + " " +
                        everyStudent.getPerson().getLastName();

                if(c > 0) {
                    clazzSheet.addValueToSheet(0, c, studentName);
                }

                if (c == 0) {
                    r = 1;
                    for (ClazzMemberWithPerson es : students) {
                        clazzSheet.addValueToSheet(r, c, studentName);
                        r++;
                    }
                }
                c++;
            }
            clazzSheetTemplate.put(clazzName, clazzSheet);
        }
    }


    /**
     * Generates the excel file with th ecurrently set data.
     * @param title             The title of the excel file
     * @param xlsxReportPath    The .xlsx file path (to be created)
     * @param theWorkingPath    The working directory where the xlsx file will be worked on.
     */
    public void dataToXLSX(String title, String xlsxReportPath, String theWorkingPath){

        try {
            File xlsxFile = ZipUtil.createEmptyZipFile(xlsxReportPath);

            UmXLSX umXLSX = new UmXLSX(title, xlsxReportPath, theWorkingPath);



            //TODO: Generate sheets from the SEL Report data
            Iterator<String> clazzIterator = clazzMap.keySet().iterator();
            while(clazzIterator.hasNext()){

                String clazzName = clazzIterator.next();
                LinkedHashMap<String, Map<Long, List<Long>>> clazzData = clazzMap.get(clazzName);
                Iterator<String> questionIterator = clazzData.keySet().iterator();
                while(questionIterator.hasNext()){
                    String question = questionIterator.next();
                    Map<Long, List<Long>> questionData = clazzData.get(question);

                }
            }



            //Testing XLSX:

            UmSheet newSheet = new UmSheet("Test sheet");
            newSheet.addValueToSheet(0,0, "The");
            newSheet.addValueToSheet(0,1, "Quick");
            newSheet.addValueToSheet(0,2, "Brown");
            newSheet.addValueToSheet(0,3, "Fox");
            newSheet.addValueToSheet(1,0, "Jumped");
            newSheet.addValueToSheet(1,1, "Over");
            newSheet.addValueToSheet(1,2, "The");
            newSheet.addValueToSheet(1,3, "Lazy");
            newSheet.addValueToSheet(2,0, "Dog");
            newSheet.addValueToSheet(2,1, "And");
            newSheet.addValueToSheet(2,2, "Then");
            newSheet.addValueToSheet(2,3, "Sleeps");

            umXLSX.addSheet(newSheet);

            UmSheet newSheet2 = new UmSheet("Another Test sheet");
            newSheet2.addValueToSheet(0,0, "The");
            newSheet2.addValueToSheet(0,1, "Quick");
            newSheet2.addValueToSheet(0,2, "Brown");
            newSheet2.addValueToSheet(0,3, "Fox");
            newSheet2.addValueToSheet(1,0, "Jumped");

            umXLSX.addSheet(newSheet2);


            //Generate the xlsx report from the xlsx object.
            umXLSX.createXLSX();
            view.generateXLSReport(xlsxReportPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
