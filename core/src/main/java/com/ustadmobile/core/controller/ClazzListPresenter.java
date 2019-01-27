package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Role;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_ATTENDANCE_ASC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_ATTENDANCE_DESC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_NAME_ASC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_NAME_DESC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_TEACHER_ASC;

/**
 * The ClazzList's Presenter - responsible for the logic of listing the relevant classes on the
 * Class list screen.
 */
public class ClazzListPresenter extends UstadBaseController<ClazzListView> {

    private UmProvider<ClazzWithNumStudents> clazzListProvider;

    public ClazzListPresenter(Object context, Hashtable arguments, ClazzListView view) {
        super(context, arguments, view);
    }

    private Hashtable<Long, Integer> idToOrderInteger;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    ClazzDao clazzDao = repository.getClazzDao();

    Long loggedInPersonUid = 0L;

    private Boolean recordAttendanceVisibility = false;

    public Boolean getRecordAttendanceVisibility() {
        return recordAttendanceVisibility;
    }

    public void setRecordAttendanceVisibility(Boolean recordAttendanceVisibility) {
        this.recordAttendanceVisibility = recordAttendanceVisibility;
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private String[] arrayListToStringArray(ArrayList<String> presetAL){
        Object[] objectArr = presetAL.toArray();
        String[] strArr = new String[objectArr.length];
        for(int j = 0 ; j < objectArr.length ; j ++){
            strArr[j] = (String) objectArr[j];
        }
        return strArr;
    }

    /**
     * The ClazzListPresenter does the following in order:
     *      1. Populates the clazzListProvider and sets it to the view.
     *      2. Updates the Sort drop down options.
     *
     * @param savedState The state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //clazzListProvider = repository.getClazzDao().findAllActiveClazzes();
        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();

        if(loggedInPersonUid != null){

            clazzListProvider = clazzDao.findAllClazzesByPersonUid(loggedInPersonUid);
            updateProviderToView();

            idToOrderInteger = new Hashtable<>();

            updateSortSpinnerPreset();

            //Check permissions
            checkPermissions();
        }
    }

    public void checkPermissions() {
        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_CLAZZ_INSERT,
            new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    view.showAddClassButton(result);
                    view.showAllClazzSettingsButton(result);

                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            })
        );

        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT,
                new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        setRecordAttendanceVisibility(result);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                })
        );
    }

    private void updateProviderToView(){
        view.setClazzListProvider(clazzListProvider);
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private void updateSortSpinnerPreset(){
        ArrayList<String> presetAL = new ArrayList<>();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        idToOrderInteger = new Hashtable<>();

        presetAL.add(impl.getString(MessageID.namesb, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_ASC);
        presetAL.add(impl.getString(MessageID.attendance_high_to_low, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_ATTENDANCE_DESC);
        presetAL.add(impl.getString(MessageID.attendance_low_to_high, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_ATTENDANCE_ASC);
        presetAL.add(impl.getString(MessageID.teacher, getContext()));
        idToOrderInteger.put((long)presetAL.size(), SORT_ORDER_TEACHER_ASC);
        String[] sortPresets = arrayListToStringArray(presetAL);

        view.updateSortSpinner(sortPresets);
    }

    /**
     * This method updates the Class List provider based on the sort order flag selected.
     * Every order has a corresponding order by change in the database query where this method
     * reloads the class list provider.
     *
     * @param order The order selected.
     */
    private void getAndSetProvider(int order){
        switch (order){

            case SORT_ORDER_NAME_DESC:
                clazzListProvider = repository.getClazzDao()
                        .findAllActiveClazzesSortByNameDesc();
                break;
            case SORT_ORDER_ATTENDANCE_ASC:
                clazzListProvider = repository.getClazzDao()
                        .findAllActiveClazzesSortByAttendanceAsc();
                break;
            case SORT_ORDER_ATTENDANCE_DESC:
                clazzListProvider = repository.getClazzDao()
                        .findAllActiveClazzesSortByAttendanceDesc();
                break;
            case SORT_ORDER_TEACHER_ASC:
                clazzListProvider = repository.getClazzDao()
                        .findAllActiveClazzesSortByTeacherAsc();
                break;
            default:
                //SORT_ORDER_NAME_ASC
                clazzListProvider = repository.getClazzDao()
                        .findAllActiveClazzesSortByNameAsc();
                break;
        }

        updateProviderToView();

    }

    /**
     * Click class card handler. This should go within a class - opening the Class Detail View.
     *
     * @param clazz The class the user clicked on.
     */
    public void handleClickClazz(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        long clazzUid = clazz.getClazzUid();
        args.put(ClazzListView.ARG_CLAZZ_UID, clazzUid);

        impl.go(ClassDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Click attendance button in Class card handler (as part of every Class item in the Class List
     * recycler view.
     *
     * @param clazz The class the user wants to record attendance for.
     */
    public void handleClickClazzRecordAttendance(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        long clazzUid = clazz.getClazzUid();
        args.put(ClazzListView.ARG_CLAZZ_UID, clazzUid);
        args.put(ARG_LOGDATE, System.currentTimeMillis());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * The primary action button for the Class List - here it is to add a new Class . (On Android it
     * is the Floating Action Button).
     * This method will create a new class, get its new ID and open up Class Edit View with the new
     * ID to edit it.
     *
     */
    public void handleClickPrimaryActionButton(){
        //Goes to ClazzEditActivity with currentClazzUid passed as argument

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Clazz  newClazz = new Clazz();
        ClazzDao clazzDao = repository.getClazzDao();


        clazzDao.insertAsync(newClazz, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Hashtable<String, Object> args = new Hashtable<>();
                args.put(ClazzListView.ARG_CLAZZ_UID, result);
                impl.go(ClazzEditView.VIEW_NAME, args, view.getContext());
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Method logic for what happens when we change the order of the clazz list.
     *
     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date
     */
    public void handleChangeSortOrder(long order){
        order=order+1;
        if(idToOrderInteger.containsKey(order)){
            int sortCode = idToOrderInteger.get(order);
            getAndSetProvider(sortCode);
        }
    }

    @Override
    public void setUIStrings() {

    }
}
