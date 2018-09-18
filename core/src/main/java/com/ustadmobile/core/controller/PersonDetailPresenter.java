package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_TEXT;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_ADDRESS;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_ATTENDANCE;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_BIRTHDAY;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_CLASSES;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FULL_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_LAST_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NUMBER;


public class PersonDetailPresenter extends UstadBaseController<PersonDetailView>{

    private UmLiveData<Person> mPerson;

    private List<PersonDetailPresenterField> presenterFields;

    private Map<Long, PersonCustomFieldWithPersonCustomFieldValue> customFieldWithFieldValueMap;

    private long personUid;

    private String attendanceAverage;

    /**
     * Presenter's constructor where we are getting arguments and setting the personUid
     *
     * @param context Android context
     * @param arguments Arguments from the Activity passed here.
     * @param view  The view that called this Presenter (PersonDetailView->PersonDetailActivity)
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
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                UmAppDatabase.getInstance(context).getPersonDetailPresenterFieldDao();
        ClazzMemberDao clazzMemberDao = 
                UmAppDatabase.getInstance(context).getClazzMemberDao();

        //Get all headers and fields
        personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsViewMode(
                new UmCallback<List<PersonDetailPresenterField>>() {
            @Override
            public void onSuccess(List<PersonDetailPresenterField> fields) {
                presenterFields = fields;

                //Get all the custom fields and their values (if applicable)
                personCustomFieldValueDao.findByPersonUidAsync2(personUid,
                        new UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>>() {
                    @Override
                    public void onSuccess(List<PersonCustomFieldWithPersonCustomFieldValue> result) {
                        //Store the values and fields in this Map
                        customFieldWithFieldValueMap = new HashMap<>();
                        for( PersonCustomFieldWithPersonCustomFieldValue fieldWithFieldValue: result){
                            customFieldWithFieldValueMap.put(
                                    fieldWithFieldValue.getPersonCustomFieldUid(), fieldWithFieldValue);
                        }

                        //Get the attendance average for this person.
                        clazzMemberDao.getAverageAttendancePercentageByPersonUidAsync(personUid,
                                new UmCallback<Float>() {
                            @Override
                            public void onSuccess(Float result) {
                                if (result == null){
                                    attendanceAverage = "N/A";
                                }else {
                                    attendanceAverage = result * 100 + "%";
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

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    public void setItemOnView(){

    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * @param person The person that needs to be displayed.
     */
    public void handlePersonDataChanged(Person person) {
        for(PersonDetailPresenterField field : presenterFields) {

            String thisValue = "";

            if(field.getFieldType() == FIELD_TYPE_HEADER) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_HEADER,
                        field.getHeaderMessageId(), null), field.getHeaderMessageId());
                continue;
            }

            if (field.getFieldUid() == PERSON_FIELD_UID_FULL_NAME){
                thisValue = person.getFirstNames() + " " + person.getLastName();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            }else if (field.getFieldUid() == PERSON_FIELD_UID_FIRST_NAMES) {
                thisValue =  person.getFirstNames();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_LAST_NAME) {
                thisValue = person.getLastName();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_ATTENDANCE) {
                if(attendanceAverage != null) {
                    thisValue = attendanceAverage;
                }
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_CLASSES) {
                thisValue = "Class Name ...";
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER) {
                thisValue = person.getFatherName() + " (" + person.getFatherNumber() +")";
                //Also tell the view that we need to add call and text buttons for the number

                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(), person.getFatherNumber(), field.getFieldIcon()),
                        thisValue);

            } else if(field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER){
                thisValue = person.getMotherName() + " (" + person.getMotherNum() + ")";

                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(), person.getMotherNum(), field.getFieldIcon()),
                        thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NAME) {
                thisValue = person.getFatherName();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME) {
                thisValue = person.getMotherName();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NUMBER) {
                thisValue = person.getFatherNumber();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NUMBER) {
                thisValue = person.getMotherNum();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }

            else if (field.getFieldUid() == PERSON_FIELD_UID_ADDRESS) {
                thisValue = person.getAddress();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if(field.getFieldUid() == PERSON_FIELD_UID_BIRTHDAY){
                thisValue = UMCalendarUtil.getPrettyDateFromLong(person.getDateOfBirth());
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }else  {//this is actually a custom field
                view.setField(
                        field.getFieldIndex(),
                        new PersonDetailViewField(
                                field.getFieldType(),
                                customFieldWithFieldValueMap.get(field.getFieldUid()).getLabelMessageId(),
                                customFieldWithFieldValueMap.get(field.getFieldUid()).getFieldIcon()
                        ),
                        customFieldWithFieldValueMap.get(field.getFieldUid())
                                .getCustomFieldValue().getFieldValue()
                );
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
        impl.go(PersonEditView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Handler to what happens when call button pressed on an entry (usually to call a person)
     *
     * @param number The phone number
     */
    public void handleClickCall(String number){
        System.out.println("Call this number: " + number);
    }

    /**
     * Handler to what happens when text / sms button pressed on an entry (usually to text a
     * person / parent)
     *
     * @param number The phone number
     */
    public void handleClickText(String number){
        System.out.println("Text this number: " + number);
    }

    @Override
    public void setUIStrings() {
    }
}
