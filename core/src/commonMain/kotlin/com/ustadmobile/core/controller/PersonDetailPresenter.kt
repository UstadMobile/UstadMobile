package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.db.dao.CustomFieldValueDao
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.db.dao.PersonCustomFieldDao
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.PersonAuthDetailView
import com.ustadmobile.core.view.PersonDetailEnrollClazzView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailViewField
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.PersonPictureDialogView
import com.ustadmobile.core.view.RecordDropoutDialogView
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValue
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PersonField
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.Role

import com.ustadmobile.core.view.PersonAuthDetailView.Companion.ARG_PERSONAUTH_PERSONUID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.PersonPictureDialogView.Companion.ARG_PERSON_IMAGE_PATH
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
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_TEXT


/**
 * PersonDetail's Presenter - responsible for the logic of displaying all the details of a person
 * that is being displayed (for viewing). It also handles going to Edit page for this person; showing
 * all assigned classes for this person; setting ui bits for calling/texting parent, showing
 * attendance numbers, marking dropouts, showing profile image, etc.
 *
 */
class PersonDetailPresenter(context: Any, arguments: Map<String, String>?, view: PersonDetailView,
 val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<PersonDetailView>(context, arguments!!, view) {

    private var mPerson: UmLiveData<Person>? = null

    private var presenterFields: List<PersonDetailPresenterField>? = null

    private var customFieldWithFieldValueMap: Map<Long, PersonCustomFieldWithPersonCustomFieldValue>? = null

    var personUid: Long = 0

    private var attendanceAverage: String? = null

    private var oneParentNumber = ""

    private val loggedInPersonUid: Long

    private var assignedClazzes: UmProvider<ClazzWithNumStudents>? = null

    private var personPictureDao: PersonPictureDao? = null

    private var currentPerson: Person? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    private var customFieldDao: CustomFieldDao? = null
    private var customFieldValueDao: CustomFieldValueDao? = null
    private var optionDao: CustomFieldValueOptionDao? = null

    init {

        personUid = (arguments!!.get(ARG_PERSON_UID)!!.toString()).toLong()

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        viewIdToCustomFieldUid = HashMap()
    }

    fun addToMap(viewId: Int, fieldId: Long) {
        viewIdToCustomFieldUid[viewId] = fieldId
    }

    /**
     * Presenter's overridden onCreate that: Gets the mPerson Live Data and observes it.
     *
     * @param savedState    The state
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val personDao = repository.personDao
        val personDetailPresenterFieldDao = repository.personDetailPresenterFieldDao
        val clazzMemberDao = repository.clazzMemberDao

        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        optionDao = repository.customFieldValueOptionDao

        personDao.findByUidAsync(personUid, object : UmCallback<Person> {
            override fun onSuccess(thisPerson: Person?) {
                if (thisPerson != null) {
                    currentPerson = thisPerson
                    if (thisPerson.getFatherNumber() != null && !thisPerson.getFatherNumber().isEmpty()) {
                        oneParentNumber = thisPerson.getFatherNumber()
                    } else if (thisPerson.getMotherNum() != null && !thisPerson.getMotherNum().isEmpty()) {
                        oneParentNumber = thisPerson.getMotherNum()
                    }

                    personPictureDao = repository.personPictureDao
                    personPictureDao!!.findByPersonUidAsync(thisPerson.personUid, object : UmCallback<PersonPicture> {
                        override fun onSuccess(personPicture: PersonPicture?) {
                            if (personPicture != null)
                                view.updateImageOnView(personPictureDao!!.getAttachmentPath(personPicture.personPictureUid))
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

        getAllPersonCustomFields()

        //Get all headers and fields
        personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsViewMode(
                object : UmCallback<List<PersonDetailPresenterField>> {
                    override fun onSuccess(fields: List<PersonDetailPresenterField>?) {

                        //Remove old custom fields
                        val fieldsIterator = fields!!.iterator()
                        while (fieldsIterator.hasNext()) {
                            val field = fieldsIterator.next()
                            val fieldIndex = field.fieldIndex
                            if (fieldIndex == 19 || fieldIndex == 20 || fieldIndex == 21) {
                                fieldsIterator.remove()
                            }
                        }

                        presenterFields = fields
                        customFieldWithFieldValueMap = HashMap()

                        //Get the attendance average for this person.
                        clazzMemberDao.getAverageAttendancePercentageByPersonUidAsync(personUid,
                                object : UmCallback<Float> {
                                    override fun onSuccess(result: Float?) {
                                        if (result == null) {
                                            attendanceAverage = "N/A"
                                        } else {
                                            attendanceAverage = (result * 100).toString() + "%"
                                        }

                                        //Get person live data and observe
                                        mPerson = personDao.findByUidLive(personUid)
                                        mPerson!!.observe(this@PersonDetailPresenter,
                                                UmObserver<Person> { this@PersonDetailPresenter.handlePersonDataChanged(it) })
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

        checkPermissions()
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
                                                    if (finalValueString[0].isEmpty()) {
                                                        finalValueString[0] = "-"
                                                    }
                                                    //view.addCustomFieldText(c, finalValueString);
                                                    view.addComponent(finalValueString[0], c.customFieldName!!)
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
                                                                view.runOnUiThread({
                                                                    view.addComponent(finalValueString, c.customFieldName!!)

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
     * Check permission for logged in user and enable/disable view components
     */
    fun checkPermissions() {
        val clazzDao = repository.clazzDao
        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_PERSON_UPDATE,
                UmCallbackWithDefaultValue(false, object : UmCallback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        view.showFAB(result!!)
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                }))

        val personDao = repository.personDao
        personDao.personHasPermission(loggedInPersonUid, personUid,
                Role.PERMISSION_PERSON_PICTURE_UPDATE,
                UmCallbackWithDefaultValue(false,
                        object : UmCallback<Boolean> {
                            override fun onSuccess(result: Boolean?) {
                                view.showUpdateImageButton(result!!)
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        }
                ))

        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_PERSON_INSERT,
                UmCallbackWithDefaultValue(false, object : UmCallback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        view.showDropout(result!!)
                        view.showEnrollInClass(result)
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                }))

    }

    /**
     * Compresses the image and updates it on on the view.
     * @param imageFile The image file object
     */
    fun handleCompressedImage(imageFile: File) {
        val personPictureDao = repository.personPictureDao
        val personPicture = PersonPicture()
        personPicture.personPicturePersonUid = personUid
        personPicture.picTimestamp = System.currentTimeMillis()

        val personDao = repository.personDao

        personPictureDao.insertAsync(personPicture, object : UmCallback<Long> {
            override fun onSuccess(personPictureUid: Long?) {
                personPictureDao.setAttachmentFromTmpFile(personPictureUid, imageFile)

                //Update person and generate feeds for person
                personDao.updateAsync(currentPerson, object : UmCallback<Int> {
                    override fun onSuccess(result: Int?) {
                        PersonEditPresenter.generateFeedsForPersonUpdate(repository, currentPerson!!)
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
                view.updateImageOnView(personPictureDao.getAttachmentPath(personPictureUid))
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }


    /**
     * Generates the all class list with assignation for the person being displayed.
     */
    fun generateAssignedClazzesLiveData() {
        val clazzDao = repository.clazzDao

        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid)

        setClazzListOnView()
    }

    /**
     * Sets the Class List provider of ClazzNumWithStudents type to the view.
     */
    private fun setClazzListOnView() {
        view.setClazzListProvider(assignedClazzes!!)
    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * @param person The person that needs to be displayed.
     */
    private fun handlePersonDataChanged(person: Person?) {

        val currentLocale = Locale(impl.getLocale(context))

        view.clearAllFields()

        if (person == null) {
            return
        }

        for (field in presenterFields!!) {

            var thisValue: String? = ""

            if (field.fieldType == FIELD_TYPE_HEADER) {
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_HEADER,
                        field.headerMessageId, null!!), field.headerMessageId)
                continue
            }

            if (field.fieldUid == PERSON_FIELD_UID_FULL_NAME.toLong()) {
                if (person.firstNames != null && person.lastName != null)
                    thisValue = person.firstNames + " " + person.lastName
                else
                    thisValue = ""
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FIRST_NAMES.toLong()) {
                thisValue = person.firstNames
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_LAST_NAME.toLong()) {
                thisValue = person.lastName
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_ATTENDANCE.toLong()) {
                if (attendanceAverage != null) {
                    thisValue = attendanceAverage
                }
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_CLASSES.toLong()) {
                thisValue = "Class Name ..."
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER.toLong()) {
                if (person.getFatherNumber() == null) {
                    thisValue = person.getFatherName()
                } else {
                    thisValue = person.getFatherName() + " (" + person.getFatherNumber() + ")"
                }
                //Also tell the view that we need to add call and text buttons for the number

                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, person.getFatherNumber(), field.fieldIcon!!),
                        thisValue!!)

            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER.toLong()) {
                if (person.getMotherNum() == null) {
                    thisValue = person.getMotherName()
                } else {
                    thisValue = person.getMotherName() + " (" + person.getMotherNum() + ")"
                }

                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, person.getMotherNum(), field.fieldIcon!!),
                        thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME.toLong()) {
                thisValue = person.getFatherName()
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
                thisValue = person.getMotherName()
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
                if (person.getFatherName() != null) {
                    if (!person.getFatherName().isEmpty()) {
                        oneParentNumber = person.getFatherNumber()
                    }
                }
                thisValue = person.getFatherNumber()
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
                if (person.getMotherNum() != null) {
                    if (!person.getMotherNum().isEmpty()) {
                        oneParentNumber = person.getMotherNum()
                    }
                }
                thisValue = person.getMotherNum()
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_ADDRESS.toLong()) {
                thisValue = person.getAddress()
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else if (field.fieldUid == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
                thisValue = UMCalendarUtil.getPrettyDateFromLong(
                        person.getDateOfBirth(), currentLocale)
                view.setField(field.fieldIndex, PersonDetailViewField(FIELD_TYPE_TEXT,
                        field.labelMessageId, field.fieldIcon!!), thisValue!!)
            } else {//this is actually a custom field

                val cf = customFieldWithFieldValueMap!![field.fieldUid]
                var cfLabelMessageId = 0
                var cfFieldIcon: String? = ""
                var cfValue: Any? = null

                if (cf != null) {
                    if (cf.labelMessageId != 0) {
                        cfLabelMessageId = cf.labelMessageId
                    }
                    if (cf.fieldIcon != null) {
                        cfFieldIcon = cf.fieldIcon
                    }
                    if (cf.customFieldValue != null) {
                        if (cf.customFieldValue!!.getFieldValue() != null) {
                            cfValue = cf.customFieldValue!!.getFieldValue()
                        }
                    }
                }

                view.setField(
                        field.fieldIndex,
                        PersonDetailViewField(
                                field.fieldType,
                                cfLabelMessageId,
                                cfFieldIcon!!
                        ),
                        cfValue!!
                )
            }
        }
    }

    /**
     * This method is called upon clicking the edit button for that person.
     * This wil take us to PersonDetailEdit
     */
    fun handleClickEdit() {

        view.finish()
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid)
        impl.go(PersonEditView.VIEW_NAME, args, view.getContext())
    }

    /**
     * Handles call parent button - calls the method that calls the parent (open platform native
     * call UI)
     */
    fun handleClickCallParent() {
        if (!oneParentNumber.isEmpty()) {
            handleClickCall(oneParentNumber)
        }
    }

    /**
     * Handles text parent button - calls the method that texts the parent (opens platform native
     * text UI)
     */
    fun handleClickTextParent() {
        if (!oneParentNumber.isEmpty()) {
            handleClickText(oneParentNumber)
        }
    }

    /**
     * Opens person picture (almost) fullscreen dialog
     * @param imagePath The full path of the image
     */
    fun openPictureDialog(imagePath: String) {
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_IMAGE_PATH, imagePath)
        args.put(ARG_PERSON_UID, personUid)
        impl.go(PersonPictureDialogView.VIEW_NAME, args, context)
    }

    /**
     * Handles what happens when Enroll in Class is clicked at the top common big buttons
     * - opens the View that shows the list of classes with their enrollment status ie:
     * PersonDetailEnrollClazz
     */
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid)

        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context)
    }

    fun handleClickRecordDropout() {
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid)

        impl.go(RecordDropoutDialogView.VIEW_NAME, args, context)
    }

    /**
     * Handler to what happens when call button pressed on an entry (usually to call a person)
     *
     * @param number The phone number
     */
    fun handleClickCall(number: String) {
        view.handleClickCall(number)
    }

    /**
     * Handler to what happens when text / sms button pressed on an entry (usually to text a
     * person / parent)
     *
     * @param number The phone number
     */
    fun handleClickText(number: String) {
        view.handleClickText(number)
    }

    fun goToUpdateUsernamePassword() {
        val args = HashMap<String, String>()
        args.put(ARG_PERSONAUTH_PERSONUID, currentPerson!!.personUid)
        impl.go(PersonAuthDetailView.VIEW_NAME, args, context)
    }
}
