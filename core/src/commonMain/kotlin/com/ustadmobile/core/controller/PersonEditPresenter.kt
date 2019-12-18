package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ADDRESS
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ATTENDANCE
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_CLASSES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_CONFIRM_PASSWORD
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FULL_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PASSWORD
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_USERNAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_ATTENDANCE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_CLASSES
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_CONFIRM_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHERS_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHERS_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FULL_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_HOME_ADDRESS
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHERS_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHERS_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PROFILE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_ROLE_ASSIGNMENTS
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_USERNAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_HEADER
import com.ustadmobile.lib.util.encryptPassword
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * PersonEditPresenter : This is responsible for generating the Edit data along with its Custom
 * Fields. It is also responsible for updating the data and checking for changes and handling
 * Done with Save or Discard.
 *
 * * Presenter's constructor where we are getting arguments and setting the newly/editable
 * personUid
 *
 * @param context Android context
 * @param arguments Arguments from the Activity passed here.
 * @param view  The view that called this presenter (PersonEditView->PersonEditActivity)
 */
class PersonEditPresenter (context: Any, arguments: Map<String, String>?, view: PersonEditView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    :UstadBaseController<PersonEditView>(context, arguments!!, view) {

    //Headers and Fields
    private var headersAndFields: List<PersonDetailPresenterField>? = null

    var personUid: Long = 0

    private var updatedPerson: Person? = null
    private var currentPerson: Person? = null
    private var currentPersonAuth: PersonAuth? = null

    private var assignedClazzes: DataSource.Factory<Int, ClazzWithNumStudents>? = null
    private var assignedRoleAssignments: DataSource.Factory<Int, EntityRoleWithGroupName>?= null
    private var groupUmLiveData: DoorLiveData<List<PersonGroup>>? = null

    //The custom fields' values
    private val customFieldWithFieldValueMap: Map<Long, PersonCustomFieldWithPersonCustomFieldValue>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var database = UmAppDatabase.getInstance(context)

    private var newPersonString = ""

    private val customFieldsToUpdate: MutableList<PersonCustomFieldValue>



    private var loggedInPersonUid: Long? = 0L

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    private val personDao : PersonDao
    private val personDaoDB : PersonDao
    private val personGroupDao : PersonGroupDao
    private val personGroupDaoDB : PersonGroupDao
    private var customFieldDao: CustomFieldDao
    private var customFieldValueDao: CustomFieldValueDao
    private var customFieldDaoDB: CustomFieldDao
    private var customFieldValueDaoDB: CustomFieldValueDao
    private var optionDao: CustomFieldValueOptionDao
    private var optionDaoDB: CustomFieldValueOptionDao
    private var personPictureDaoRepo : PersonPictureDao
    private val personAuthDao: PersonAuthDao
    private val personPictureDaoDB : PersonPictureDao
    private val personCustomFieldValueDao : PersonCustomFieldValueDao
    private val personCustomFieldValueDaoDB : PersonCustomFieldValueDao
    private val fieldsDaoRepo : PersonDetailPresenterFieldDao
    private val fieldsDaoDB : PersonDetailPresenterFieldDao
    private val feedEntryDao : FeedEntryDao
    private val feedEntryDaoDB : FeedEntryDao

    private val customFieldDropDownOptions: HashMap<Long, List<String>>

    var passwordSet: String? = null
    var confirmPasswordSet: String? = null
    var usernameSet: String? = null

    private var groupIdToPosition: HashMap<Long, Int>? = null
    private val groupPositionToId: HashMap<Int, Long>
    private var groupPresets: Array<String>? = null
    private var personWEGroupUid : Long = 0L



    init {

        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            personUid = (arguments.get(ARG_PERSON_UID)!!.toString()).toLong()
        }

        if (arguments.containsKey(ARG_NEW_PERSON)) {
            newPersonString = arguments.get(ARG_NEW_PERSON)!!.toString()
        }

        groupIdToPosition = HashMap()
        groupPositionToId = HashMap()
        customFieldsToUpdate = ArrayList()
        viewIdToCustomFieldUid = HashMap()
        customFieldDropDownOptions = HashMap()

        personPictureDaoRepo = UmAccountManager.getRepositoryForActiveAccount(context).personPictureDao
        customFieldDaoDB = database.customFieldDao
        optionDaoDB = database.customFieldValueOptionDao
        customFieldValueDaoDB = database.customFieldValueDao
        fieldsDaoDB = database.personDetailPresenterFieldDao
        personPictureDaoDB = database.personPictureDao
        personDaoDB = database.personDao
        personGroupDaoDB = database.personGroupDao
        personCustomFieldValueDaoDB = database.personCustomFieldValueDao
        feedEntryDaoDB = database.feedEntryDao

        personAuthDao = repository.personAuthDao
        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        optionDao = repository.customFieldValueOptionDao
        fieldsDaoRepo = repository.personDetailPresenterFieldDao
        personDao = repository.personDao
        personGroupDao = repository.personGroupDao
        personCustomFieldValueDao = repository.personCustomFieldValueDao
        feedEntryDao = repository.feedEntryDao

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

    }

    /**
     * Presenter's Overridden onCreate that: Gets the mPerson LiveData and observe it.
     * @param savedState    The saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (newPersonString == "true") {
            view.updateToolbarTitle(impl.getString(MessageID.new_person, context))
        }

        getAllPersonCustomFields()

        val thisP = this

        GlobalScope.launch {

            //Get the person's Auth (create if not exist)
            if(personUid != 0L){

                currentPerson = personDaoDB.findByUidAsync(personUid)

                currentPersonAuth = personAuthDao.findByUidAsync(personUid)
                if (currentPersonAuth == null) {
                    currentPersonAuth = PersonAuth()
                    currentPersonAuth!!.personAuthUid = personUid
                    currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)
                }
            }

            //Get all the currently set headers and fields:
            val resultLive = fieldsDaoDB.findAllPersonDetailPresenterFieldsEditModeLive()
            GlobalScope.launch(Dispatchers.Main){
                resultLive.observe(thisP, thisP::handleFieldsLive)
            }
        }
    }

    //Handle fields first then person
    private fun handleFieldsLive(fields: List<PersonDetailPresenterField>?){
        //Build the fields list:
        val cleanedResult = ArrayList<PersonDetailPresenterField>()
        //Remove old custom fields
        val fieldsIterator = fields!!.iterator()
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            cleanedResult.add(field)
        }
        headersAndFields = cleanedResult

        //Observe the person that will update the fields with values
        GlobalScope.launch {
            val personObj = personDaoDB.findByUidAsync(personUid)
            view.runOnUiThread(Runnable {
                handlePersonValueChanged(personObj)
            })
        }
    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * The Live Data handler calls this method when the data (via Live data) is updated.
     *
     * @param person The person that needs to be displayed.
     */
    private fun handlePersonValueChanged(person: Person?) {

        if (updatedPerson == null || updatedPerson != person) {
            //set fields on the view as they change and arrive.
            if (person != null) {
                updatedPerson = person
                usernameSet = updatedPerson!!.username

                setFieldsOnView(person, headersAndFields!!, view,
                        customFieldWithFieldValueMap)

                personWEGroupUid = person.mPersonGroupUid
            }
        }
    }


    fun addToMap(viewId: Int, fieldId: Long) {
        viewIdToCustomFieldUid[viewId] = fieldId
    }

    /**
     * Getting custom fields (new way)
     */
    private fun getAllPersonCustomFields() {
        //0. Clear all added custom fields on view.
        view.runOnUiThread(Runnable{ view.clearAllCustomFields() })

        GlobalScope.launch {
            //1. Get all custom fields
            val result = customFieldDao!!.findAllCustomFieldsProviderForEntityAsync(Person.TABLE_ID)
            for (c in result) {
                //Get value as well
                val result2 = customFieldValueDao!!.findValueByCustomFieldUidAndEntityUid(
                        c.customFieldUid, personUid)
                var valueString: String? = ""
                var valueSelection = 0

                if (c.customFieldType == CustomField.FIELD_TYPE_TEXT) {

                    if (result2 != null) {
                        valueString = result2.customFieldValueValue
                    }
                    val finalValueString = arrayOf<String>(valueString!!)
                    view.runOnUiThread(Runnable{

                        view.addCustomFieldText(c, finalValueString[0])
                        //view.addComponent(finalValueString[0], c.getCustomFieldName());
                    })

                } else if (c.customFieldType == CustomField.FIELD_TYPE_DROPDOWN) {
                    if (result2 != null) {
                        try {
                            valueSelection = (result2.customFieldValueValue!!).toInt()
                        } catch (nfe: NumberFormatException) {
                            valueSelection = 0
                        }

                    }
                    val finalValueSelection = valueSelection
                    val result3 = optionDao!!.findAllOptionsForFieldAsync(c.customFieldUid)
                    val options = ArrayList<String>()

                    for (o in result3!!) {
                        options.add(o.customFieldValueOptionName!!)
                    }
                    //Get value
                    var valueString = "-"
                    if (finalValueSelection > 0) {
                        valueString = options[finalValueSelection]
                    }
                    val finalValueString = valueString

                    customFieldDropDownOptions[c.customFieldUid] = options
                    view.runOnUiThread(Runnable{
                        val a = arrayOfNulls<String>(options.size)
                        options.toTypedArray()
                        view.addCustomFieldDropdown(c, a, finalValueSelection)
                    })
                }
            }
        }
    }

    fun generateGroupList(){
        //Update group
        groupUmLiveData = personGroupDao.findAllActiveGroupPersonGroupsLive()
        view.runOnUiThread(Runnable {
            groupUmLiveData!!.observe(this, this::handleAllGroupsChanged)
        })
    }

    private fun handleAllGroupsChanged(groups: List<PersonGroup>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        groupIdToPosition = HashMap()

        groupIdToPosition!![0] = 0
        groupPositionToId[0] = 0
        entityList.add(impl.getString(MessageID.no_women_embroiderers_set, context))

        var posIter = 1
        for (everyEntity in groups!!) {
            entityList.add(everyEntity.groupName!!)
            groupIdToPosition!![everyEntity.groupUid] = posIter
            groupPositionToId[posIter] = everyEntity.groupUid
            posIter++
        }
        groupPresets = entityList.toTypedArray()

        if (personWEGroupUid != 0L) {

            if (groupIdToPosition!!.containsKey(personWEGroupUid)) {
                selectedPosition = groupIdToPosition!![personWEGroupUid]!!
            }
        }

        view.setGroupPresets(groupPresets!!, selectedPosition)
    }

    fun updateGroup(position: Int) {

        if(position > 0) {
            if (groupPositionToId.containsKey(position))
                personWEGroupUid = groupPositionToId[position]!!
        }else{
            personWEGroupUid = 0
        }
    }

    /**
     * Updates the pic of the person after taken to the Person object directly
     *
     * @param imageFilePath    The whole path of the picture.
     */
    fun updatePersonPic(imageFilePath: String) {


        //Find the person
        GlobalScope.launch {
            val thisPerson = personDao.findByUidAsync(personUid)

            var personPictureUid : Long = 0L
            var existingPP: PersonPicture ? = null
            existingPP = personPictureDaoRepo.findByPersonUidAsync(personUid)
            if(existingPP == null){
                existingPP = PersonPicture()
                existingPP.personPicturePersonUid = personUid
                existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
                personPictureUid = personPictureDaoRepo.insertAsync(existingPP)
                existingPP.personPictureUid = personPictureUid
            }

            personPictureDaoRepo.setAttachment(existingPP, imageFilePath)
            existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
            personPictureDaoRepo.update(existingPP)

            //Update personWithpic
            personDao.updatePersonAsync(thisPerson!!, loggedInPersonUid!!)
            generateFeedsForPersonUpdate(database, updatedPerson!!)

        }
    }

    /**
     * Generates live data for Clazz list to be assigned to the current Person being edited.
     */
    fun generateAssignedClazzesLiveData() {
        val clazzDao = repository.clazzDao
        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid)
        updateClazzListProviderToView()
    }

    fun generateAssignedRoleAssignments(){
        val roleAssignmentDao = repository.entityRoleDao
        assignedRoleAssignments =
                roleAssignmentDao.findAllActiveRoleAssignmentsByGroupPersonUid(personUid)
        setRoleAssignmentsOnView()
    }

    /**
     * Updates the Clazz List provider of type ClazzWithNumStudents that is set on this Presenter to
     * the View.
     */
    private fun updateClazzListProviderToView() {
        view.setClazzListProvider(assignedClazzes!!)
    }

    private fun setRoleAssignmentsOnView(){
        view.setRoleAssignmentListProvider(assignedRoleAssignments!!)
    }


    private fun updatePersonPic(thisPerson: Person) {

        GlobalScope.launch {

            //Load the local image first
            val personPictureLocal = personPictureDaoDB.findByPersonUidAsync(
                    thisPerson!!.personUid)
            if(personPictureLocal != null) {
                val imagePathLocal = personPictureDaoRepo.getAttachmentPath(personPictureLocal!!)!!;

                if (imagePathLocal.isNotEmpty())
                    view.updateImageOnView(imagePathLocal)
            }

            //Get the server image
            val personPictureServer =
                    personPictureDaoRepo.findByPersonUidAsync(thisPerson!!.personUid)
            if(personPictureServer != null) {
                val imagePathServer =
                        personPictureDaoRepo.getAttachmentPath(personPictureServer!!)!!;

                if (imagePathServer.isNotEmpty())
                    view.updateImageOnView(imagePathServer)
            }


        }

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

        //TODO: Locale on Kotlin Core
//        val currnetLocale = Locale.getDefault()
        val currnetLocale = ""

        updatePersonPic(thisPerson)

        //Clear all view before setting fields ?
        view.clearAllFields()

        for (field in allFields) {

            var labelMessageId = 0
            when(field.labelMessageId){
                FIELD_HEADING_PROFILE -> { labelMessageId = MessageID.profile }
                FIELD_HEADING_FULL_NAME -> { labelMessageId = MessageID.field_fullname }
                FIELD_HEADING_FIRST_NAMES -> { labelMessageId = MessageID.first_names }
                FIELD_HEADING_LAST_NAME -> { labelMessageId = MessageID.last_name }
                FIELD_HEADING_BIRTHDAY -> { labelMessageId = MessageID.birthday }
                FIELD_HEADING_HOME_ADDRESS -> { labelMessageId = MessageID.home_address }
                FIELD_HEADING_ATTENDANCE -> { labelMessageId = MessageID. attendance}
                FIELD_HEADING_FATHER -> { labelMessageId = MessageID.father }
                FIELD_HEADING_FATHERS_NAME -> { labelMessageId = MessageID.fathers_name }
                FIELD_HEADING_FATHERS_NUMBER -> { labelMessageId = MessageID.fathers_number }
                FIELD_HEADING_MOTHERS_NAME -> { labelMessageId = MessageID.mothers_name }
                FIELD_HEADING_MOTHERS_NUMBER -> { labelMessageId = MessageID.mothers_number }
                FIELD_HEADING_MOTHER -> { labelMessageId = MessageID.mother }
                FIELD_HEADING_CLASSES -> { labelMessageId = MessageID.classes }
                FIELD_HEADING_USERNAME -> { labelMessageId = MessageID.username }
                FIELD_HEADING_PASSWORD -> {
                    if (newPersonString == "true") {
                        labelMessageId = MessageID.password
                    }else {
                        labelMessageId = MessageID.password_unchanged
                    }
                }
                FIELD_HEADING_CONFIRM_PASSWORD -> {
                    if (newPersonString == "true") {
                        labelMessageId = MessageID.confirm_password
                    }else {
                        labelMessageId = MessageID.confirm_password_unchanged
                    }
                }
            }

            var headerMessageId = 0
            when(field.headerMessageId){
                FIELD_HEADING_PROFILE -> { headerMessageId = MessageID.profile }
                FIELD_HEADING_FULL_NAME -> { headerMessageId = MessageID.field_fullname }
                FIELD_HEADING_FIRST_NAMES -> { headerMessageId = MessageID.first_names }
                FIELD_HEADING_LAST_NAME -> { headerMessageId = MessageID.last_name }
                FIELD_HEADING_BIRTHDAY -> { headerMessageId = MessageID.birthday }
                FIELD_HEADING_HOME_ADDRESS -> { headerMessageId = MessageID.home_address }
                FIELD_HEADING_ATTENDANCE -> { headerMessageId = MessageID. attendance}
                FIELD_HEADING_FATHER -> { headerMessageId = MessageID.father }
                FIELD_HEADING_FATHERS_NAME -> { headerMessageId = MessageID.fathers_name }
                FIELD_HEADING_FATHERS_NUMBER -> { headerMessageId = MessageID.fathers_number }
                FIELD_HEADING_MOTHERS_NAME -> { headerMessageId = MessageID.mothers_name }
                FIELD_HEADING_MOTHERS_NUMBER -> { headerMessageId = MessageID.mothers_number }
                FIELD_HEADING_MOTHER -> { headerMessageId = MessageID.mother }
                FIELD_HEADING_CLASSES -> { headerMessageId = MessageID.classes }
                FIELD_HEADING_USERNAME -> { headerMessageId = MessageID.username }

                FIELD_HEADING_PASSWORD -> {
                    if (newPersonString == "true") {
                        headerMessageId = MessageID.password
                    }else {
                        headerMessageId = MessageID.password_unchanged
                    }
                }
                FIELD_HEADING_CONFIRM_PASSWORD -> {
                    if (newPersonString == "true") {
                        headerMessageId = MessageID.confirm_password
                    }else {
                        headerMessageId = MessageID.confirm_password_unchanged
                    }
                }

                FIELD_HEADING_ROLE_ASSIGNMENTS -> { headerMessageId = MessageID.role_assignments }

            }

            var thisValue: String? = ""

            if (field.fieldType == FIELD_TYPE_HEADER) {
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(FIELD_TYPE_HEADER,
                                headerMessageId, null), headerMessageId)
                continue
            }

            if (field.fieldUid == PERSON_FIELD_UID_FULL_NAME.toLong()) {
                thisValue = thisPerson.firstNames + " " + thisPerson.lastName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FIRST_NAMES.toLong()) {
                thisValue = thisPerson.firstNames
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_LAST_NAME.toLong()) {
                thisValue = thisPerson.lastName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_ATTENDANCE.toLong()) {
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_CLASSES.toLong()) {
                thisValue = "Class Name ..."
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER.toLong()) {
                thisValue = thisPerson.fatherName + " (" + thisPerson.fatherNumber + ")"
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER.toLong()) {
                thisValue = thisPerson.motherName + " (" + thisPerson.motherNum + ")"
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME.toLong()) {
                thisValue = thisPerson.fatherName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
                thisValue = thisPerson.motherName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
                thisValue = thisPerson.fatherNumber
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
                thisValue = thisPerson.motherNum
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_ADDRESS.toLong()) {
                thisValue = thisPerson.personAddress
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
                if(thisPerson.dateOfBirth > 0L) {
                    thisValue = UMCalendarUtil.getPrettyDateFromLong(
                            thisPerson.dateOfBirth, currnetLocale)
                }else{
                    thisValue = ""
                }
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_USERNAME.toLong() ) {
                usernameSet = thisPerson.username
                thisValue = usernameSet

                if(thisValue == null){
                    thisValue = ""
                }
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if(field.fieldUid == PERSON_FIELD_UID_PASSWORD.toLong()
                    || field.fieldUid == PERSON_FIELD_UID_CONFIRM_PASSWORD.toLong()){
                thisValue = ""
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else {//this is actually a custom field
                var messageLabel = 0
                var iconName: String? = null
                var fieldValue: String? = null
                if (valueMap != null && valueMap.containsKey(field.fieldUid) ){

                    if(valueMap!![field.fieldUid] != null) {
                            if (valueMap[field.fieldUid]!!.labelMessageId != 0) {
                                messageLabel = valueMap[field.fieldUid]!!.labelMessageId
                            }
                            if (valueMap[field.fieldUid]!!.fieldIcon != null) {
                                iconName = valueMap[field.fieldUid]!!.fieldIcon
                            }
                            if (valueMap[field.fieldUid]!!.customFieldValue!!.fieldValue != null) {
                                fieldValue = valueMap[field.fieldUid]!!
                                        .customFieldValue!!.fieldValue
                            }
                    }
                    thisView.setField(
                            field.fieldIndex,
                            field.fieldUid,
                            PersonDetailViewField(
                                    field.fieldType,
                                    messageLabel,
                                    iconName
                            ), fieldValue
                    )
                }
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
            personToUpdate!!.fatherName = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
            personToUpdate!!.fatherNumber = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
            personToUpdate!!.motherName = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
            personToUpdate!!.motherName = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
            personToUpdate!!.dateOfBirth = (value as Long)

        } else if (fieldcode == PERSON_FIELD_UID_ADDRESS.toLong()) {
            personToUpdate!!.personAddress = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_USERNAME.toLong() ){
            usernameSet = value as String
            personToUpdate!!.username = usernameSet
        } else if (fieldcode == PERSON_FIELD_UID_PASSWORD.toLong() ){
            passwordSet = value as String
        } else if (fieldcode == PERSON_FIELD_UID_CONFIRM_PASSWORD.toLong() ){
            confirmPasswordSet = value as String
        }else {
            //This is actually a custom field. (old)
            GlobalScope.launch {
                val result = personCustomFieldValueDaoDB
                        .findCustomFieldByFieldAndPersonAsync(fieldcode, personToUpdate!!.personUid)
                if (result != null) {
                    result.fieldValue = (value.toString())
                    customFieldsToUpdate.add(result)
                } else {
                    //Create the custom field
                    val newCustomValue = PersonCustomFieldValue()
                    newCustomValue.personCustomFieldValuePersonUid = (personToUpdate.personUid)
                    newCustomValue.personCustomFieldValuePersonCustomFieldUid = (fieldcode)
                    personCustomFieldValueDao.insert(newCustomValue)
                    newCustomValue.fieldValue = (value.toString())
                    customFieldsToUpdate.add(newCustomValue)
                }
            }
        }

        return personToUpdate!!

    }



    /**
     * Handles every field Edit (focus changed).
     *
     * @param fieldCode The field code that needs editing
     * @param value The new value of the field from the view
     */
    fun handleFieldEdited(fieldCode: Long, value: Any) {

        updatedPerson = updateSansPersistPersonField(updatedPerson, fieldCode, value)
    }

    /**
     * Click handler when Add new Class clicked on Classes section
     */
    fun handleClickAddNewClazz() {
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid.toString())
        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context)
    }

    fun handleClickAddNewRoleAssignment(){
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid.toString())
        impl.go(RoleAssignmentDetailView.VIEW_NAME, args, context)
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
                GlobalScope.launch {
                    val result =
                            customFieldValueDao!!.findValueByCustomFieldUidAndEntityUid(
                                    customFieldUid, personUid)
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
            }

        }
    }

    /**
     * Done click handler on the Edit / Enrollment page: Clicking done will persist and save it and
     * end the activity.
     *
     */
    fun handleClickDone() {

        view.runOnUiThread(Runnable {
            view.setInProgress(true)
        })

        updatedPerson!!.active = true
        updatedPerson!!.mPersonGroupUid = personWEGroupUid
        GlobalScope.launch {
            personDaoDB.updatePersonAsync(updatedPerson!!, loggedInPersonUid!!)

            //Update the custom fields
            personCustomFieldValueDaoDB.updateListAsync(customFieldsToUpdate)
            //Start of feed generation
            generateFeedsForPersonUpdate(database, updatedPerson!!)

            //Update password if necessary
            val updatePassword = updatePassword()

            //Update person's individual group to  set the right name of the group
            val fullName = updatedPerson!!.fullName()

            val personGroup = personGroupDaoDB.findPersonIndividualGroup(updatedPerson!!.personUid)
            if(personGroup != null){
                personGroup.groupName = fullName + "'s individual person group"
                personGroupDaoDB.updateAsync(personGroup)
            }

            //Close the activity.
            if(updatePassword) {
                view.finish()
            }else{
                view.runOnUiThread(Runnable {
                    view.setInProgress(false)
                })
            }

        }

    }

    private fun updatePassword() : Boolean {


        if (passwordSet != null && !passwordSet!!.isEmpty() && usernameSet != null
                && !usernameSet!!.isEmpty() && currentPersonAuth != null && currentPerson != null) {
            if (passwordSet != confirmPasswordSet) {
                view.sendMessage(MessageID.passwords_dont_match)
                view.runOnUiThread(Runnable {
                    view.setInProgress(false)
                })
                return false

            }
            currentPerson!!.username = usernameSet

            currentPersonAuth!!.passwordHash = PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                    encryptPassword(passwordSet!!)
            currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)
            GlobalScope.launch {
                //Update locally
                personDao.updateAsync(updatedPerson!!)

                //Update on server
                try {
                    val serverUrl = UmAccountManager.getActiveEndpoint(context)
                    val resetPasswordResponse = defaultHttpClient().get<HttpResponse>()
                    {
                        url {
                            takeFrom(serverUrl!!)
                            encodedPath = "${encodedPath}UmAppDatabase/PersonAuthDao/resetPassword"
                        }
                        parameter("p0", personUid)
                        parameter("p1", passwordSet)
                        parameter("p2", loggedInPersonUid)
                    }

                    if(resetPasswordResponse.status == HttpStatusCode.OK) {
                        //Update locally
                        personAuthDao.updateAsync(currentPersonAuth!!)
                        view.finish()
                    }else {
                        view.runOnUiThread(Runnable {
                            view.setInProgress(false)
                        })
                        view.sendMessage(MessageID.unable_to_update_password)
                        println("nope")

                        false
                    }
                } catch (e: Exception) {
                    view.runOnUiThread(Runnable {
                        view.setInProgress(false)
                    })
                    view.sendMessage(MessageID.unable_to_update_password)
                    print("oops")
                    false
                }
            }
            view.runOnUiThread(Runnable {
                view.setInProgress(false)
            })
            return false
        }
        view.runOnUiThread(Runnable {
            view.setInProgress(false)
        })

        return true
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
                        everyClazz.clazzUid, mneOfficerRole!!.roleUid)

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
