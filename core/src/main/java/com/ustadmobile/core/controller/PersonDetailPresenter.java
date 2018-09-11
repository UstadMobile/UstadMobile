package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.PersonDetailEditView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValueWithPersonCustomField;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.controller.PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.FIELD_TYPE_HEADER;

public class PersonDetailPresenter extends UstadBaseController<PersonDetailView>{

    /**
     * Class representing a person's detail in the View. This is the class that the View will be
     * made aware of. Part of Person and PersonDetailCustomField
     * Used in PersonDetail and PersonDetailEdit.
     *
     * We assign every field an id, its type, label and options.
     *
     */
    public class PersonDetailViewField {


        public PersonDetailViewField(int fieldType, int messageLabel, String iconName) {
            this.fieldType = fieldType;
            this.messageLabel = messageLabel;
            this.iconName = iconName;
        }

        public PersonDetailViewField(int fieldType, int messageLabel, String iconName,
                                     List<Map.Entry<Object, String>> options) {
            this.fieldType = fieldType;
            this.messageLabel = messageLabel;
            this.iconName = iconName;
            this.fieldOptions = options;
        }

        public static final int FIELD_TYPE_TEXT = 3;

        public static final int FIELD_TYPE_DROPDOWN = 4;

        public static final int FIELD_TYPE_PHONE_NUMBER = 5;

        public static final int FIELD_TYPE_DATE = 6;


        private int fieldType;

        private int messageLabel;

        private String iconName;

        private List<Map.Entry<Object, String>> fieldOptions;

        public int getFieldType() {
            return fieldType;
        }

        public void setFieldType(int fieldType) {
            this.fieldType = fieldType;
        }

        public int getMessageLabel() {
            return messageLabel;
        }

        public void setMessageLabel(int messageLabel) {
            this.messageLabel = messageLabel;
        }

        public String getIconName() {
            return iconName;
        }

        public void setIconName(String iconName) {
            this.iconName = iconName;
        }

        public List<Map.Entry<Object, String>> getFieldOptions() {
            return fieldOptions;
        }

        public void setFieldOptions(List<Map.Entry<Object, String>> fieldOptions) {
            this.fieldOptions = fieldOptions;
        }
    }

    private UmLiveData<Person> mPerson;

    private List<PersonDetailPresenterField> presenterFields;

    private Map<Long, PersonCustomFieldValueWithPersonCustomField> customFieldValueMap;

    private long personUid;

    /**
     * Presenter's constructor where we are getting arguments and setting the personUid
     *
     * @param context Android context
     * @param arguments Arguments to the Activity passed here.
     * @param view  The view that called this Presenter (PersonDetailActivity)
     */
    public PersonDetailPresenter(Object context, Hashtable arguments, PersonDetailView view) {
        super(context, arguments, view);

        personUid = Long.parseLong(arguments.get(PersonDetailView.ARG_PERSON_UID).toString());
    }

    /**
     * Presenter's overridden onCreate that: Gets the mPerson Live Data and observes it.
     *
     * @param savedState    The state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();
        PersonDetailPresenterFieldDao personDetailPresenterField =
                UmAppDatabase.getInstance(context).getPersonDetailPresenterFieldDao();

        //Get all fields
        personDetailPresenterField.findAllPersonDetailPresenterFields(new UmCallback<List<PersonDetailPresenterField>>() {
            @Override
            public void onSuccess(List<PersonDetailPresenterField> fields) {
                presenterFields = fields;

                //Get all values
                personCustomFieldValueDao.findByPersonUidAsync(personUid,
                                new UmCallback<List<PersonCustomFieldValueWithPersonCustomField>>() {
                    @Override
                    public void onSuccess(List<PersonCustomFieldValueWithPersonCustomField> result) {
                        customFieldValueMap = new HashMap<>();
                        for( PersonCustomFieldValueWithPersonCustomField fieldValue: result){
                            customFieldValueMap.put(
                                    fieldValue.getPersonCustomFieldValuePersonCustomFieldUid(),
                                    fieldValue);
                        }

                        //Get person live data and observe
                        mPerson = personDao.findByUidLive(personUid);
                        mPerson.observe(PersonDetailPresenter.this,
                                PersonDetailPresenter.this::handlePersonDataChanged);
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * @param person The person that needs to be displayed.
     */
    public void handlePersonDataChanged(Person person) {
        for(PersonDetailPresenterField field : presenterFields) {
            if(field.getFieldType() == FIELD_TYPE_HEADER) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_HEADER,
                        field.getHeaderMessageId(), null), field.getHeaderMessageId());
                continue;
            }

            if (field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        MessageID.first_names, "ic_account"), person.getFirstNames());

            } else if (field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_LAST_NAME) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        MessageID.last_name, "ic_account"), person.getLastName());

            } else if (field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_ATTENDANCE) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        MessageID.attendance, "ic_lens_black_24dp"), "Attended ...");

            } else if (field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_CLASSES) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        MessageID.classes, "ic_people_black_24dp"), "Class Name ...");

            } else if (field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                                MessageID.field_father_name, "ic_person_black_24dp")
                        , "Father Name (Number)");

            } else if(field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER){
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                                MessageID.mother, "ic_person_black_24dp")
                        , "Mother Name (Number)");
            } else if(field.getFieldUid() == PersonDetailPresenterField.PERSON_FIELD_UID_BIRTHDAY){

                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                                MessageID.birthday, "ic_perm_contact_calendar_black_24dp")
                        , "Birthday2");
            }else  {//this is actually a custom field

                view.setField(field.getFieldIndex(), new PersonDetailViewField(field.getFieldType(),
                                customFieldValueMap.get(field.getFieldUid()).getCustomField().getLabelMessageId(),
                                customFieldValueMap.get(field.getFieldUid()).getCustomField().getFieldIcon()),
                        customFieldValueMap.get(field.getFieldUid()).getFieldValue());
            }
        }
    }

    /**
     * This method is called upon clicking the edit button for that person.
     * This wil take us to PersonDetailEdit
     */
    public void handleClickEdit() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(PersonDetailView.ARG_PERSON_UID, personUid);
        impl.go(PersonDetailEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }
}
