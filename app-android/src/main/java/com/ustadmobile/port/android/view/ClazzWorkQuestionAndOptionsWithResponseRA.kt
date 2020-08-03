package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkquestionandoptionswithresponseBinding
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse

class ClazzWorkQuestionAndOptionsWithResponseRA(var studentMode: Boolean)
    : ListAdapter<ClazzWorkQuestionAndOptionWithResponse,
        ClazzWorkQuestionAndOptionsWithResponseRA.ClazzWorkQuestionViewHolder>(
        ClazzWorkDetailOverviewFragment.DU_CLAZZWORKQUESTIONANDOPTIONWITHRESPONSE) {

    class ClazzWorkQuestionViewHolder(val binding: ItemClazzworkquestionandoptionswithresponseBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkQuestionViewHolder {
        val viewHolder = ClazzWorkQuestionViewHolder(
                ItemClazzworkquestionandoptionswithresponseBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        viewHolder.binding.freeTextType = ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT
        viewHolder.binding.quizType = ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE
        viewHolder.binding.clazzWorkQuizType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
        viewHolder.binding.studentMode = studentMode?:false
        return viewHolder
    }

    override fun onBindViewHolder(holder: ClazzWorkQuestionViewHolder, position: Int) {
        holder.binding.clazzWorkQuestionAndOptionsWithResponse = getItem(position)
        holder.binding.studentMode = studentMode
        holder.itemView.tag = getItem(position).clazzWorkQuestion.clazzWorkQuestionUid
        holder.binding.itemClazzworkquestionandoptionswithresponseAnswerEt.tag =
                getItem(position).clazzWorkQuestion.clazzWorkQuestionUid
    }
}