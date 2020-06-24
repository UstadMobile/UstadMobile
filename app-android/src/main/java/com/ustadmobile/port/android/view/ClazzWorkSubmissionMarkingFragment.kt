package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkSubmissionMarkingBinding
import com.ustadmobile.core.controller.ClazzWorkSubmissionMarkingPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver


class ClazzWorkSubmissionMarkingFragment: UstadEditFragment<ClazzMemberAndClazzWorkWithSubmission>(),
        ClazzWorkSubmissionMarkingView{

    internal var mBinding: FragmentClazzWorkSubmissionMarkingBinding? = null

    private var mPresenter: ClazzWorkSubmissionMarkingPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzMemberAndClazzWorkWithSubmission>?
        get() = mPresenter

    private lateinit var dbRepo : UmAppDatabase

    override val viewContext: Any
        get() = requireContext()

    private var submissionHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var submissionResultRecyclerAdapter
            : ClazzWorkDetailOverviewFragment.SubmissionResultRecyclerAdapter? = null

    private var markingEditRecyclerAdapter
            : ClazzWorkSubmissionScoreEditRecyclerAdapter? = null
    private var submissionFreeTextRecyclerAdapter
            : ClazzWorkDetailOverviewFragment.SubmissionTextEntryWithResultRecyclerAdapter? = null

    private var markingHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var questionsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var quizQuestionsRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter? = null
    private val quizQuestionAndResponseObserver = Observer<List<ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizQuestionsRecyclerAdapter?.submitList(t)
    }

    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsMergerRecyclerAdapter: MergeAdapter? = null

    private var detailMergerRecyclerAdapter: MergeAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkSubmissionMarkingBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())

        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_submission_marking_rv)

        mPresenter = ClazzWorkSubmissionMarkingPresenter(requireContext(), arguments.toStringMap(),
                this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        val clazzWorkWithSubmission: ClazzWorkWithSubmission =
                ClazzWorkWithSubmission().generateWithClazzWorkAndClazzWorkSubmission(
                        entity?.clazzWork?: ClazzWork(), entity?.submission
                )
        submissionResultRecyclerAdapter =
                ClazzWorkDetailOverviewFragment.SubmissionResultRecyclerAdapter(
                        clazzWorkWithSubmission)
        submissionResultRecyclerAdapter?.visible = false

        markingEditRecyclerAdapter =
                ClazzWorkSubmissionScoreEditRecyclerAdapter(clazzWorkWithSubmission)
        markingEditRecyclerAdapter?.visible = false

        submissionFreeTextRecyclerAdapter =
                ClazzWorkDetailOverviewFragment.SubmissionTextEntryWithResultRecyclerAdapter(
                        clazzWorkWithSubmission)
        submissionFreeTextRecyclerAdapter?.visible = true

        submissionHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.submission).toString())
        submissionHeadingRecyclerAdapter?.visible = true

        questionsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.questions).toString())
        questionsHeadingRecyclerAdapter?.visible = true

        markingHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.marking).toString())
        markingHeadingRecyclerAdapter?.visible = true

        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        )
        privateCommentsHeadingRecyclerAdapter?.visible = true


        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(mPresenter,
                requireContext().getString(R.string.add_private_comment), false, ClazzWork.CLAZZ_WORK_TABLE_ID,
                entity?.clazzWork?.clazzWorkUid?:0L, entity?.clazzMemberPersonUid?:0L,
                UmAccountManager.getActivePersonUid(requireContext())
        )
        newPrivateCommentRecyclerAdapter?.visible = true

        quizQuestionsRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter(
                false)

        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        privateCommentsMergerRecyclerAdapter = MergeAdapter(newPrivateCommentRecyclerAdapter,
                privateCommentsRecyclerAdapter)

        detailMergerRecyclerAdapter = MergeAdapter(
                submissionHeadingRecyclerAdapter, submissionFreeTextRecyclerAdapter,
                questionsHeadingRecyclerAdapter, quizQuestionsRecyclerAdapter,
                markingHeadingRecyclerAdapter, markingEditRecyclerAdapter,
                privateCommentsHeadingRecyclerAdapter, privateCommentsMergerRecyclerAdapter
        )
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.marking)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
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
        setEditFragmentTitle(R.string.marking)
    }

    override var entity: ClazzMemberAndClazzWorkWithSubmission? = null
        get() = field
        set(value) {
            field = value

            newPrivateCommentRecyclerAdapter?.entityUid = value?.clazzWork?.clazzWorkUid?:0L
            newPrivateCommentRecyclerAdapter?.commentTo = value?.clazzMemberPersonUid?:0L
            newPrivateCommentRecyclerAdapter?.commentFrom = UmAccountManager.getActivePersonUid(requireContext())
            newPrivateCommentRecyclerAdapter?.visible = true


            val clazzWorkWithSubmission: ClazzWorkWithSubmission =
                    ClazzWorkWithSubmission().generateWithClazzWorkAndClazzWorkSubmission(
                            entity?.clazzWork?: ClazzWork(), entity?.submission
                    )

            submissionResultRecyclerAdapter?.submitList(listOf(clazzWorkWithSubmission))
            markingEditRecyclerAdapter?.submitList(listOf(clazzWorkWithSubmission))


            if(entity?.clazzWork?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT ){
                submissionFreeTextRecyclerAdapter?.submitList(listOf(clazzWorkWithSubmission))
                submissionFreeTextRecyclerAdapter?.visible = true
            }else{
                submissionFreeTextRecyclerAdapter?.visible = false
            }

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

    override var fieldsEnabled: Boolean = true
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

}