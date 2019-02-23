package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.ScheduledCheck;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

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

    private boolean hasEditPermissions = false;

    private long loggedInPersonUid = 0L;

    private List<ClazzLogDao.ClazzLogUidAndDate> currentClazzLogs;

    private ClazzLog currentClazzLog;

    private int currentClazzLogIndex;

    private String title;

    public boolean isHasEditPermissions() {
        return hasEditPermissions;
    }

    public void setHasEditPermissions(boolean hasEditPermissions) {
        this.hasEditPermissions = hasEditPermissions;
    }

    private UmProvider<ClazzLogAttendanceRecordWithPerson> clazzLogAttendanceRecordUmProvider;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    private UmCallback<ClazzLog> setupFromClazzLogCallback = new UmCallback<ClazzLog>() {
        @Override
        public void onSuccess(ClazzLog clazzLog) {
            if(clazzLog != null) {
                currentClazzLog = clazzLog;
                insertAllAndSetProvider(clazzLog);
                loadClazzLogListForClazz();
                checkPermissions();

                if(title == null) {
                    repository.getClazzDao().getClazzNameAsync(clazzLog.getClazzLogClazzUid(),
                            setTitleCallback);
                }

                view.runOnUiThread(() -> updateViewDateHeading());
            }else {
                //TODO: show error message to user + record - should not happen
            }

        }

        @Override
        public void onFailure(Throwable exception) {

        }
    };

    private UmCallback<String> setTitleCallback = new UmCallback<String>() {
        @Override
        public void onSuccess(String result) {
            title = result + " " + UstadMobileSystemImpl.getInstance().getString(
                    MessageID.attendance, getContext());
            view.runOnUiThread(() -> view.updateToolbarTitle(title));
        }

        @Override
        public void onFailure(Throwable exception) {

        }
    } ;

    public ClazzLogDetailPresenter(Object context,
                                   Hashtable arguments,
                                   ClassLogDetailView view) {
        super(context, arguments, view);
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

        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();

        //Get clazz uid and set it
        if(getArguments().containsKey(ClassLogDetailView.ARG_CLAZZ_LOG_UID)){
            long clazzLogUid = Long.parseLong(getArguments().get(
                    ClassLogDetailView.ARG_CLAZZ_LOG_UID).toString());
            repository.getClazzLogDao().findByUidAsync(clazzLogUid, setupFromClazzLogCallback);
        }else if(getArguments().containsKey(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID)) {
            long clazzUid = Long.parseLong(getArguments().get(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID).toString());
            repository.getClazzLogDao().findMostRecentByClazzUid(clazzUid, setupFromClazzLogCallback);
        }
    }



    public void checkPermissions(){
        ClazzDao clazzDao = repository.getClazzDao();

        clazzDao.personHasPermission(loggedInPersonUid, currentClazzLog.getClazzLogClazzUid(),
                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT,
                new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        setHasEditPermissions(result);
                        view.showMarkAllButtons(result);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                }));
    }

    private void loadClazzLogListForClazz() {
        repository.getClazzLogDao().getListOfClazzLogUidsAndDatesForClazz(
                currentClazzLog.getClazzLogClazzUid(),
                new UmCallback<List<ClazzLogDao.ClazzLogUidAndDate>>() {
            @Override
            public void onSuccess(List<ClazzLogDao.ClazzLogUidAndDate> result) {
                currentClazzLogs = result;
                currentClazzLogIndex = currentClazzLogs.indexOf(
                        new ClazzLogDao.ClazzLogUidAndDate(currentClazzLog));
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


    public void updateViewDateHeading(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Date currentLogDateDate = new Date(currentClazzLog.getLogDate());
        String prettyDate="";
        if(UMCalendarUtil.isToday(currentLogDateDate)){
            prettyDate = impl.getString(MessageID.today, context);
        }
        Locale currentLocale = Locale.getDefault();
        prettyDate += " (" +
                UMCalendarUtil.getPrettyDateFromLong(currentClazzLog.getLogDate(), currentLocale) + ")";

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

        clazzLogAttendanceRecordDao.insertAllAttendanceRecords(currentClazzLog.getClazzLogClazzUid(),
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

    public void handleClickGoBackDate(){
        incrementLogInList(-1);

    }

    private void incrementLogInList(int inc) {
        int nextIndex = currentClazzLogIndex + inc;
        if(nextIndex > 0 && nextIndex < currentClazzLogs.size())
            repository.getClazzLogDao().findByUidAsync(currentClazzLogs.get(nextIndex).getClazzLogUid(),
                setupFromClazzLogCallback);
    }

    public void handleClickGoForwardDate(){
        incrementLogInList(1);
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
                clazzDao.updateAttendancePercentage(currentClazzLog.getClazzLogClazzUid());
                //3. Update Attendance numbers for ClazzMember for this clazzUid.
                clazzMemberDao.updateAttendancePercentages(currentClazzLog.getClazzLogClazzUid());

                int numPresent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_ATTENDED);
                int numAbsent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_ABSENT);
                int numPartial = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_PARTIAL);

                clazzLogDao.updateClazzAttendanceNumbersAsync(currentClazzLog.getClazzLogUid(),
                        numPresent, numAbsent, numPartial, null);

                //4. Set any FeedEntry to done
                repository.getFeedEntryDao().markEntryAsDoneByClazzLogUidAndTaskType(
                        currentClazzLog.getClazzLogUid(),
                        ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER, true);

                //5. Close the activity.
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }


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
        repository.getClazzLogAttendanceRecordDao()
                .updateAttendanceStatus(clazzLogAttendanceRecordUid,
                        attendanceStatus, null);
    }

}
