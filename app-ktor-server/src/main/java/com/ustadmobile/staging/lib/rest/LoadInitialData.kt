package com.ustadmobile.staging.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.CUSTOM_FIELD_MIN_UID
import com.ustadmobile.lib.db.entities.Role.Companion.ROLE_NAME_CUSTOMER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_ATTENDANCE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_CLASSES
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_CONFIRM_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHERS_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHERS_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FULL_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_ROLE_ASSIGNMENTS
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_HOME_ADDRESS
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHERS_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHERS_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PROFILE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_USERNAME
import com.ustadmobile.lib.db.entities.Role.Companion.ROLE_NAME_MNE
import com.ustadmobile.lib.db.entities.Role.Companion.ROLE_NAME_OFFICER
import com.ustadmobile.lib.db.entities.Role.Companion.ROLE_NAME_SEL
import com.ustadmobile.lib.db.entities.Role.Companion.ROLE_NAME_SITE_STAFF
import com.ustadmobile.lib.db.entities.Role.Companion.ROLE_NAME_TEACHER
import com.ustadmobile.lib.util.encryptPassword
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class LoadInitialData {

    private var appDb: UmAppDatabase? = null

    private var personCustomFieldDao: PersonCustomFieldDao
    private var personDetailPresenterFieldDao: PersonDetailPresenterFieldDao
    private var personDao: PersonDao
    private var roleDao: RoleDao
    private var entityRoleDao: EntityRoleDao
    private var personAuthDao: PersonAuthDao
    private var personGroupMemberDao: PersonGroupMemberDao

    private lateinit var officerRole: Role
    private lateinit var selRole: Role
    private var fieldIndex = 0
    private lateinit var allFields: List<HeadersAndFields>

    constructor(db: UmAppDatabase) {
        appDb = db
        personCustomFieldDao = db.personCustomFieldDao
        personDetailPresenterFieldDao = db.personDetailPresenterFieldDao
        personDao = db.personDao
        roleDao = db.roleDao
        entityRoleDao = db.entityRoleDao
        personAuthDao = db.personAuthDao
        personGroupMemberDao = db.personGroupMemberDao

    }

    fun loadData() {
        println("\nServletContextListener started")

        //Load initial data
        loadInitialData()

    }

    private fun loadInitialData() {
        addRolesAndPermissions()
    }


    /**
     * Create the SEL officer.
     */
    private fun createSELOfficer() {

        var selPerson = personDao!!.findByUsername("sel")
        if (selPerson == null) {
            selPerson = Person()
            selPerson.active = false
            selPerson.username = "sel"
            selPerson.firstNames = "SEL"
            selPerson.lastName = "SEL"

            GlobalScope.launch {
                val personWithGroup = personDao.createPersonWithGroupAsync(selPerson)

                val selPersonUid = personWithGroup!!.personUid

                //Create password

                val result = personAuthDao.insert(PersonAuth(selPersonUid,
                        PersonAuthDao.ENCRYPTED_PASS_PREFIX + encryptPassword("irZahle4")))

                if (result != null) {

                    //Create entity roles for all clazzes
                    //Assign Role for all clazzes

                    println("SEL created. Updating permissions over all classes")
                    val allClazzes = personDao.findAllClazzes()
                    for (thisClazz in allClazzes) {
                        val entityRole = EntityRole()
                        entityRole.erRoleUid = selRole!!.roleUid
                        entityRole.erTableId = Clazz.TABLE_ID
                        entityRole.erGroupUid = personWithGroup.personGroupUid
                        entityRole.erEntityUid = thisClazz.clazzUid
                        entityRoleDao!!.insert(entityRole)
                    }

                } else {
                    println("LoadInitialData: Unable to set auth")
                }

                //Adding field data:
                addFieldData()

            }


        } else {  //SEL person has been created.

            println("SEL already created. Updating permissions over all classes")
            GlobalScope.launch {
                val result = personGroupMemberDao.findAllGroupWherePersonIsIn(selPerson.personUid)

                if (!result!!.isEmpty()) {

                    val selGroupUid = result[0].groupMemberGroupUid

                    val allClazzes = personDao.findAllClazzes()
                    for (thisClazz in allClazzes) {

                        val existingER = entityRoleDao.findByEntitiyAndPersonGroup(Clazz.TABLE_ID,
                                thisClazz.clazzUid, selGroupUid)
                        if (existingER!!.isEmpty()) {
                            val entityRole = EntityRole()
                            entityRole.erRoleUid = selRole.roleUid
                            entityRole.erTableId = Clazz.TABLE_ID
                            entityRole.erGroupUid = selGroupUid
                            entityRole.erEntityUid = thisClazz.clazzUid
                            entityRoleDao.insertAsync(entityRole)
                        }

                    }
                } else {
                    println("ServletContxtClass: " + "ERROR Unable to find Person Group. ERROR")
                }

                //Adding stuff
                addFieldData()

            }
        }
    }


    private fun createOfficer() {

        var officerPerson = personDao!!.findByUsername("officer")
        if (officerPerson == null) {
            officerPerson = Person()
            officerPerson.active = false
            officerPerson.username = "officer"
            officerPerson.firstNames = "Officer"
            officerPerson.lastName = "Officer"

            GlobalScope.launch {
                val personWithGroup = personDao.createPersonWithGroupAsync(officerPerson)
                val officerPersonUid = personWithGroup!!.personUid

                //Create password
                val officerPersonAuth = PersonAuth(officerPersonUid,
                        PersonAuthDao.ENCRYPTED_PASS_PREFIX + encryptPassword("irZahle3"))

                val result = personAuthDao!!.insertAsync(officerPersonAuth)

                if (result != null) {

                    //Create entity roles for all clazzes
                    //Assign Role for all clazzes

                    println("Officer created. Assigning permission over classes")

                    val allClazzes = personDao.findAllClazzes()
                    for (thisClazz in allClazzes) {
                        val entityRole = EntityRole()
                        entityRole.erRoleUid = officerRole!!.roleUid
                        entityRole.erTableId = Clazz.TABLE_ID
                        entityRole.erGroupUid = personWithGroup.personGroupUid
                        entityRole.erEntityUid = thisClazz.clazzUid
                        entityRoleDao!!.insert(entityRole)
                    }

                } else {
                    println("LoadInitialData: Unable to set auth")
                }


                //Adding SEL data:
                createSELOfficer()

            }


        } else {
            println("Officer already created. Updating permissions over all classes")
            GlobalScope.launch {
                val result = personGroupMemberDao!!.findAllGroupWherePersonIsIn(officerPerson.personUid)

                if (!result!!.isEmpty()) {

                    val officerGroupUid = result[0].groupMemberGroupUid

                    val allClazzes = personDao.findAllClazzes()
                    for (thisClazz in allClazzes) {

                        GlobalScope.launch {
                            val existingER = entityRoleDao!!.findByEntitiyAndPersonGroup(Clazz.TABLE_ID,
                                    thisClazz.clazzUid, officerGroupUid)

                            if (existingER!!.isEmpty()) {
                                val entityRole = EntityRole()
                                entityRole.erRoleUid = officerRole!!.roleUid
                                entityRole.erTableId = Clazz.TABLE_ID
                                entityRole.erGroupUid = officerGroupUid
                                entityRole.erEntityUid = thisClazz.clazzUid
                                entityRoleDao.insert(entityRole)
                            }
                        }

                    }
                } else {
                    println("ServletContxtClass: " + "ERROR Unable to find Person Group. ERROR")
                }
            }

            //Go next to SEL
            createSELOfficer()

        }
    }


    private fun addRolesAndPermissions() {

        //TEACHER

        GlobalScope.launch {
            val result = roleDao!!.findByName(ROLE_NAME_TEACHER)
            if (result == null) {
                //Add teacher role
                val newRole = Role()

                newRole.roleName = ROLE_NAME_TEACHER
                val teacherPermissions = Role.PERMISSION_CLAZZ_ADD_STUDENT or
                        Role.PERMISSION_CLAZZ_SELECT or                  //See Clazzes

                        Role.PERMISSION_CLAZZ_UPDATE or                  //Update Clazz

                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or     //See Clazz Activity

                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE or     //Update Clazz Activity

                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT or     //Add/Take Clazz Activities

                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or   //See Attendance

                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT or   //Take attendance

                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE or   //Update attendance

                        Role.PERMISSION_PERSON_SELECT or                //See People

                        Role.PERMISSION_PERSON_PICTURE_INSERT or         //Insert Person Picture

                        Role.PERMISSION_PERSON_PICTURE_SELECT or         //See Person Picture

                        Role.PERMISSION_PERSON_PICTURE_UPDATE           //Update Person picture
                newRole.rolePermissions = teacherPermissions
                newRole.roleUid = roleDao.insert(newRole)
            }

            //Officer
            val oResult = roleDao.findByName(ROLE_NAME_OFFICER)
            if (oResult == null) {
                val newRole = Role()
                newRole.roleName = ROLE_NAME_OFFICER
                val officerPermissions = Role.PERMISSION_CLAZZ_ADD_STUDENT or
                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or
                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or
                        Role.PERMISSION_CLAZZ_SELECT or
                        Role.PERMISSION_CLAZZ_UPDATE or
                        Role.PERMISSION_PERSON_SELECT or
                        Role.PERMISSION_PERSON_UPDATE or
                        Role.PERMISSION_PERSON_INSERT or
                        Role.PERMISSION_PERSON_PICTURE_INSERT or
                        Role.PERMISSION_PERSON_PICTURE_SELECT or
                        Role.PERMISSION_PERSON_PICTURE_UPDATE
                newRole.rolePermissions = officerPermissions
                newRole.roleUid = roleDao.insert(newRole)

                officerRole = newRole


            } else {
                officerRole = result!!
            }

            //SEL
            val sResult = roleDao.findByName(ROLE_NAME_SEL)
            if (sResult == null) {
                val newRole = Role()
                newRole.roleName = ROLE_NAME_SEL
                val selPermissions = Role.PERMISSION_CLAZZ_SELECT or
                        Role.PERMISSION_PERSON_SELECT or
                        Role.PERMISSION_PERSON_PICTURE_SELECT or
                        Role.PERMISSION_SEL_QUESTION_SELECT or
                        Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT or
                        Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT or
                        Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE
                newRole.rolePermissions = selPermissions
                roleDao.insert(newRole)
                selRole = newRole
            } else {
                selRole = result!!
            }

            //MNE
            val mResult = roleDao.findByName(ROLE_NAME_MNE)
            if (mResult == null) {
                val newRole = Role()
                newRole.roleName = ROLE_NAME_MNE
                val mnePermissions = Role.PERMISSION_CLAZZ_ADD_STUDENT or
                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or
                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or
                        Role.PERMISSION_CLAZZ_SELECT or
                        Role.PERMISSION_CLAZZ_UPDATE or
                        Role.PERMISSION_PERSON_SELECT or
                        Role.PERMISSION_PERSON_UPDATE or
                        Role.PERMISSION_PERSON_INSERT or
                        Role.PERMISSION_PERSON_PICTURE_INSERT or
                        Role.PERMISSION_PERSON_PICTURE_SELECT or
                        Role.PERMISSION_PERSON_PICTURE_UPDATE or
                        Role.PERMISSION_SEL_QUESTION_INSERT or
                        Role.PERMISSION_SEL_QUESTION_UPDATE or
                        Role.PERMISSION_SEL_QUESTION_SELECT or
                        Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT or
                        Role.PERMISSION_REPORTS_VIEW
                newRole.rolePermissions = mnePermissions
                roleDao.insert(newRole)
            }

            //SITE STAFF
            val ssResult = roleDao.findByName(ROLE_NAME_SITE_STAFF)
            if (ssResult == null) {
                val newRole = Role()
                newRole.roleName = ROLE_NAME_SITE_STAFF
                val siteStaffPermissions = Role.PERMISSION_PERSON_SELECT or Role.PERMISSION_PERSON_PICTURE_SELECT
                newRole.rolePermissions = siteStaffPermissions
                roleDao.insert(newRole)
            }

            //Customer
            val customerResult = roleDao.findByName(ROLE_NAME_CUSTOMER)
            if(customerResult == null){
                val customerRole = Role()
                customerRole.roleName = ROLE_NAME_CUSTOMER
                roleDao.insert(customerRole)
            }

            createOfficer()

        }

    }

    private fun addNextField() {

        if (fieldIndex >= allFields!!.size) {
            return
        }
        val field = allFields!![fieldIndex]

        var isHeader = false
        if (field.fieldType == PersonField.FIELD_TYPE_HEADER) {
            isHeader = true
        }

        val finalIsHeader = isHeader

        GlobalScope.launch {

            val resultList = personCustomFieldDao!!.findByFieldNameAsync(field.fieldName)

            //Create the custom fields - basically label & icon .
            val personField = PersonField()

            if (resultList!!.isEmpty()) {

                //Create the field only if it is a field (ie not a header)
                if (!finalIsHeader) {
                    personField.fieldIcon = field.fieldIcon //Icon
                    personField.fieldName = field.fieldName //Internal name
                    personField.labelMessageId = field.fieldLabel    //Label

                    //Set PersonFields' Uid (PersonCustomFieldUid) (No auto generation)
                    //If field not set ie its a Custom Field
                    if (field.fieldUid == 0) {
                        //It is a custom field
                        val lastPersonCustomFieldUidUsed = personCustomFieldDao.findLatestUid()
                        var newCustomPersonCustomFieldUid = lastPersonCustomFieldUidUsed + 1
                        if (lastPersonCustomFieldUidUsed < CUSTOM_FIELD_MIN_UID) {
                            //first Custom field
                            newCustomPersonCustomFieldUid = CUSTOM_FIELD_MIN_UID + 1
                        }
                        personField.personCustomFieldUid = newCustomPersonCustomFieldUid.toLong()
                        field.fieldUid = newCustomPersonCustomFieldUid

                    } else {
                        //Not a custom field.
                        personField.personCustomFieldUid = field.fieldUid.toLong()   //Field's uid
                    }

                    //Persist
                    val result = personCustomFieldDao.insertAsync(personField)
                    //Persist 2
                    createPersonDetailPresenterField(field, finalIsHeader, personField,
                            personDetailPresenterFieldDao!!, true)

                } else {

                    //Persist 2
                    createPersonDetailPresenterField(field, finalIsHeader, personField,
                            personDetailPresenterFieldDao!!, true)
                }

            } else {
                //Persist 2
                createPersonDetailPresenterField(field, finalIsHeader, personField,
                        personDetailPresenterFieldDao!!, true)
            }

        }

    }


    /**
     * Adds dummy data in the start of the application here. It also sets a key so that we don't
     * add the dummy data every time. This will get replaced with real data that will sync with
     * the server.
     */
    private fun addFieldData() {
        allFields = getAllFields()

        println("Adding PersonFields and PersonDetailPresenter entities. \n")
        //Start with next field (1st field really)
        addNextField()

    }

    private fun createPersonDetailPresenterField(field: HeadersAndFields, isHeader: Boolean,
                                                 pcf: PersonField, personDetailPresenterFieldDao: PersonDetailPresenterFieldDao,
                                                 gotoNext: Boolean?) {


        GlobalScope.launch {
            val resultList2 = personDetailPresenterFieldDao.findAllByFieldIndex(field.fieldIndex)

            val resultList3 = personDetailPresenterFieldDao.findAllByFieldUid(field.fieldUid.toLong())

            if (resultList2!!.isEmpty()) {
                println("Adding field : " + field.fieldUid)

                //Create the Mapping between the fields and extra information like :
                //  type(header / field)
                //  index (for ordering)
                //  Header String Id (if header)
                //
                val pdpf1 = PersonDetailPresenterField()
                pdpf1.fieldType = field.fieldType
                pdpf1.fieldIndex = field.fieldIndex

                pdpf1.fieldIcon = field.fieldIcon
                pdpf1.labelMessageId = field.fieldLabel

                //Set Visibility
                pdpf1.isReadyOnly = field.readOnly
                pdpf1.viewModeVisible = field.viewMode
                pdpf1.editModeVisible = field.editMode

                //If not a header set the field. If is header, set the header label.
                if (!isHeader) {
                    val pcfUid = pcf.personCustomFieldUid
                    pdpf1.fieldUid = pcfUid
                } else {
                    pdpf1.headerMessageId = field.headerMessageId
                }

                //persist:
                personDetailPresenterFieldDao.insert(pdpf1)
            }else{
            }

            if (gotoNext!!) {
                fieldIndex++
                addNextField()
            }
        }
    }

    /**
     * Just a POJO for this test class to loop through and create the fields.
     */
    internal inner class HeadersAndFields(//icon
            var fieldIcon: String, //random name
            var fieldName: String, //label
            var fieldLabel: Int, //field uid
            var fieldUid: Int,
            //index (order)
            var fieldIndex: Int, //type (field/header)
            var fieldType: Int, //header label (if applicable)
            var headerMessageId: Int,
            var readOnly: Boolean, var viewMode: Boolean, var editMode: Boolean)


    private fun getAllFields(): List<HeadersAndFields> {

        val allTheFields = ArrayList<HeadersAndFields>()

        allTheFields.add(HeadersAndFields(
                "",
                "",
                0,
                0,
                1,
                PersonField.FIELD_TYPE_HEADER,
                FIELD_HEADING_PROFILE,
                false,
                true,
                true
        ))
        allTheFields.add(HeadersAndFields(
                "",
                "Full Name",
                FIELD_HEADING_FULL_NAME,
                PersonDetailPresenterField.PERSON_FIELD_UID_FULL_NAME,
                2,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                true,
                false
        ))

        ///FIRST NAME LAST NAME
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "First Names",
                FIELD_HEADING_FIRST_NAMES,
                PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES,
                3,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                false,
                true
        ))
        allTheFields.add(HeadersAndFields(
                "",
                "Last Name",
                FIELD_HEADING_LAST_NAME,
                PersonDetailPresenterField.PERSON_FIELD_UID_LAST_NAME,
                4,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                false,
                true
        ))

        allTheFields.add(HeadersAndFields(
                "",
                "Username",
                FIELD_HEADING_USERNAME, //Field Label (for lookup)
                PersonDetailPresenterField.PERSON_FIELD_UID_USERNAME, //field Uid
                5,
                PersonField.FIELD_TYPE_USERNAME,
                0,
                false,
                true,
                true
        ))

        allTheFields.add(HeadersAndFields(
                "",
                "Password",
                FIELD_HEADING_PASSWORD, //Field Label (for lookup)
                PersonDetailPresenterField.PERSON_FIELD_UID_PASSWORD, //field Uid
                6,
                PersonField.FIELD_TYPE_PASSWORD,
                0,
                false,
                false,
                true
        ))

        allTheFields.add(HeadersAndFields(
                "",
                "Confirm password",
                FIELD_HEADING_CONFIRM_PASSWORD, //Field Label (for lookup)
                PersonDetailPresenterField.PERSON_FIELD_UID_CONFIRM_PASSWORD, //field Uid
                7,
                PersonField.FIELD_TYPE_PASSWORD,
                0,
                false,
                false,
                true
        ))

        //BIRTHDAY
        allTheFields.add(HeadersAndFields(
                "ic_perm_contact_calendar_black_24dp",
                "Date of Birth",
                FIELD_HEADING_BIRTHDAY,
                PersonDetailPresenterField.PERSON_FIELD_UID_BIRTHDAY,
                8,
                PersonField.FIELD_TYPE_DATE,
                0,
                false,
                true,
                true
        ))
        //ADDRESS
        allTheFields.add(HeadersAndFields(
                "",
                "Home Address",
                FIELD_HEADING_HOME_ADDRESS,
                PersonDetailPresenterField.PERSON_FIELD_UID_ADDRESS,
                9,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                true,
                true
        ))

        //ATTENDANCE
        allTheFields.add(HeadersAndFields(
                "",
                "",
                0,
                0,
                10,
                PersonField.FIELD_TYPE_HEADER,
                FIELD_HEADING_ATTENDANCE,
                false,
                true,
                false
        ))
        allTheFields.add(HeadersAndFields(
                "ic_lens_black_24dp",
                "Total Attendance for student and days",
                FIELD_HEADING_ATTENDANCE,
                PersonDetailPresenterField.PERSON_FIELD_UID_ATTENDANCE,
                11,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                true,
                false
        ))

        //PARENTS
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "Father with number",
                FIELD_HEADING_FATHER,
                PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER,
                12,
                PersonField.FIELD_TYPE_PHONE_NUMBER,
                0,
                false,
                true,
                false
        ))
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "Father name",
                FIELD_HEADING_FATHERS_NAME,
                PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME,
                13,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                false,
                true
        ))
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "Father  number",
                FIELD_HEADING_FATHERS_NUMBER,
                PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NUMBER,
                14,
                PersonField.FIELD_TYPE_PHONE_NUMBER,
                0,
                false,
                false,
                true
        ))
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "Mother name",
                FIELD_HEADING_MOTHERS_NAME,
                PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME,
                15,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                false,
                true
        ))
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "Mother number",
                FIELD_HEADING_MOTHERS_NUMBER,
                PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NUMBER,
                16,
                PersonField.FIELD_TYPE_PHONE_NUMBER,
                0,
                false,
                false,
                true
        ))
        allTheFields.add(HeadersAndFields(
                "ic_person_black_24dp",
                "Mother with number",
                FIELD_HEADING_MOTHER,
                PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER,
                17,
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                true,
                false
        ))

        //CLASSES
        allTheFields.add(HeadersAndFields(
                "",
                "",
                0,
                0,
                18,
                PersonField.FIELD_TYPE_HEADER,
                FIELD_HEADING_CLASSES,
                false,
                true,
                true
        ))

        //ROLE ASSIGNMENTS
        allTheFields.add(HeadersAndFields(
                "",
                "",
                0,
                0,
                19,
                PersonField.FIELD_TYPE_HEADER,
                FIELD_HEADING_ROLE_ASSIGNMENTS,
                false,
                true,
                true
        ))

        return allTheFields
    }
}

