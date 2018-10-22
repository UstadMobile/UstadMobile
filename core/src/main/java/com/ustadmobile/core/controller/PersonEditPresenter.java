package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
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
 * PersonEditPresenter : This is responsible for generating the Edit data along with its Custom
 * Fields. It is also responsible for updating the data and checking for changes and handling
 * Done with Save or Discard.
 *
 */
public class PersonEditPresenter extends UstadBaseController<PersonEditView> {


    private UmLiveData<Person> personLiveData;

    //Headers and Fields
    private List<PersonDetailPresenterField> headersAndFields;

    private long personUid;

    private long assignToThisClazzUid;

    private Person mUpdatedPerson;

    //OG person before Done/Save/Discard clicked.
    private Person mOriginalValuePerson;

    private UmProvider<ClazzWithNumStudents> assignedClazzes;

    //The custom fields' values
    private Map<Long, PersonCustomFieldWithPersonCustomFieldValue> customFieldWithFieldValueMap;

    PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();

    private String newPersonString = "";

    List<PersonCustomFieldValue> customFieldsToUpdate;

    PersonCustomFieldValueDao personCustomFieldValueDao =
            UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();

    /**
     * Presenter's constructor where we are getting arguments and setting the newly/editable
     * personUid
     *
     * @param context Android context
     * @param arguments Arguments from the Activity passed here.
     * @param view  The view that called this presenter (PersonEditView->PersonEditActivity)
     */
    public PersonEditPresenter(Object context, Hashtable arguments, PersonEditView view) {
        super(context, arguments, view);

        if (arguments.containsKey(ARG_PERSON_UID)) {
            personUid = Long.parseLong(arguments.get(ARG_PERSON_UID).toString());
        }

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            assignToThisClazzUid = Long.parseLong(arguments.get(ARG_CLAZZ_UID).toString());
        }

        if(arguments.containsKey(ARG_NEW_PERSON)){
            newPersonString = arguments.get(ARG_NEW_PERSON).toString();
        }

