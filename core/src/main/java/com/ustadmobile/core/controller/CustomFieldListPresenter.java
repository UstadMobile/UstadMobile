package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.CustomFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.CustomFieldDetailView;
import com.ustadmobile.core.view.CustomFieldListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

import static com.ustadmobile.core.view.CustomFieldDetailView.ARG_CUSTOM_FIELD_UID;

/**
 * Presenter for CustomFieldList view
 **/
public class CustomFieldListPresenter extends UstadBaseController<CustomFieldListView> {

    private UmProvider<CustomField> umProvider;
    UmAppDatabase repository;

    private CustomFieldDao customFieldDao;
    private String[] entityTypePresets;

    public static final int ENTITY_TYPE_CLASS = 0 ;
    public static final int ENTITY_TYPE_PERSON = 1;

    public CustomFieldListPresenter(Object context, Hashtable arguments, CustomFieldListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        customFieldDao = repository.getCustomFieldDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        entityTypePresets = new String[]{
          impl.getString(MessageID.clazz, context), impl.getString(MessageID.person, context)
        };
        view.setEntityTypePresets(entityTypePresets);

        //Get provider
        generateProvider(ENTITY_TYPE_CLASS);

    }

    public void handleEntityTypeChange(int type){

        generateProvider(type);
    }

    private void generateProvider(int type){
        int tableId = 0;
        switch (type){
            case ENTITY_TYPE_CLASS:
                tableId = Clazz.TABLE_ID;
                break;
            case ENTITY_TYPE_PERSON:
                tableId = Person.TABLE_ID;
                break;
            default:break;
        }
        umProvider = customFieldDao.findAllCustomFieldsProviderForEntity(tableId);
        view.setListProvider(umProvider);

    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(CustomFieldDetailView.VIEW_NAME, args, context);
    }

    public void handleClickEditCustomField(long customFieldUid){
        //Go to custom field detail
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CUSTOM_FIELD_UID, customFieldUid);
        impl.go(CustomFieldDetailView.VIEW_NAME, args, context);
    }

    public void handleClickDeleteCustomField(long customFieldUid){
        customFieldDao.deleteCustomField(customFieldUid, null);
    }

}
