package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.CustomFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AddCustomFieldOptionDialogView;
import com.ustadmobile.core.view.CustomFieldDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;

import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao;
import com.ustadmobile.lib.db.entities.Person;

import static com.ustadmobile.core.controller.CustomFieldListPresenter.ENTITY_TYPE_CLASS;
import static com.ustadmobile.core.controller.CustomFieldListPresenter.ENTITY_TYPE_PERSON;
import static com.ustadmobile.core.view.AddCustomFieldOptionDialogView.ARG_CUSTOM_FIELD_VALUE_OPTION_UID;
import static com.ustadmobile.core.view.CustomFieldDetailView.ARG_CUSTOM_FIELD_UID;

/**
 * Presenter for CustomFieldDetail view
 **/
public class CustomFieldDetailPresenter extends UstadBaseController<CustomFieldDetailView> {

    private UmProvider<CustomFieldValueOption> optionProvider;
    UmAppDatabase repository;

    private CustomFieldDao customFieldDao;
    private CustomFieldValueOptionDao optionDao;

    private CustomField currentField;
    private CustomField updatedField;
    private long customFieldUid = 0;

    private String[] fieldTypePresets;
    private String[] entityTypePresets;

    public static final int FIELD_TYPE_TEXT = 0 ;
    public static final int FIELD_TYPE_DROPDOWN = 1;


    public CustomFieldDetailPresenter(Object context, Hashtable arguments, CustomFieldDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        optionDao = repository.getCustomFieldValueOptionDao();
        customFieldDao = repository.getCustomFieldDao();

        if(arguments.containsKey(ARG_CUSTOM_FIELD_UID)){
            customFieldUid = (long) arguments.get(ARG_CUSTOM_FIELD_UID);
        }


    }

    public void initFromCustomField(long uid){
        UmLiveData<CustomField> currentFieldLive = customFieldDao.findByUidLive(uid);
        currentFieldLive.observe(CustomFieldDetailPresenter.this,
                CustomFieldDetailPresenter.this::handleCustomFieldChanged);
        customFieldDao.findByUidAsync(uid, new UmCallback<CustomField>() {
            @Override
            public void onSuccess(CustomField result) {
                updatedField = result;
                view.setCustomFieldOnView(updatedField);

                getSetOptionProvider();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        fieldTypePresets = new String[]{
            impl.getString(MessageID.text, context), impl.getString(MessageID.dropdown, context)
        };
        view.setDropdownPresetsOnView(fieldTypePresets);

        entityTypePresets = new String[] {
                impl.getString(MessageID.clazz, context), impl.getString(MessageID.people, context)
        };
        view.setEntityTypePresetsOnView(entityTypePresets);


        if(customFieldUid == 0){
            currentField = new CustomField();
            currentField.setCustomFieldActive(false);
            customFieldDao.insertAsync(currentField, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromCustomField(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else{
            initFromCustomField(customFieldUid);
        }

    }

    public void handleFieldNameChanged(String title){
        updatedField.setCustomFieldName(title);
    }

    public void handleFieldNameAltChanged(String title){
        updatedField.setCustomFieldNameAlt(title);
    }

    public void handleFieldTypeChanged(int type){
        int fieldType = 0;
        switch (type){
            case FIELD_TYPE_TEXT:
                fieldType = CustomField.FIELD_TYPE_TEXT;
                view.showOptions(false);
                break;
            case FIELD_TYPE_DROPDOWN:
                fieldType = CustomField.FIELD_TYPE_DROPDOWN;
                view.showOptions(true);
                break;
            default:
                view.showOptions(false);
                break;
        }

        if(updatedField != null){
            updatedField.setCustomFieldType(fieldType);
        }
    }

    public void handleEntityEntityChanged(int type){
        switch (type){
            case ENTITY_TYPE_CLASS:
                updatedField.setCustomFieldEntityType(Clazz.TABLE_ID);
                break;
            case ENTITY_TYPE_PERSON:
                updatedField.setCustomFieldEntityType(Person.TABLE_ID);
                break;
            default:break;
        }
    }

    public void handleDefaultValueChanged(String defaultString){
        updatedField.setCustomFieldDefaultValue(defaultString);
    }
    public void handleClickAddOption() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(AddCustomFieldOptionDialogView.VIEW_NAME, args, context);
    }

    public void handleClickOptionEdit(long customFieldOptionUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CUSTOM_FIELD_VALUE_OPTION_UID, customFieldOptionUid);
        impl.go(AddCustomFieldOptionDialogView.VIEW_NAME, args, context);
    }

    public void handleClickOptionDelete(long customFieldOptionUid){
        optionDao.deleteOption(customFieldOptionUid, null);
    }

    private void handleCustomFieldChanged(CustomField changedCustomField){
        //set the og person value
        if(currentField == null)
            currentField = changedCustomField;

        if(updatedField == null || !updatedField.equals(changedCustomField)) {

            if(changedCustomField != null) {
                //Update the currently editing custom field object
                updatedField = changedCustomField;

                view.setCustomFieldOnView(changedCustomField);
            }
        }
    }

    private void getSetOptionProvider(){
        optionProvider = optionDao.findAllOptionsForField(updatedField.getCustomFieldUid());
        view.setListProvider(optionProvider);
    }

    public void handleClickDone() {
        updatedField.setCustomFieldActive(true);
        customFieldDao.updateAsync(updatedField, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }
}
