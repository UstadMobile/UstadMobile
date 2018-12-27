package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.Role;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_HEADER;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_TEXT;
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


/**
 * PersonDetail's Presenter - responsible for the logic of displaying all the details of a person
 * that is being displayed (for viewing). It also handles going to Edit page for this person; showing
 * all assigned classes for this person; setting ui bits for calling/texting parent, showing
 * attendance numbers, marking dropouts, showing profile image, etc.
 *
 */
public class PersonDetailPresenter extends UstadBaseController<PersonDetailView>{

    private UmLiveData<Person> mPerson;

    private List<PersonDetailPresenterField> presenterFields;

    private Map<Long, PersonCustomFieldWithPersonCustomFieldValue> customFieldWithFieldValueMap;

    private long personUid;

    private String attendanceAverage;

    private String oneParentNumber = "";

    private long loggedInPersonUid = 0L;

    private UmProvider<ClazzWithNumStudents> assignedClazzes;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    /**
     * Presenter's constructor where we are getting arguments and setting the personUid
     *
     * @param context Android context
     * @param arguments Arguments from the Activity passed here.
     * @param view  The view that called this Presenter (PersonDetailView->PersonDetailActivity)
     */
    public PersonDetailPresenter(Object context, Hashtable arguments, PersonDetailView view) {
        super(context, arguments, view);

        personUid = Long.parseLong(arguments.get(ARG_PERSON_UID).toString());

        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();
    }

    /**
     * Presenter's overridden onCreate that: Gets the mPerson Live Data and observes it.
     *
     * @param savedState    The state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        PersonDao personDao = repository.getPersonDao();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                repository.getPersonCustomFieldValueDao();
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                repository.getPersonDetailPresenterFieldDao();
        ClazzMemberDao clazzMemberDao =
                repository.getClazzMemberDao();
        PersonCustomFieldDao personCustomFieldDao =
                repository.getPersonCustomFieldDao();

        personDao.findByUidAsync(personUid, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person thisPerson) {
                if(thisPerson != null) {
                    if(thisPerson.getFatherNumber() != null && !thisPerson.getFatherNumber().isEmpty()){
                        oneParentNumber = thisPerson.getFatherNumber();
                    }else if(thisPerson.getMotherNum() != null && !thisPerson.getMotherNum().isEmpty()){
                        oneParentNumber = thisPerson.getMotherNum();
                    }
                    if(thisPerson.getImagePath() != null){
                        view.runOnUiThread(() -> view.updateImageOnView(thisPerson.getImagePath()));
                    }
                }else {
                    //Todo: show this entity has not loaded yet
                }

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        //Get all headers and fields
        personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsViewMode(
                new UmCallback<List<PersonDetailPresenterField>>() {
            @Override
            public void onSuccess(List<PersonDetailPresenterField> fields) {
                presenterFields = fields;

                personCustomFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID,
                        new UmCallback<List<PersonField>>() {
                    @Override
                    public void onSuccess(List<PersonField> customFields) {

                        //Create a list of every custom fields supposed to be and fill them with
                        //blank values that will be used to display empty fields. If those fields
                        //exists, then they will get replaced in the next Dao call.
                        customFieldWithFieldValueMap = new HashMap<>();
                        for(PersonField customField:customFields){

                            //the blank custom field value.
                            PersonCustomFieldValue blankCustomValue = new PersonCustomFieldValue();
                            blankCustomValue.setFieldValue("");

                            //Create a (custom field + custom value) map object
                            PersonCustomFieldWithPersonCustomFieldValue blankCustomMap =
                                    new PersonCustomFieldWithPersonCustomFieldValue();
                            blankCustomMap.setFieldName(customField.getFieldName());
                            blankCustomMap.setLabelMessageId(customField.getLabelMessageId());
                            blankCustomMap.setFieldIcon(customField.getFieldIcon());
                            blankCustomMap.setCustomFieldValue(blankCustomValue);

                            //Set the custom field and the field+value object to the map.
                            customFieldWithFieldValueMap.put(customField.getPersonCustomFieldUid(),
                                    blankCustomMap);
                        }

                        //Get all the custom fields and their values (if applicable)
                        personCustomFieldValueDao.findByPersonUidAsync2(personUid,
                          new UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>>() {
                            @Override
                            public void onSuccess(List<PersonCustomFieldWithPersonCustomFieldValue> result) {
                                //Store the values and fields in this Map

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
                                        exception.printStackTrace();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        checkPermissions();
    }

    public void checkPermissions(){
        ClazzDao clazzDao = repository.getClazzDao();
        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_INSERT,
            new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    view.showFAB(result);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            }));
    }

    /**
     * Generates the all class list with assignation for the person being displayed.
     */
    public void generateAssignedClazzesLiveData(){
        ClazzDao clazzDao = repository.getClazzDao();

        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid);

