package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Hashtable;

import com.ustadmobile.core.view.SelectProducerView;
import com.ustadmobile.core.view.SelectSaleProductView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import com.ustadmobile.core.db.dao.PersonDao;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;

/**
 * Presenter for SelectProducer view
 **/
public class SelectProducerPresenter extends UstadBaseController<SelectProducerView> {

    private UmProvider<Person> umProvider;
    UmAppDatabase repository;
    private PersonDao providerDao;
    private long saleItemUid = 0;

    private Hashtable<Long, Integer> idToOrderInteger;
    private static final int SORT_ORDER_NAME_ASC=1;
    private static final int SORT_ORDER_NAME_DESC=2;
    private static final int SORT_ORDER_MOST_USED=3;

    public SelectProducerPresenter(Object context, Hashtable arguments, SelectProducerView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getPersonDao();

        if(getArguments().containsKey(ARG_SALE_ITEM_UID)){
            saleItemUid = Long.valueOf((String) getArguments().get(ARG_SALE_ITEM_UID));
        }else{
            //Create a new SaleItem? - shouldn't happen.
            //throw exception.
        }


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        idToOrderInteger = new Hashtable<>();
        updateSortSpinnerPreset();

    }

    public void getAndSetProvider(int sortCode){
        switch (sortCode){
            case SORT_ORDER_NAME_ASC:
                //Get provider
                umProvider = providerDao.findAllPeopleNameAscProvider();
                view.setListProvider(umProvider);
                break;
            case SORT_ORDER_NAME_DESC:
                //Get provider
                umProvider = providerDao.findAllPeopleNameDescProvider();
                view.setListProvider(umProvider);
                break;
            case SORT_ORDER_MOST_USED:
                break;
            default:
                //Get provider
                umProvider = providerDao.findAllPeopleProvider();
                view.setListProvider(umProvider);
                break;
        }
    }

    public void handleClickProducer(long personUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String,String> args = new Hashtable<>();
        args.put(ARG_PRODUCER_UID, String.valueOf(personUid));
        args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        impl.go(SelectSaleProductView.VIEW_NAME, args, context);
        view.finish();
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

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_ASC);
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_DESC);
        presetAL.add(impl.getString(MessageID.sort_by_most_used, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_MOST_USED);


        String[] sortPresets = arrayListToStringArray(presetAL);

        view.updateSpinner(sortPresets);
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

    public void handleChangeSortOrder(long order) {
        order=order+1;

        if(idToOrderInteger.containsKey(order)){
            int sortCode = idToOrderInteger.get(order);
            getAndSetProvider(sortCode);
        }

    }
}
