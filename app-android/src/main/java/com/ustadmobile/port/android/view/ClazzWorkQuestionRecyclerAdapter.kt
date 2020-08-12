package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkquestionBinding
import com.ustadmobile.core.controller.ClazzWorkEditPresenter
import com.ustadmobile.core.controller.ClazzWorkQuestionAndOptionsEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions

class ClazzWorkQuestionRecyclerAdapter(
        val activityEventHandler: ClazzWorkEditFragmentEventHandler,
        var presenter: ClazzWorkEditPresenter?)
    : ListAdapter<ClazzWorkQuestionAndOptions,
        ClazzWorkQuestionRecyclerAdapter.ClazzWorkQuestionViewHolder>(
            ClazzWorkEditFragment.DIFF_CALLBACK_CLAZZ_WORK_QUESTION_OPTION) {

    class ClazzWorkQuestionViewHolder(val binding: ItemClazzworkquestionBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkQuestionViewHolder {
        val viewHolder = ClazzWorkQuestionViewHolder(ItemClazzworkquestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        viewHolder.binding.mPresenter = presenter
        viewHolder.binding.mActivity = activityEventHandler
        viewHolder.binding.questionTypeList =
                ClazzWorkQuestionAndOptionsEditPresenter.ClazzWorkQuestionOptions.values()
                        .map { MessageIdOption(it.messageId, parent.context, it.optionVal) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ClazzWorkQuestionViewHolder, position: Int) {
        holder.binding.clazzWorkQuestionAndOptions = getItem(position)
    }
}