        customFieldsToUpdate = new ArrayList<>();

    }

    /**
     * Presenter's Overridden onCreate that: Gets the mPerson LiveData and observe it.
     * @param savedState
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                UmAppDatabase.getInstance(context).getPersonDetailPresenterFieldDao();


        if(newPersonString.equals("true")){
            view.updateToolbarTitle(impl.getString(MessageID.new_person, context));
        }

        //Get all the currently set headers and fields:
        //personDetailPresenterFieldDao.findAllPersonDetailPresenterFields(
        personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsEditMode(
                new UmCallback<List<PersonDetailPresenterField>>() {
            @Override
            public void onSuccess(List<PersonDetailPresenterField> result) {

                headersAndFields = result;

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

                            //Get person live data and observe
                            personLiveData = personDao.findByUidLive(personUid);
                            //Observe the live data
                            personLiveData.observe(PersonEditPresenter.this,
                                    PersonEditPresenter.this::handlePersonValueChanged);
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

    public void updatePersonPic(String picPath){
        personDao.findByUidAsync(personUid, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person personWithPic) {
                personWithPic.setImagePath(picPath);
                personDao.updateAsync(personWithPic, new UmCallback<Integer>(){

                    @Override
                    public void onSuccess(Integer result) {
                        System.out.println("Success updating person with Pic..");
                        //Update pic on the view
                        //view.updateImageOnView(picPath);

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    /**
     * Generates live data for Clazz list to be assigned to the current Person being edited.
     */
    public void generateAssignedClazzesLiveData(){
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();
        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid);
        view.setClazzListProvider(assignedClazzes);
    }

    @Override
    public void setUIStrings() {

    }

    public void setFieldsOnView(Person thisPerson, List<PersonDetailPresenterField> allFields,
                                String attendanceAverage, PersonEditView thisView,
                                Map<Long, PersonCustomFieldWithPersonCustomFieldValue> valueMap ){


        if(thisPerson.getImagePath() != null){
            view.updateImageOnView(thisPerson.getImagePath());
        }

        for(PersonDetailPresenterField field : allFields) {

            String thisValue = "";

            if(field.getFieldType() == FIELD_TYPE_HEADER) {
                thisView.setField(field.getFieldIndex(), field.getFieldUid(), new PersonDetailViewField(FIELD_TYPE_HEADER,
                        field.getHeaderMessageId(), null), field.getHeaderMessageId());
                continue;
            }

            if (field.getFieldUid() == PERSON_FIELD_UID_FULL_NAME){
                thisValue = thisPerson.getFirstNames() + " " + thisPerson.getLastName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            }else if (field.getFieldUid() == PERSON_FIELD_UID_FIRST_NAMES) {
                thisValue =  thisPerson.getFirstNames();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_LAST_NAME) {
                thisValue = thisPerson.getLastName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_ATTENDANCE) {
                if(attendanceAverage != null) {
                    thisValue = attendanceAverage;
                }
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_CLASSES) {
                thisValue = "Class Name ...";
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER) {
                thisValue = thisPerson.getFatherName() + " (" + thisPerson.getFatherNumber() +")";
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if(field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER){
                thisValue = thisPerson.getMotherName() + " (" + thisPerson.getMotherNum() + ")";
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NAME) {
                thisValue = thisPerson.getFatherName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME) {
                thisValue = thisPerson.getMotherName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NUMBER) {
                thisValue = thisPerson.getFatherNumber();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NUMBER) {
                thisValue = thisPerson.getMotherNum();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }

            else if (field.getFieldUid() == PERSON_FIELD_UID_ADDRESS) {
                thisValue = thisPerson.getAddress();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if(field.getFieldUid() == PERSON_FIELD_UID_BIRTHDAY){
                thisValue = UMCalendarUtil.getPrettyDateFromLong(thisPerson.getDateOfBirth());
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }else  {//this is actually a custom field
                int messageLabel = 0;
                String iconName = null;
                String fieldValue = null;
                if(valueMap.get(field.getFieldUid()) != null){
                    if(valueMap.get(field.getFieldUid()).getLabelMessageId() != 0){
                        messageLabel = valueMap.get(field.getFieldUid()).getLabelMessageId();
                    }
                    if(valueMap.get(field.getFieldUid()).getFieldIcon() != null){
                        iconName = valueMap.get(field.getFieldUid()).getFieldIcon();
                    }
                    if(valueMap.get(field.getFieldUid()).getCustomFieldValue().getFieldValue() != null){
                        fieldValue = valueMap.get(field.getFieldUid())
                                .getCustomFieldValue().getFieldValue();
                    }
                }
                thisView.setField(
                        field.getFieldIndex(),
                        field.getFieldUid(),
                        new PersonDetailViewField(
                                field.getFieldType(),
                                messageLabel,
                                iconName
                        ), fieldValue

                );
            }

        }

    }

    /**
     * Updates fields of Person w.r.t field Id given and the value. This method does NOT persist
     * the data.
     *
     * @param updateThisPerson the Person object to update values for.
     * @param fieldcode The field Uid that needs to get updated
     * @param value The value to update the Person's field.
     * @return  The updated Person with the updated field.
     */
    public Person updateSansPersistPersonField(Person updateThisPerson,
                                               long fieldcode, Object value){

        //Update Core fields
        if (fieldcode == PERSON_FIELD_UID_FIRST_NAMES) {
            updateThisPerson.setFirstNames((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_LAST_NAME) {
            updateThisPerson.setLastName((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NAME) {
            updateThisPerson.setFatherName((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NUMBER) {
            updateThisPerson.setFatherNumber((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NAME) {
            updateThisPerson.setMotherName((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NUMBER) {
            updateThisPerson.setMotherNum((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_BIRTHDAY) {
            //TODO: Change and work this
            //updateThisPerson.setDateOfBirth((Long) value);

        } else if (fieldcode == PERSON_FIELD_UID_ADDRESS) {
            updateThisPerson.setAddress((String) value);

        } else {
            System.out.println("Custom field edit.");
            //This is actually a custom field.

            personCustomFieldValueDao.findCustomFieldByFieldAndPersonAsync(fieldcode,
                    updateThisPerson.getPersonUid(), new UmCallback<PersonCustomFieldValue>() {
                @Override
                public void onSuccess(PersonCustomFieldValue result) {
                    if(result != null) {
                        result.setFieldValue(value.toString());
                        customFieldsToUpdate.add(result);
                    }

                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }

        //Update Custom Fields:
        //TODO


        return updateThisPerson;

    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * The Live Data handler calls this method when the data (via Live data) is updated.
     *
     * @param person The person that needs to be displayed.
     */
    public void handlePersonValueChanged(Person person) {
        //set the og person value
        if(mOriginalValuePerson == null)
            mOriginalValuePerson = person;

        if(mUpdatedPerson == null || !mUpdatedPerson.equals(person)) {
            //set fields on the view as they change and arrive.
            setFieldsOnView(person, headersAndFields, null, view,
                    customFieldWithFieldValueMap);

            mUpdatedPerson = person;
        }

    }

    /**
     * Handles every field Edit (focus changed).
     *
     * @param fieldCode The field code that needs editing
     * @param value The new value of the field from the view
     */
    public void handleFieldEdited(long fieldCode, Object value) {


        mUpdatedPerson = updateSansPersistPersonField(mUpdatedPerson, fieldCode, value);
        /*
        //update the DAO with mUpdatedPerson
        //TODO: Check if we need to update or insert
        personDao.insertAsync(mUpdatedPerson, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                mUpdatedPerson.setPersonUid(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
        */

    }

    /**
     * Handle discarding the edits done so far when leaving the activity / clicking discard.
     */
    public void handleClickDiscardChanges(){
        //TODO:  Make use of this method?
        //Update dao with mOriginalValuePerson
        personDao.insert(mOriginalValuePerson);

    }

    /**
     * Click handler when Add new Class clicked on Classes section
     */
    public void handleClickAddNewClazz(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_PERSON_UID, personUid);

        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context);
    }

    /**
     * Done click handler on the Edit / Enrollment page: Clicking done will persist and save it and
     * end the activity.
     *
     */
    public void handleClickDone(){
        mUpdatedPerson.setActive(true);
        personDao.updateAsync(mUpdatedPerson, new UmCallback<Integer>(){

            @Override
            public void onSuccess(Integer result) {

                //Update the custom fields
                personCustomFieldValueDao.updateListAsync(customFieldsToUpdate,
                        new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        //Close the activity.
                        view.finish();
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

}
