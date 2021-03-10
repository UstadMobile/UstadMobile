package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkquestionandoptionswithresponseBinding
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse

class ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter()
    : ListAdapter<ClazzWorkQuestionAndOptionWithResponse,
        ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter.ClazzWorkQuestionViewHolder>(
        ClazzWorkDetailOverviewFragment.DU_CLAZZWORKQUESTIONANDOPTIONWITHRESPONSE_EDIT) {

    class ClazzWorkQuestionViewHolder(val binding: ItemClazzworkquestionandoptionswithresponseBinding)
        : RecyclerView.ViewHolder(binding.root)

    private var viewHolder: ClazzWorkQuestionViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkQuestionViewHolder {
        val viewHolder = ClazzWorkQuestionViewHolder(
                ItemClazzworkquestionandoptionswithresponseBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        viewHolder.binding.freeTextType = ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT
        viewHolder.binding.quizType = ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE
        viewHolder.binding.clazzWorkQuizType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
        return viewHolder
    }

    override fun onBindViewHolder(holder: ClazzWorkQuestionViewHolder, position: Int) {
        holder.binding.clazzWorkQuestionAndOptionsWithResponse = getItem(position)
        holder.itemView.tag = getItem(position).clazzWorkQuestion.clazzWorkQuestionUid
        holder.binding.itemClazzworkquestionandoptionswithresponseAnswerEt.tag =
                getItem(position).clazzWorkQuestion.clazzWorkQuestionUid
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }
}