package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonPictureDialogView.Companion.ARG_PERSON_IMAGE_PATH
import com.ustadmobile.core.view.PersonPictureDialogView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class UserProfilePresenter (context: Any, arguments: Map<String, String>, view: UserProfileView,
                            val repository: UmAppDatabase, val impl: UstadMobileSystemImpl)
    : UstadBaseController<UserProfileView>(context, arguments, view){

    private val languageOptions = impl.getAllUiLanguage(context)

    private var personPictureDao: PersonPictureDao = repository.personPictureDao

    private var loggedInPerson: Person? = null

    private val personDao = repository.personDao

    var loggedInPersonUid = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val account = UmAccountManager.getActiveAccount(context)
        if(account != null) {
            GlobalScope.launch {
                val loggedInPersonVal = personDao.findByUidAsync(loggedInPersonUid)
                if(loggedInPersonVal != null) {
                    view.runOnUiThread(Runnable {
                        view.person = loggedInPersonVal
                    })
                }

                personPictureDao.findByPersonUidAsync(account.personUid)?.also {personPicture ->
                    personPictureDao.getAttachmentPath(personPicture)?.also {picPath ->
                        view.runOnUiThread(Runnable { view.updateImageOnView(picPath, true) })
                    }
                }
            }
        }

        view.setCurrentLanguage(languageOptions[impl.getDisplayedLocale(context)])
    }

    fun handleClickChangePassword() {
        val args = HashMap<String, String>()
        impl.go(ChangePasswordView.VIEW_NAME, args, context)
    }


    fun handleUserLogout(){
        UmAccountManager.setActiveAccount(UmAccount(0,
                "", "", ""), context)
        view.callFinishAffinity()
        impl.go(LoginView.VIEW_NAME, mapOf(), context)
    }

    /**
     * Starts the sync process
     */
    fun handleClickLastSync(){
        //TODO Start the sync process.
    }

    fun handleClickLanguage(){
        view.setLanguageOption(languageOptions.values.sorted().toMutableList())
    }

    fun handleLanguageSelected(position: Int){
        val languageName = languageOptions.values.sorted().toMutableList()[position]
        val localeCode = getLocaleCode(languageName)
        if(impl.getDisplayedLocale(context) != localeCode){
            impl.setLocale(localeCode, context)
            view.restartUI()
        }
    }

    private fun getLocaleCode(name: String): String{
        for(pair in languageOptions){
            if(name == pair.value) return pair.key
        }
        return "en"
    }

    fun openPictureDialog(imagePath: String) {
        //Open Dialog
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_IMAGE_PATH, imagePath);
        args.put(ARG_PERSON_UID, loggedInPersonUid.toString());
        impl.go(PersonPictureDialogView.VIEW_NAME, args, context);
    }

    fun handleProfileImageSelected(imageFilePath: String) {
        GlobalScope.launch {
            try {
                var existingPP = personPictureDao.findByPersonUidAsync(loggedInPersonUid)
                if(existingPP == null){
                    existingPP = PersonPicture()
                    existingPP.personPicturePersonUid = loggedInPersonUid
                    existingPP.picTimestamp = systemTimeInMillis()
                    existingPP.personPictureUid = personPictureDao.insertAsync(existingPP)
                }

                personPictureDao.setAttachment(existingPP, imageFilePath)
                existingPP.picTimestamp = systemTimeInMillis()
                personPictureDao.update(existingPP)

                //Update person and generate feeds for person
                if(loggedInPerson != null) {
                    val person = loggedInPerson?:Person()
                    personDao.updatePersonAsync(person, loggedInPersonUid)
                }

                //Update view with path
                val picturePath = personPictureDao.getAttachmentPath(existingPP)?:""
                view.updateImageOnView(picturePath, true)
            }catch(e:Exception){
                throw e
            }
        }
    }

}