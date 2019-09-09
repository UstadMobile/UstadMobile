package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.SELAnswerListView
import com.ustadmobile.core.view.SELSelectStudentView
import com.ustadmobile.lib.db.entities.Person

/**
 * SELAnswerList's presenter - responsible for the logic of all SEL Answer list from the database
 * and handling starting a new SEL run.
 *
 */
class SELAnswerListPresenter(context: Any, arguments: Map<String, String>?,
                             view: SELAnswerListView ,
                             val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : CommonHandlerPresenter<SELAnswerListView>(context, arguments!!, view) {

    private var currentClazzUid = -1L

    private var selAnswersProvider: DataSource.Factory<Int,Person>? = null

    init {

        //Get the clazz uid and set it to the presenter.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

    }

    /**
     * The Presenter here's onCreate.
     * In Order:
     * 1. This populates the provider and sets it to the View.
     *
     * This will be called when the implementation View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState    The saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val repository = UmAccountManager.getRepositoryForActiveAccount(context)

        selAnswersProvider = repository.selQuestionSetResponseDao.findAllDoneSNByClazzUid(currentClazzUid)
        setSELAnswerProviderToView()

    }

    /**
     * Sets the SEL Answer list provider of Person type that is set on this Presenter to the View.
     */
    private fun setSELAnswerProviderToView() {
        view.setSELAnswerListProvider(selAnswersProvider!!)
    }

    /**
     * Handles when Record SEL FAB button is pressed.
     * It should open a new Record SEL activity.
     */
    fun handleClickRecordSEL() {

        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())

        impl.go(SELSelectStudentView.VIEW_NAME, args, view.viewContext)

    }


    /**
     * Handles what happens when the primary button of every item on the SEL Answer list recycler
     * adapter is clicked - It should go to the SEL Answers. TODOne: Finish this.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    override fun handleCommonPressed(arg: Any, arg2:Any) {
        //Update: Not meant to go anywhere.
        //        Hashtable<String, Object> args = new Hashtable<>();
        //        args.put(ARG_CLAZZ_UID, currentClazzUid);
        //        args.put(ARG_PERSON_UID, arg);
        //        //Go somewhere ? To SELEdit maybe?
        //        //TODOne: this

    }

    /**
     * Handles what happens when the secondary button of every item on the SEL Answer List Recycler
     * adapter is clicked - It does nothing here.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    override fun handleSecondaryPressed(arg: Any) {
        // No secondary option here.
    }
}
