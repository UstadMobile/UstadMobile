package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonWithSaleInfoListView.Companion.ARG_LE_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


class UserProfilePresenter (context: Any, arguments: Map<String, String?>, view: UserProfileView,
                            val personDao: PersonDao, val impl: UstadMobileSystemImpl)
    : UstadBaseController<UserProfileView>(context, arguments, view){

    internal var repository: UmAppDatabase =
            UmAccountManager.getRepositoryForActiveAccount(context)
    private var loggedInPerson: Person? = null
    private var personPictureDao: PersonPictureDao

    var loggedInPersonUid = 0L

    private val languageOptions = impl.getAllUiLanguage(context)

    init {

        //Get provider Dao
        personPictureDao =
                UmAccountManager.getRepositoryForActiveAccount(context).personPictureDao

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val activeAccount = UmAccountManager.getActiveAccount(context)

        if (activeAccount != null) {
            loggedInPersonUid = activeAccount.personUid
            GlobalScope.launch {
                val result = personDao.findByUidAsync(loggedInPersonUid)
                loggedInPerson = result

                if (loggedInPerson != null) {

                    var firstNames = ""
                    var lastName = ""
                    if(result!!.firstNames!=null){
                        firstNames = result!!.firstNames!!
                    }
                    if(result!!.lastName != null){
                        lastName = result!!.lastName!!
                    }

                    val personName = firstNames + " " + lastName
                    view.runOnUiThread(Runnable {
                        view.updateToolbarTitle(personName)
                    })

                    val personPicture =
                            personPictureDao.findByPersonUidAsync(loggedInPerson!!.personUid)
                    if (personPicture != null) {
                        val picturePath = personPictureDao.getAttachmentPath(personPicture)
                        view.updateImageOnView(picturePath!!, true)
                    }
                }
            }
        }

        //TODO: Get last time the device/account was synced.
        val lastSyncedText = impl.getString(MessageID.account_last_synced,
                context)
        val lastSyncedText2 = ""
        val lastSynced = lastSyncedText + " " + lastSyncedText2
        view.runOnUiThread(Runnable {
            view.updateLastSyncedText(lastSynced)
        })
    }

    fun handleClickChangePassword() {
        val args = HashMap<String, String>()
        impl.go(ChangePasswordView.VIEW_NAME, args, context)
    }

    fun handleClickChangeLanguage() {
        val args = HashMap<String, String>()
        impl.go(SelectLanguageDialogView.VIEW_NAME, args, context)

    }

    fun handleClickLogout() {
        val emptyAcccount = UmAccount(0, "", "", "")
        UmAccountManager.setActiveAccount(emptyAcccount, context)
        UmAccountManager.updatePasswordHash(null, context, impl)
        impl.setAppPref(UmAccountManager.PREFKEY_PASSWORD_HASH_USERNAME, "", context)
        val args = HashMap<String, String>()
        impl.go(LoginView.VIEW_NAME, args, context)
    }


    fun handleClickMyWomenEntrepreneurs(){
        val args = HashMap<String, String>()
        args[ARG_LE_UID] = loggedInPersonUid.toString()
        impl.go(PersonWithSaleInfoListView.VIEW_NAME, args, context)
    }

    fun openPictureDialog(imagePath: String) {
        //Open Dialog
        val args = HashMap<String, String>()
        //TODO If needed:
        //        args.put(ARG_PERSON_IMAGE_PATH, imagePath);
        //        args.put(ARG_PERSON_UID, personUid);
        //        impl.go(PersonPictureDialogView.VIEW_NAME, args, context);
    }

    fun handleCompressedImage(imageFilePath: String) {

        GlobalScope.launch {
            try {

                var existingPP = personPictureDao.findByPersonUidAsync(loggedInPersonUid)
                if(existingPP == null){
                    existingPP = PersonPicture()
                    existingPP.personPicturePersonUid = loggedInPersonUid
                    existingPP.picTimestamp = DateTime.nowUnixLong() //Check this TODO
                    val personPictureUid = personPictureDao.insertAsync(existingPP)
                    existingPP.personPictureUid = personPictureUid
                }

                personPictureDao.setAttachment(existingPP, imageFilePath)
                existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
                personPictureDao.update(existingPP)

                //Update person and generate feeds for person
                personDao.updatePersonAsync(loggedInPerson!!, loggedInPersonUid)

                //Update view with path
                val picturePath = personPictureDao.getAttachmentPath(existingPP)
                view.updateImageOnView(picturePath!!, true)
            }catch(e:Exception){
                throw e
            }
        }
    }

    fun handleUserLogout(){
        UmAccountManager.setActiveAccount(UmAccount(0,
                "", "", ""), context)
        //UmAccountManager.setActiveAccount(null, context)

        val args = HashMap<String, String>()
        view.runOnUiThread(Runnable {
            view.callFinishAffinity()
        })

        impl.go(LoginView.VIEW_NAME, args, context)
    }

    fun handleShowLanguageOptions(){
        view.setLanguageOption(languageOptions.values.sorted().toMutableList())
    }
}
