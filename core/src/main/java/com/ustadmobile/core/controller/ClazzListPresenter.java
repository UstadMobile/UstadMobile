package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_ATTENDANCE_ASC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_ATTENDANCE_DESC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_NAME_ASC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_NAME_DESC;

public class ClazzListPresenter extends UstadBaseController<ClazzListView> {

    private long currentPersonUid = -1L;

    public static final String ARG_CLAZZ_UID = "ClazzUid";

    private UmProvider<ClazzWithNumStudents> clazzListProvider;

    public ClazzListPresenter(Object context, Hashtable arguments, ClazzListView view) {
        super(context, arguments, view);
    }

    Hashtable<Long, Integer> idToOrderInteger;
    String[] sortPresets;


    public String[] arrayListToStringArray(ArrayList<String> presetAL){
        Object[] objectArr = presetAL.toArray();
        String[] strArr = new String[objectArr.length];
        for(int j = 0 ; j < objectArr.length ; j ++){
            strArr[j] = (String) objectArr[j];
        }
        return strArr;
    }

    /**
     * The ClazzListPresenter does the following:
     * 1. Populates the clazzListProvider and sets it to the view.
     *
     * @param savedState The state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //TODO: Remove. Replace with Logged-In User
        currentPersonUid = 1L;

        clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                .findAllActiveClazzes();
        view.setClazzListProvider(clazzListProvider);

        idToOrderInteger = new Hashtable<>();

        updateSortSpinnerPreset();
    }

    /**
     * Updates preset on the view.
     * TOOD: clean this.
     */
    public void updateSortSpinnerPreset(){
        ArrayList<String> presetAL = new ArrayList<>();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        idToOrderInteger = new Hashtable<>();

        presetAL.add(impl.getString(MessageID.namesb, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_ASC);
        presetAL.add(impl.getString(MessageID.attendance_high_to_low, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_ATTENDANCE_DESC);
        presetAL.add(impl.getString(MessageID.attendance_low_to_high, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_ATTENDANCE_ASC);
        sortPresets = arrayListToStringArray(presetAL);

        view.updateSortSpinner(sortPresets);
    }

    private void getAndSetProvider(int order){
        switch (order){

            case SORT_ORDER_NAME_DESC:
                clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                        .findAllActiveClazzesSortByNameDesc();
                break;
            case SORT_ORDER_ATTENDANCE_ASC:
                clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                        .findAllActiveClazzesSortByAttendanceAsc();
                break;
            case SORT_ORDER_ATTENDANCE_DESC:
                clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                        .findAllActiveClazzesSortByAttendanceDesc();
                break;
            default:
                //SORT_ORDER_NAME_ASC
                clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                        .findAllActiveClazzesSortByNameAsc();
                break;
        }
        view.setClazzListProvider(clazzListProvider);
    }

    /**
     * Click class card handler
     *
     * @param clazz
     */
    public void handleClickClazz(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        long clazzUid = clazz.getClazzUid();
        args.put(ARG_CLAZZ_UID, clazzUid);

        impl.go(ClassDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Click attendance button in Class card handler
     *
     * @param clazz
     */
    public void handleClickClazzRecordAttendance(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        long clazzUid = clazz.getClazzUid();
        args.put(ARG_CLAZZ_UID, clazzUid);
        args.put(ARG_LOGDATE, System.currentTimeMillis());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }


    public void handleClickPrimaryActionButton(){
        //Goes to ClazzEditActivity with currentClazzUid passed as argument

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Clazz  newClazz = new Clazz();
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();


        clazzDao.insertAsync(newClazz, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Hashtable args = new Hashtable();
                args.put(ARG_CLAZZ_UID, result);
                impl.go(ClazzEditView.VIEW_NAME, args, view.getContext());
            }

            @Override
            public void onFailure(Throwable exception) {

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