        setClazzListOnView();
    }

    /**
     * Sets the Class List provider of ClazzNumWithStudents type to the view.
     */
    private void setClazzListOnView(){
        view.setClazzListProvider(assignedClazzes);
    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * @param person The person that needs to be displayed.
     */
    private void handlePersonDataChanged(Person person) {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Locale currentLocale = new Locale(impl.getLocale(context));

        view.clearAllFields();

        if(person == null){
            return;
        }

        for(PersonDetailPresenterField field : presenterFields) {

            String thisValue = "";

            if(field.getFieldType() == FIELD_TYPE_HEADER) {
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_HEADER,
                        field.getHeaderMessageId(), null), field.getHeaderMessageId());
                continue;
            }

            if (field.getFieldUid() == PERSON_FIELD_UID_FULL_NAME){
                if(person.getFirstNames() != null && person.getLastName() != null)
                thisValue = person.getFirstNames() + " " + person.getLastName();
                else
                    thisValue = "";
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
                if(person.getFatherNumber() == null){
                    thisValue = person.getFatherName();
                }else {
                    thisValue = person.getFatherName() + " (" + person.getFatherNumber() + ")";
                }
                //Also tell the view that we need to add call and text buttons for the number

                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(), person.getFatherNumber(), field.getFieldIcon()),
                        thisValue);

            } else if(field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER){
                if(person.getMotherNum() == null){
                    thisValue = person.getMotherName();
                }else {
                    thisValue = person.getMotherName() + " (" + person.getMotherNum() + ")";
                }

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
                if(person.getFatherName() != null ){
                    if(!person.getFatherName().isEmpty()){
                        oneParentNumber = person.getFatherNumber();
                    }
                }
                thisValue = person.getFatherNumber();
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NUMBER) {
                if(person.getMotherNum() != null ){
                    if(!person.getMotherNum().isEmpty()){
                        oneParentNumber = person.getMotherNum();
                    }
                }
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
                thisValue = UMCalendarUtil.getPrettyDateFromLong(
                        person.getDateOfBirth(), currentLocale);
                view.setField(field.getFieldIndex(), new PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }else  {//this is actually a custom field

                PersonCustomFieldWithPersonCustomFieldValue cf =
                        customFieldWithFieldValueMap.get(field.getFieldUid());
                int cfLabelMessageId = 0;
                String cfFieldIcon = "";
                Object cfValue = null;

                if (cf != null){
                    if(cf.getLabelMessageId() != 0){
                        cfLabelMessageId = cf.getLabelMessageId();
                    }
                    if(cf.getFieldIcon() != null){
                        cfFieldIcon = cf.getFieldIcon();
                    }
                    if(cf.getCustomFieldValue() != null){
                        if(cf.getCustomFieldValue().getFieldValue() != null){
                            cfValue = cf.getCustomFieldValue().getFieldValue();
                        }
                    }
                }

                view.setField(
                        field.getFieldIndex(),
                        new PersonDetailViewField(
                                field.getFieldType(),
                                cfLabelMessageId,
                                cfFieldIcon
                        ),
                        cfValue
                );
            }
        }
    }

    /**
     * This method is called upon clicking the edit button for that person.
     * This wil take us to PersonDetailEdit
     */
    public void handleClickEdit() {

        view.finish();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_PERSON_UID, personUid);
        impl.go(PersonEditView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Handles call parent button - calls the method that calls the parent (open platform native
     * call UI)
     */
    public void handleClickCallParent(){
        if(!oneParentNumber.isEmpty()) {
            handleClickCall(oneParentNumber);
        }
    }

    /**
     * Handles text parent button - calls the method that texts the parent (opens platform native
     * text UI)
     */
    public void handleClickTextParent(){
        if(!oneParentNumber.isEmpty()) {
            handleClickText(oneParentNumber);
        }
    }

    /**
     * Handles what happens when Enroll in Class is clicked at the top common big buttons
     * - opens the View that shows the list of classes with their enrollment status ie:
     *      PersonDetailEnrollClazz
     */
    public void handleClickEnrollInClass(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_PERSON_UID, personUid);

        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context);
    }

    /**
     * Handler to what happens when call button pressed on an entry (usually to call a person)
     *
     * @param number The phone number
     */
    public void handleClickCall(String number){
        view.handleClickCall(number);
    }

    /**
     * Handler to what happens when text / sms button pressed on an entry (usually to text a
     * person / parent)
     *
     * @param number The phone number
     */
    public void handleClickText(String number){
        view.handleClickText(number);
    }

    /**
     * Overriding. Doesn't do anything.
     */
    @Override
    public void setUIStrings() {
    }
}
