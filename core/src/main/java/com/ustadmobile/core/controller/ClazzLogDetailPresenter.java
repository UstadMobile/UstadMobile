package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.UMCalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_ABSENT;
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_ATTENDED;
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_PARTIAL;


/**
 * ClazzLogDetail's presenter - responsible for the logic of displaying (and editing) every
 * Attendance Logs Entry attempt for a clazz and a date. (who's id we get from arguments). This is common
 * with a new Attendance Log Entry.
 *
 */
public class ClazzLogDetailPresenter extends UstadBaseController<ClassLogDetailView> {

    private long currentClazzUid = -1L;
    private long currentLogDate = -1L;

    private UmProvider<ClazzLogAttendanceRecordWithPerson> clazzLogAttendanceRecordUmProvider;


    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    private ClazzLog currentClazzLog;
    public Clazz currentClazz;

    public ClazzLogDetailPresenter(Object context,
                                   Hashtable arguments,
                                   ClassLogDetailView view) {
        super(context, arguments, view);

        //Get clazz uid and set it
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = Long.parseLong(arguments.get(ARG_CLAZZ_UID).toString());
        }

        //Get log date and set it
        if(arguments.containsKey(ClazzListView.ARG_LOGDATE)){
            String thisLogDate = arguments.get(ClazzListView.ARG_LOGDATE).toString();
            currentLogDate = Long.parseLong(thisLogDate);
        }
    }

    /**
     * UstadBaseController\s setUiString()
     * Doesn't do anything here.
     */
    @Override
    public void setUIStrings() {

    }

    /**
     * The Presenter's onCreate. This populated the provider and sets it to the View.
     *
     * This will be called when the implementation view is ready.
     * (ie: on Android, this is called in the Activity's onCreateView() )
     *
     * Steps to get there:
     * 1. For a particular Clazz UID and date, we find if an existing ClazzLog exists.
     * 2. If it does not, we create it.
     * 3. We then call insertAllAndSetProvider where it checks the records associated and set the
     * provider.
     *
     * @param savedState This is generally the state which Android resumes this app. This is not
     *                   the arguments. It will most likely be null in a normal application run.
      */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        setUpLogDetail();
    }

    /**
     * Sets up Log Detail Attendance View.
     */
    private void setUpLogDetail(){
        //Check for ClassLog
        ClazzLogDao clazzLogDao = repository.getClazzLogDao();
        ClazzDao clazzDao = repository.getClazzDao();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        clazzLogDao.findByClazzIdAndDateAsync(currentClazzUid, currentLogDate,
                new UmCallback<ClazzLog>() {
                    @Override
                    public void onSuccess(ClazzLog result) {

                        currentClazz = clazzDao.findByUid(currentClazzUid);

                        view.updateToolbarTitle(currentClazz.getClazzName() + " "
                                + impl.getString(MessageID.attendance, context));

                        updateViewDateHeading();

                        if(result == null){
                            //Create one anyway if not set
                            clazzLogDao.createClazzLogForDate(currentClazzUid, currentLogDate,
                                    new UmCallback<Long>() {
                                        @Override
                                        public void onSuccess(Long result) {

                                            currentClazzLog = clazzLogDao.findByUid(result);
                                            insertAllAndSetProvider(currentClazzLog);
                                        }
                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    });
                        }else{
                            currentClazzLog = result;
                            insertAllAndSetProvider(currentClazzLog);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    public void updateViewDateHeading(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Date currentLogDateDate = new Date(currentLogDate);
        String prettyDate="";
        if(UMCalendarUtil.isToday(currentLogDateDate)){
            prettyDate = impl.getString(MessageID.today, context);
        }
        Locale currentLocale = Locale.getDefault();
        prettyDate += " (" +
                UMCalendarUtil.getPrettyDateFromLong(currentLogDate, currentLocale) + ")";

        view.updateDateHeading(prettyDate);
    }

    /**
     * Common method to insert all attendance records for a clazz log uid and prepare its provider
     * to be set to the view.
     *
     * @param result The ClazzLog for which to insert and get provider data for.
     */
    private void insertAllAndSetProvider(ClazzLog result){

        ClazzLogAttendanceRecordDao clazzLogAttendanceRecordDao =
                repository.getClazzLogAttendanceRecordDao();

        clazzLogAttendanceRecordDao.insertAllAttendanceRecords(currentClazzUid,
                result.getClazzLogUid(), new UmCallback<Long[]>() {
                    @Override
                    public void onSuccess(Long[] result2) {
                        //Get provider
                        clazzLogAttendanceRecordUmProvider = repository
                                .getClazzLogAttendanceRecordDao()
                                .findAttendanceRecordsWithPersonByClassLogId(result.getClazzLogUid());
                        //Set to view
                        view.runOnUiThread(() ->
                                view.setClazzLogAttendanceRecordProvider(
                                        clazzLogAttendanceRecordUmProvider));
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Re loads the attendance log detail view
     *
     * @param newDate The new date set
     */
    public void reloadLogDetailForDate(long newDate){
        System.out.println("Reload for date: " + newDate);

        //1. Set currentLogDate to newDate
        currentLogDate = newDate;

        //2. Re load view and recycler
        setUpLogDetail();

        //3. Update date heading
        updateViewDateHeading();


    }

    public void handleClickGoBackDate(){
        long newDate = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(currentLogDate, -1);
        System.out.println("Go back: " + newDate);

        reloadLogDetailForDate(newDate);

    }

    public void handleClickGoForwardDate(){
        Date currentLogDateDate = new Date(currentLogDate);

        if(!UMCalendarUtil.isToday(currentLogDateDate)){
            if(currentLogDate < System.currentTimeMillis()){
                //Go to next day's
                long newDate = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(currentLogDate, 1);
                System.out.println("go forawrd: " + newDate);
                reloadLogDetailForDate(newDate);

            }
        }

    }

    /**
     * Method logic to what happens when we click "Done" on the ClassLogDetail View
     */
    public void handleClickDone(){
        //1. Update Done status on ClazzLog for this clazzLogUid
        ClazzLogDao clazzLogDao = repository.getClazzLogDao();
        ClazzDao clazzDao = repository.getClazzDao();
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();
        ClazzLogAttendanceRecordDao clazzLogAttendanceRecordDao =
                repository.getClazzLogAttendanceRecordDao();
        currentClazzLog.setDone(true);
        clazzLogDao.updateDoneForClazzLogAsync(currentClazzLog.getClazzLogUid(),
                new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                //2. Update Attendance numbers for this clazzUid
                clazzDao.updateAttendancePercentage(currentClazzUid);
                //3. Update Attendance numbers for ClazzMember for this clazzUid.
                clazzMemberDao.updateAttendancePercentages(currentClazzUid);

                int numPresent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_ATTENDED);
                int numAbsent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_ABSENT);
                int numPartial = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_PARTIAL);

                clazzLogDao.updateClazzAttendanceNumbersAsync(currentClazzLog.getClazzLogUid(),
                        numPresent, numAbsent, numPartial, null);

                //4. Set any parent feed to done.
                FeedEntryDao feedEntryDao =
                        repository.getFeedEntryDao();
                String possibleFeedLink = ClassLogDetailView.VIEW_NAME + "?" +
                        ClazzListView.ARG_CLAZZ_UID + "=" + currentClazzUid +
                        "&" + ClazzListView.ARG_LOGDATE + "=" + currentLogDate;
                FeedEntry parentFeed =
                        feedEntryDao.findByLink(FeedListPresenter.TEST_DEFAULT_PERSON_UID, possibleFeedLink);
                if(parentFeed != null){
                    parentFeed.setFeedEntryDone(false);
                    feedEntryDao.updateDoneTrue(parentFeed.getFeedEntryUid());
                }

                //5. Close the activity.
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

//    /**
//     * Method logic for what happens when we change the order of the student list.
//     *
//     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date.
//     */
//    public void handleChangeSortOrder(int order){
//        //TODO: Change provider's sort order
//    }

    /**
     * Handle when the user taps to mark all present, or mark all absent. This will update the
     * database to set all ClazzLogAttendanceRecord
     *
     * @param attendanceStatus attendance status to set for all ClazzLogAttendanceRecords that
     *                         are in this ClazzLog
     */
    public void handleMarkAll(int attendanceStatus){
        repository.getClazzLogAttendanceRecordDao()
                .updateAllByClazzLogUid(currentClazzLog.getClazzLogUid(),
                        attendanceStatus, null);
    }

    /**
     * Handle when the user taps a student and marks it. This will update the database and
     * therefore the recycler view via LiveData.
     *
     * @param clazzLogAttendanceRecordUid       The Attendance Log Entry's per person Record Uid
     * @param attendanceStatus      The Person Attendance Log Entry's attendance status. Can be:
     *                              STATUS_ATTENDED, STATUS_ABSENT, STATUS_PARTIAL as defined in
     *                              ClazzLogAttendanceRecord
     */
    public void handleMarkStudent(long clazzLogAttendanceRecordUid, int attendanceStatus) {
        repository  .getClazzLogAttendanceRecordDao()
                .updateAttendanceStatus(clazzLogAttendanceRecordUid,
                        attendanceStatus, null);
    }

}
