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
    HashMap<Long, Integer> nominatorToIdMap;
    HashMap<Long, Integer> nomineeToIdMap;

    public static final String TICK_UNICODE = "\u2713";
    public static final String CROSS_UNICODE = "\u2718";



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
        nominatorToIdMap = new HashMap<>();
        nomineeToIdMap = new HashMap<>();
        for (String clazzName : clazzToStudents.keySet()) {
            List<ClazzMemberWithPerson> students = clazzToStudents.get(clazzName);

            UmSheet clazzSheet = new UmSheet(clazzName);
            String nominating = "Nominating";
            //1st corner is "Nominating"
            clazzSheet.addValueToSheet(0, 0, nominating);



            int r = 0;
            int c = 1;
            int t = students.size();

            //Top Nominee Row ( 0th Row) and every X
            for (ClazzMemberWithPerson everyStudent : students) {

                String studentName = everyStudent.getPerson().getFirstNames() + " " +
                        everyStudent.getPerson().getLastName();

                clazzSheet.addValueToSheet(0, c, studentName);
                nomineeToIdMap.put(everyStudent.getClazzMemberUid(), c);

                c++;

            }

            //Every Nominator names
            r = 1;
            for (ClazzMemberWithPerson es : students) {
                nominatorToIdMap.put(es.getClazzMemberUid(), r);
                String nominatorName = es.getPerson().getFirstNames() + " " +
                        es.getPerson().getLastName();

                clazzSheet.addValueToSheet(r, 0, nominatorName);
                r++;

            }

            //Every x cross
            for(int j=1;j<=t;j++){
                for(int k = 1; k<=t;k++){
                    clazzSheet.addValueToSheet(j, k, CROSS_UNICODE);
                }
            }

            //Every -
            for(int j=1;j<=t;j++){
                clazzSheet.addValueToSheet(j,j, "-");
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
            ZipUtil.createEmptyZipFile(xlsxReportPath);

            UmXLSX umXLSX = new UmXLSX(title, xlsxReportPath, theWorkingPath);

            /*
                Sheet order
                Class A - Question 1 ]- Uses Class A template
                Class A - Question 2 ]
                Class B - Question 1 ]- Uses Class B template
                Class B - Question 2 ]

             */
            Iterator<String> clazzIterator = clazzMap.keySet().iterator();
            while(clazzIterator.hasNext()){

                String clazzName = clazzIterator.next();
                LinkedHashMap<String, Map<Long, List<Long>>> clazzData = clazzMap.get(clazzName);
                Iterator<String> questionIterator = clazzData.keySet().iterator();
                while(questionIterator.hasNext()){
                    String question = questionIterator.next();
                    Map<Long, List<Long>> questionData = clazzData.get(question);

                    UmSheet clazzSheet = clazzSheetTemplate.get(clazzName);
                    String sheetTitle = clazzName + " " + question;
                    if(sheetTitle.length() > 30){
                        sheetTitle = sheetTitle.substring(0,29);
                    }
                    String sheetTitleShort = sheetTitle.replace('?',' ');

                    UmSheet clazzQuestionSheet = new UmSheet(sheetTitleShort,
                            clazzSheet.getSheetValues(), clazzSheet.getSheetMap());

                    //TODO: Put in values to the sheet : Check
                    Iterator<Long> questionDataIterator = questionData.keySet().iterator();
                    while(questionDataIterator.hasNext()){
                        Long nominatorUid = questionDataIterator.next();
                        int r = nominatorToIdMap.get(nominatorUid);

                        List<Long> nomineeList = questionData.get(nominatorUid);
                        for(Long nominee:nomineeList){
                            int c = nomineeToIdMap.get(nominee);

                            //Put value
                            clazzQuestionSheet.addValueToSheet(r,c, TICK_UNICODE);
                        }
                    }


                    umXLSX.addSheet(clazzQuestionSheet);
                }
            }

            //Generate the xlsx report from the xlsx object.
            umXLSX.createXLSX();
            view.generateXLSReport(xlsxReportPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
