package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.toughra.ustadmobile.databinding.FragmentClazzWorkSubmissionWithPersonEditBinding
import com.ustadmobile.core.controller.ClazzWorkSubmissionMarkingPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberAndClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver


interface ClazzWorkSubmissionWithPersonEditFragmentEventHandler {

}

class ClazzWorkSubmissionMarkingFragment: UstadEditFragment<ClazzMemberAndClazzWorkWithSubmission>(),
        ClazzWorkSubmissionMarkingView, ClazzWorkSubmissionWithPersonEditFragmentEventHandler {

    private var mBinding: FragmentClazzWorkSubmissionWithPersonEditBinding? = null

    private var mPresenter: ClazzWorkSubmissionMarkingPresenter? = null

    private lateinit var dbRepo : UmAppDatabase

    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null

    private var quizQuestionsRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter? = null
    private val quizQuestionAndResponseObserver = Observer<List<ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizQuestionsRecyclerAdapter?.submitList(t)
    }

    override val mEditPresenter: UstadEditPresenter<*, ClazzMemberAndClazzWorkWithSubmission>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkSubmissionWithPersonEditBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())

        mPresenter = ClazzWorkSubmissionMarkingPresenter(requireContext(), arguments.toStringMap(),
                this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        quizQuestionsRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter(
                mPresenter, true)

        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }
        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        privateCommentsRecyclerAdapter = null
    }

    override fun onResume() {
        super.onResume()
        //setEditFragmentTitle(R.string.clazzworksubmissionwithperson)
    }

    override var entity: ClazzMemberAndClazzWorkWithSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWorkSubmissionWithPerson = value
        }

    override var privateCommentsToPerson: DataSource.Factory<Int, CommentsWithPerson>? = null
        get() = field
        set(value) {
            val privateCommentsObserverVal = privateCommentsObserver?:return
            privateCommentsLiveData?.removeObserver(privateCommentsObserverVal)
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            privateCommentsLiveData?.observe(viewLifecycleOwner, privateCommentsObserverVal)
        }


    override var clazzWorkQuizQuestionsAndOptionsWithResponse
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        get() = field
        set(value) {
            field?.removeObserver(quizQuestionAndResponseObserver)
            field = value
            value?.observe(viewLifecycleOwner, quizQuestionAndResponseObserver)
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}