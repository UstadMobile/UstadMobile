package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.db.dao.CustomFieldValueDao
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.db.dao.FeedEntryDao
import com.ustadmobile.core.db.dao.PersonCustomFieldDao
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.PersonDetailEnrollClazzView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailViewField
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValue
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PersonField
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScheduledCheck

import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.CUSTOM_FIELD_MIN_UID
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ADDRESS
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ATTENDANCE
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_CLASSES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FULL_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_HEADER

/**
 * PersonEditPresenter : This is responsible for generating the Edit data along with its Custom
 * Fields. It is also responsible for updating the data and checking for changes and handling
 * Done with Save or Discard.
 *
 */
class PersonEditPresenter
/**
 * Presenter's constructor where we are getting arguments and setting the newly/editable
 * personUid
 *
 * @param context Android context
 * @param arguments Arguments from the Activity passed here.
 * @param view  The view that called this presenter (PersonEditView->PersonEditActivity)
 */
(context: Any, arguments: Map<String, String>?, view: PersonEditView,
        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    :UstadBaseController<PersonEditView>(context, arguments!!, view) {


    private var personLiveData: UmLiveData<Person>? = null

    //Headers and Fields
    private var headersAndFields: List<PersonDetailPresenterField>? = null

    var personUid: Long = 0

    private var mUpdatedPerson: Person? = null

    //OG person before Done/Save/Discard clicked.
    private var mOriginalValuePerson: Person? = null

    private var assignedClazzes: UmProvider<ClazzWithNumStudents>? = null

    //The custom fields' values
    private val customFieldWithFieldValueMap: Map<Long, PersonCustomFieldWithPersonCustomFieldValue>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    private val personDao = repository.personDao

    private var newPersonString = ""

    private val customFieldsToUpdate: MutableList<PersonCustomFieldValue>

    private val personCustomFieldValueDao = repository.personCustomFieldValueDao

    private var loggedInPersonUid: Long? = 0L

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    private var customFieldDao: CustomFieldDao? = null
    private var customFieldValueDao: CustomFieldValueDao? = null
    private var optionDao: CustomFieldValueOptionDao? = null

    private val customFieldDropDownOptions: HashMap<Long, List<String>>

    init {

        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            personUid = (arguments!!.get(ARG_PERSON_UID)!!.toString()).toLong()
        }

        if (arguments!!.containsKey(ARG_NEW_PERSON)) {
            newPersonString = arguments!!.get(ARG_NEW_PERSON)!!.toString()
        }

        customFieldsToUpdate = ArrayList()

        viewIdToCustomFieldUid = HashMap()

        customFieldDropDownOptions = HashMap()

    }

    fun addToMap(viewId: Int, fieldId: Long) {
        viewIdToCustomFieldUid[viewId] = fieldId
    }

    /**
     * Getting custom fields (new way)
     */
    private fun getAllPersonCustomFields() {
        //0. Clear all added custom fields on view.
        view.runOnUiThread({ view.clearAllCustomFields() })

        //1. Get all custom fields
        customFieldDao!!.findAllCustomFieldsProviderForEntityAsync(Person.TABLE_ID,
                object : UmCallback<List<CustomField>> {
                    override fun onSuccess(result: List<CustomField>?) {
                        for (c in result!!) {
                            //Get value as well
                            customFieldValueDao!!.findValueByCustomFieldUidAndEntityUid(
                                    c.customFieldUid, personUid,
                                    object : UmCallback<CustomFieldValue> {
                                        override fun onSuccess(result: CustomFieldValue?) {
                                            var valueString: String? = ""
                                            var valueSelection = 0

                                            if (c.customFieldType == CustomField.FIELD_TYPE_TEXT) {

                                                if (result != null) {
                                                    valueString = result.customFieldValueValue
                                                }
                                                val finalValueString = arrayOf<String>(valueString)
                                                view.runOnUiThread({

                                                    view.addCustomFieldText(c, finalValueString[0])
                                                    //view.addComponent(finalValueString[0], c.getCustomFieldName());
                                                })

                                            } else if (c.customFieldType == CustomField.FIELD_TYPE_DROPDOWN) {
                                                if (result != null) {
                                                    try {
                                                        valueSelection = Integer.valueOf(result.customFieldValueValue!!)
                                                    } catch (nfe: NumberFormatException) {
                                                        valueSelection = 0
                                                    }

                                                }
                                                val finalValueSelection = valueSelection
                                                optionDao!!.findAllOptionsForFieldAsync(c.customFieldUid,
                                                        object : UmCallback<List<CustomFieldValueOption>> {
                                                            override fun onSuccess(result: List<CustomFieldValueOption>?) {
                                                                val options = ArrayList<String>()

                                                                for (o in result!!) {
                                                                    options.add(o.customFieldValueOptionName)
                                                                }
                                                                //Get value
                                                                var valueString = "-"
                                                                if (finalValueSelection > 0) {
                                                                    valueString = options[finalValueSelection]
                                                                }
                                                                val finalValueString = valueString

                                                                customFieldDropDownOptions[c.customFieldUid] = options
                                                                view.runOnUiThread({
                                                                    //view.addComponent(finalValueString, c.getCustomFieldName());
                                                                    val a = arrayOfNulls<String>(options.size)
                                                                    options.toTypedArray()
                                                                    view.addCustomFieldDropdown(c, a, finalValueSelection)
                                                                    //view.addCustomFieldText(c, finalValueString);

                                                                })
                                                            }

                                                            override fun onFailure(exception: Throwable?) {
                                                                print(exception!!.message)
                                                            }
                                                        })
                                            }
                                        }

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }

    /**
     * Presenter's Overridden onCreate that: Gets the mPerson LiveData and observe it.
     * @param savedState    The saved state
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val personCustomFieldValueDao = repository.personCustomFieldValueDao
        val personDetailPresenterFieldDao = repository.personDetailPresenterFieldDao
        val personCustomFieldDao = repository.personCustomFieldDao

        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        optionDao = repository.customFieldValueOptionDao

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        if (newPersonString == "true") {
            view.updateToolbarTitle(impl.getString(MessageID.new_person, context))
        }

        getAllPersonCustomFields()

        //Get all the currently set headers and fields:
        personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsEditMode(
                object : UmCallback<List<PersonDetailPresenterField>> {
                    override fun onSuccess(result: List<PersonDetailPresenterField>?) {

                        //Remove old custom fields
                        val fieldsIterator = result!!.iterator()
                        while (fieldsIterator.hasNext()) {
                            val field = fieldsIterator.next()
                            val fieldIndex = field.fieldIndex
                            if (fieldIndex == 19 || fieldIndex == 20 || fieldIndex == 21) {
                                fieldsIterator.remove()
                            }
                        }

                        headersAndFields = result

                        //Get person live data and observe
                        personLiveData = personDao.findByUidLive(personUid)
                        //Observe the live data
                        personLiveData!!.observe(this@PersonEditPresenter,
                                UmObserver<Person> { this@PersonEditPresenter.handlePersonValueChanged(it) })

                        //                //Get all custom fields (if any) (old way)
                        //                personCustomFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID,
                        //                        new UmCallback<List<PersonField>>() {
                        //                    @Override
                        //                    public void onSuccess(List<PersonField> customFields) {
                        //                        //Create a list of every custom fields supposed to be and fill them with
                        //                        //blank values that will be used to display empty fields. If those fields
                        //                        //exists, then they will get replaced in the next Dao call.
                        //                        customFieldWithFieldValueMap = new HashMap<>();
                        //                        for(PersonField customField:customFields){
                        //
                        //                            //the blank custom field value.
                        //                            PersonCustomFieldValue blankCustomValue = new PersonCustomFieldValue();
                        //                            blankCustomValue.setFieldValue("");
                        //
                        //                            //Create a (custom field + custom value) map object
                        //                            PersonCustomFieldWithPersonCustomFieldValue blankCustomMap =
                        //                                    new PersonCustomFieldWithPersonCustomFieldValue();
                        //                            blankCustomMap.setFieldName(customField.getFieldName());
                        //                            blankCustomMap.setLabelMessageId(customField.getLabelMessageId());
                        //                            blankCustomMap.setFieldIcon(customField.getFieldIcon());
                        //                            blankCustomMap.setCustomFieldValue(blankCustomValue);
                        //
                        //                            //Set the custom field and the field+value object to the map.
                        //                            customFieldWithFieldValueMap.put(customField.getPersonCustomFieldUid(),
                        //                                    blankCustomMap);
                        //                        }
                        //
                        //                        //Get all the custom fields and their values for this person (if applicable)
                        //                        personCustomFieldValueDao.findByPersonUidAsync2(personUid,
                        //                            new UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>>() {
                        //                                @Override
                        //                                public void onSuccess(List<PersonCustomFieldWithPersonCustomFieldValue> result) {
                        //
                        //                                    //Store the values and fields in this Map
                        //
                        //                                    for (PersonCustomFieldWithPersonCustomFieldValue fieldWithFieldValue : result) {
                        //                                        customFieldWithFieldValueMap.put(
                        //                                                fieldWithFieldValue.getPersonCustomFieldUid(), fieldWithFieldValue);
                        //                                    }
                        //
                        //                                    //Get person live data and observe
                        //                                    personLiveData = personDao.findByUidLive(personUid);
                        //                                    //Observe the live data
                        //                                    personLiveData.observe(PersonEditPresenter.this,
                        //                                            PersonEditPresenter.this::handlePersonValueChanged);
                        //                                }
                        //
                        //                                @Override
                        //                                public void onFailure(Throwable exception) {exception.printStackTrace();}
                        //                        });
                        //
                        //                    }
                        //
                        //                    @Override
                        //                    public void onFailure(Throwable exception) {
                        //                        exception.printStackTrace();
                        //                    }
                        //                });

                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })

    }

    /**
     * Updates the pic of the person after taken to the Person object directly
     *
     * @param picPath    The whole path of the picture.
     */
    fun updatePersonPic(picPath: String) {
        //Find the person
        personDao.findByUidAsync(personUid, object : UmCallback<Person> {
            override fun onSuccess(personWithPic: Person?) {

                val personPictureDao = repository.personPictureDao
                personPictureDao.findByPersonUidAsync(personWithPic!!.personUid,
                        object : UmCallback<PersonPicture> {
                            override fun onSuccess(personPicture: PersonPicture?) {
                                if (personPicture != null) {
                                    val picFile = File(picPath)
                                    personPictureDao.setAttachmentFromTmpFile(
                                            personPicture.personPictureUid, picFile)
                                }
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        })

                //Update personWithpic
                personDao.updatePersonAsync(personWithPic, loggedInPersonUid, object : UmCallback<Int> {
                    //personDao.updateAsync(personWithPic, new UmCallback<Integer>(){

                    override fun onSuccess(result: Int?) {
                        generateFeedsForPersonUpdate(repository, mUpdatedPerson!!)
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    /**
     * Generates live data for Clazz list to be assigned to the current Person being edited.
     */
    fun generateAssignedClazzesLiveData() {
        val clazzDao = repository.clazzDao
        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid)
        updateClazzListProviderToView()
    }

    /**
     * Updates the Clazz List provider of type ClazzWithNumStudents that is set on this Presenter to
     * the View.
     */
    private fun updateClazzListProviderToView() {
        view.setClazzListProvider(assignedClazzes!!)
    }

    private fun updatePersonPic(thisPerson: Person) {
        val personPictureDao = repository.personPictureDao
        personPictureDao.findByPersonUidAsync(thisPerson.personUid,
                object : UmCallback<PersonPicture> {
                    override fun onSuccess(personPicture: PersonPicture?) {
                        if (personPicture != null) {
                            view.updateImageOnView(personPictureDao.getAttachmentPath(
                                    personPicture.personPictureUid))
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }

    /**
     * Common method to set edit fields up for the current Person Editing.
     *
     * @param thisPerson The person being edited
     * @param allFields The Fields
     * @param thisView  The View
     * @param valueMap  The Custom fields value map
     */
    private fun setFieldsOnView(thisPerson: Person, allFields: List<PersonDetailPresenterField>,
                                thisView: PersonEditView,
                                valueMap: Map<Long, PersonCustomFieldWithPersonCustomFieldValue>?) {

        val currnetLocale = Locale.getDefault()

        updatePersonPic(thisPerson)

        //Clear all view before setting fields ?
        view.clearAllFields()

        for (field in allFields) {

            var thisValue: String? = ""

            if (field.fieldType == FIELD_TYPE_HEADER) {
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(FIELD_TYPE_HEADER,
                                field.headerMessageId, null!!), field.headerMessageId)
                continue
            }

            if (field.fieldUid == PERSON_FIELD_UID_FULL_NAME.toLong()) {
                thisValue = thisPerson.firstNames + " " + thisPerson.lastName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FIRST_NAMES.toLong()) {
                thisValue = thisPerson.firstNames
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_LAST_NAME.toLong()) {
                thisValue = thisPerson.lastName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_ATTENDANCE.toLong()) {
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_CLASSES.toLong()) {
                thisValue = "Class Name ..."
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER.toLong()) {
                thisValue = thisPerson.getFatherName() + " (" + thisPerson.getFatherNumber() + ")"
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER.toLong()) {
                thisValue = thisPerson.getMotherName() + " (" + thisPerson.getMotherNum() + ")"
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME.toLong()) {
                thisValue = thisPerson.getFatherName()
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
                thisValue = thisPerson.getMotherName()
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
                thisValue = thisPerson.getFatherNumber()
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
                thisValue = thisPerson.getMotherNum()
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_ADDRESS.toLong()) {
                thisValue = thisPerson.getAddress()
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
                thisValue = UMCalendarUtil.getPrettyDateFromLong(
                        thisPerson.getDateOfBirth(), currnetLocale)
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else {//this is actually a custom field
                var messageLabel = 0
                var iconName: String? = null
                var fieldValue: String? = null
                if (valueMap!![field.fieldUid] != null) {
                    if (valueMap[field.fieldUid]!!.labelMessageId != 0) {
                        messageLabel = valueMap[field.fieldUid]!!.labelMessageId
                    }
                    if (valueMap[field.fieldUid]!!.fieldIcon != null) {
                        iconName = valueMap[field.fieldUid]!!.fieldIcon
                    }
                    if (valueMap[field.fieldUid]!!.customFieldValue!!.getFieldValue() != null) {
                        fieldValue = valueMap[field.fieldUid]!!
                                .customFieldValue!!.getFieldValue()
                    }
                }
                thisView.setField(
                        field.fieldIndex,
                        field.fieldUid,
                        PersonDetailViewField(
                                field.fieldType,
                                messageLabel,
                                iconName!!
                        ), fieldValue!!

                )
            }
        }
    }

    /**
     * Updates fields of Person w.r.t field Id given and the value. This method does NOT persist
     * the data.
     *
     * @param personToUpdate the Person object to update values for.
     * @param fieldcode The field Uid that needs to get updated
     * @param value The value to update the Person's field.
     * @return  The updated Person with the updated field.
     */
    private fun updateSansPersistPersonField(personToUpdate: Person?,
                                             fieldcode: Long, value: Any): Person {

        //Update Core fields
        if (fieldcode == PERSON_FIELD_UID_FIRST_NAMES.toLong()) {
            personToUpdate!!.firstNames = value as String

        } else if (fieldcode == PERSON_FIELD_UID_LAST_NAME.toLong()) {
            personToUpdate!!.lastName = value as String

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NAME.toLong()) {
            personToUpdate!!.setFatherName(value as String)

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
            personToUpdate!!.setFatherNumber(value as String)

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
            personToUpdate!!.setMotherName(value as String)

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
            personToUpdate!!.setMotherNum(value as String)

        } else if (fieldcode == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
            personToUpdate!!.setDateOfBirth(value as Long)

        } else if (fieldcode == PERSON_FIELD_UID_ADDRESS.toLong()) {
            personToUpdate!!.setAddress(value as String)

        } else {
            //This is actually a custom field. (old)

            personCustomFieldValueDao.findCustomFieldByFieldAndPersonAsync(fieldcode,
                    personToUpdate!!.personUid, object : UmCallback<PersonCustomFieldValue> {
                override fun onSuccess(result: PersonCustomFieldValue?) {
                    if (result != null) {
                        result.setFieldValue(value.toString())
                        customFieldsToUpdate.add(result)
                    } else {
                        //Create the custom field
                        val newCustomValue = PersonCustomFieldValue()
                        newCustomValue.setPersonCustomFieldValuePersonUid(personToUpdate.personUid)
                        newCustomValue.setPersonCustomFieldValuePersonCustomFieldUid(fieldcode)
                        personCustomFieldValueDao.insert(newCustomValue)
                        newCustomValue.setFieldValue(value.toString())
                        customFieldsToUpdate.add(newCustomValue)
                    }
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        }

        return personToUpdate

    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * The Live Data handler calls this method when the data (via Live data) is updated.
     *
     * @param person The person that needs to be displayed.
     */
    private fun handlePersonValueChanged(person: Person?) {
        //set the og person value
        if (mOriginalValuePerson == null)
            mOriginalValuePerson = person

        if (mUpdatedPerson == null || mUpdatedPerson != person) {
            //set fields on the view as they change and arrive.
            if (person != null) {
                setFieldsOnView(person, headersAndFields!!, view,
                        customFieldWithFieldValueMap)
                mUpdatedPerson = person
            }
        }
    }

    /**
     * Handles every field Edit (focus changed).
     *
     * @param fieldCode The field code that needs editing
     * @param value The new value of the field from the view
     */
    fun handleFieldEdited(fieldCode: Long, value: Any) {
        //TODO: Check this warning
        mUpdatedPerson = updateSansPersistPersonField(mUpdatedPerson, fieldCode, value)
    }

    /**
     * Click handler when Add new Class clicked on Classes section
     */
    fun handleClickAddNewClazz() {
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid)
        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context)
    }

    /**
     * Saves custom field values.
     * @param viewId
     * @param type
     * @param value
     */
    fun handleSaveCustomFieldValues(viewId: Int, type: Int, value: Any) {

        //Lookup viewId
        if (viewIdToCustomFieldUid.containsKey(viewId)) {
            val customFieldUid = viewIdToCustomFieldUid[viewId]!!

            var valueString: String? = null
            if (type == CustomField.FIELD_TYPE_TEXT) {
                valueString = value.toString()

            } else if (type == CustomField.FIELD_TYPE_DROPDOWN) {
                val spinnerSelection = value as Int
                val options = customFieldDropDownOptions[customFieldUid]
                //valueString = options.get(spinnerSelection);
                //or:
                valueString = spinnerSelection.toString()
            }

            if (valueString != null && !valueString.isEmpty()) {
                val finalValueString = valueString
                customFieldValueDao!!.findValueByCustomFieldUidAndEntityUid(customFieldUid,
                        personUid, object : UmCallback<CustomFieldValue> {
                    override fun onSuccess(result: CustomFieldValue?) {
                        val customFieldValue: CustomFieldValue?
                        if (result == null) {
                            customFieldValue = CustomFieldValue()
                            customFieldValue.customFieldValueEntityUid = personUid
                            customFieldValue.customFieldValueFieldUid = customFieldUid
                            customFieldValue.customFieldValueValue = finalValueString
                            customFieldValueDao!!.insert(customFieldValue)
                        } else {
                            customFieldValue = result
                            customFieldValue.customFieldValueValue = finalValueString
                            customFieldValueDao!!.update(customFieldValue)
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
            }

        }
    }

    /**
     * Done click handler on the Edit / Enrollment page: Clicking done will persist and save it and
     * end the activity.
     *
     */
    fun handleClickDone() {
        mUpdatedPerson!!.active = true
        personDao.updatePersonAsync(mUpdatedPerson, loggedInPersonUid, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {

                //Update the custom fields
                personCustomFieldValueDao.updateListAsync(customFieldsToUpdate,
                        object : UmCallback<Int> {
                            override fun onSuccess(result: Int?) {

                                //Start of feed generation
                                generateFeedsForPersonUpdate(repository, mUpdatedPerson!!)

                                //Close the activity.
                                view.finish()
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        })

            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }

    companion object {

        internal fun generateFeedsForPersonUpdate(repository: UmAppDatabase, mUpdatedPerson: Person) {
            //All edits trigger a feed
            val personClazzes = repository.clazzDao
                    .findAllClazzesByPersonUidAsList(mUpdatedPerson.personUid)

            val newFeedEntries = ArrayList<FeedEntry>()
            val updateFeedEntries = ArrayList<FeedEntry>()

            val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                    PersonDetailView.ARG_PERSON_UID + "=" +
                    mUpdatedPerson.personUid

            for (everyClazz in personClazzes) {
                val mneOfficerRole = repository.roleDao.findByNameSync(Role.ROLE_NAME_MNE)
                val mneofficers = repository.clazzDao.findPeopleWithRoleAssignedToClazz(
                        everyClazz.clazzUid, mneOfficerRole.roleUid)

                val admins = repository.personDao.findAllAdminsAsList()

                for (mne in mneofficers) {
                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            mne.personUid, everyClazz.clazzUid,
                            ScheduledCheck.TYPE_CHECK_PERSON_PROFILE_UPDATED,
                            feedLinkViewPerson)

                    val thisEntry = FeedEntry(
                            feedEntryUid,
                            "Student details updated",
                            "Student " + mUpdatedPerson.firstNames
                                    + " " + mUpdatedPerson.lastName
                                    + " details updated",
                            feedLinkViewPerson,
                            everyClazz.clazzName!!,
                            mne.personUid
                    )
                    val existingEntry = repository.feedEntryDao.findByUid(feedEntryUid)

                    if (existingEntry == null) {
                        newFeedEntries.add(thisEntry)
                    } else {
                        updateFeedEntries.add(thisEntry)
                    }
                }

                for (admin in admins) {
                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            admin.personUid, everyClazz.clazzUid,
                            ScheduledCheck.TYPE_CHECK_PERSON_PROFILE_UPDATED,
                            feedLinkViewPerson)

                    val thisEntry = FeedEntry(
                            feedEntryUid,
                            "Student details updated",
                            "Student " + mUpdatedPerson.firstNames
                                    + " " + mUpdatedPerson.lastName
                                    + " details updated",
                            feedLinkViewPerson,
                            everyClazz.clazzName!!,
                            admin.personUid
                    )

                    val existingEntry = repository.feedEntryDao.findByUid(feedEntryUid)

                    if (existingEntry == null) {
                        newFeedEntries.add(thisEntry)
                    } else {
                        updateFeedEntries.add(thisEntry)
                    }
                }
            }

            repository.feedEntryDao.insertList(newFeedEntries)
            repository.feedEntryDao.updateList(updateFeedEntries)

            //End of feed Generation
        }
    }


}
