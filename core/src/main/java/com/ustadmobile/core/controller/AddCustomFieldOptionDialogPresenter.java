package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;

import java.util.Hashtable;

import static com.ustadmobile.core.view.AddCustomFieldOptionDialogView.ARG_CUSTOM_FIELD_VALUE_OPTION_UID;
import static com.ustadmobile.core.view.CustomFieldDetailView.ARG_CUSTOM_FIELD_UID;


/**
 * Presenter for AddCustomFieldOptionDialog view
 **/
public class AddCustomFieldOptionDialogPresenter extends UstadBaseController<AddCustomFieldOptionDialogView> {

    UmAppDatabase repository;

    private long customfieldUid;
    String optionValue;
    private long optionUid;
    private CustomFieldValueOption currentOption;
    private CustomFieldValueOptionDao optionDao;

    public AddCustomFieldOptionDialogPresenter(Object context, Hashtable arguments, AddCustomFieldOptionDialogView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        optionDao = repository.getCustomFieldValueOptionDao();

        if(getArguments().containsKey(ARG_CUSTOM_FIELD_UID)){
            customfieldUid = Long.parseLong(getArguments().get(ARG_CUSTOM_FIELD_UID).toString());
        }
        if(getArguments().containsKey(ARG_CUSTOM_FIELD_VALUE_OPTION_UID))
            optionUid = Long.parseLong(getArguments().get(ARG_CUSTOM_FIELD_VALUE_OPTION_UID).toString());

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


        if(optionUid != 0){
            optionDao.findByUidAsync(optionUid, new UmCallback<CustomFieldValueOption>() {
                @Override
                public void onSuccess(CustomFieldValueOption result) {
                    initFromOption(result);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }else{
            CustomFieldValueOption option = new CustomFieldValueOption();
            optionDao.insertAsync(option, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    if(result != 0){
                        option.setCustomFieldValueOptionUid(result);
                        initFromOption(option);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }

    }

    private void initFromOption(CustomFieldValueOption option){
        if(option != null){
            currentOption = option;
            view.setOptionValue(option.getCustomFieldValueOptionName());
        }
    }
    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public void handleClickOk(){

        currentOption.setCustomFieldValueOptionName(optionValue);
        currentOption.setCustomFieldValueOptionFieldUid(customfieldUid);
        currentOption.setCustomFieldValueOptionActive(true);

        optionDao.updateAsync(currentOption, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }


}
