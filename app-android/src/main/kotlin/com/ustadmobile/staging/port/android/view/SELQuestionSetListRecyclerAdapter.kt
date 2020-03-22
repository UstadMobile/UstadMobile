package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionSetsPresenter
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions

class SELQuestionSetListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions>,
        internal var mPresenter: SELQuestionSetsPresenter,
        internal var theContext: Context)
    : PagedListAdapter<SELQuestionSetWithNumQuestions, SELQuestionSetListRecyclerAdapter.SELQuestionSetsViewHolder>(diffCallback) {
    internal var theActivity: Activity? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SELQuestionSetsViewHolder {

        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sel_question_set, parent, false)
        return SELQuestionSetsViewHolder(list)

    }

    override fun onBindViewHolder(holder: SELQuestionSetsViewHolder, position: Int) {

        val theQuestionSet = getItem(position)
        val questionTitle = holder.itemView.findViewById<TextView>(R.id
                .item_sel_question_set_question)
        val questionAmount = holder.itemView.findViewById<TextView>(R.id
                .item_sel_question_set_number_of_questions)

        questionTitle.setText(theQuestionSet!!.title)
        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id
                .item_sel_question_set_question_cl)
        cl.setOnClickListener{ view ->
            mPresenter.handleGoToQuestionSet(theQuestionSet.selQuestionSetUid,
                    theQuestionSet.title!!)
        }

        val numQuestions = theQuestionSet.numQuestions
        val quetionText: String
        if (numQuestions > 1) {
            quetionText = theContext.getText(R.string.question).toString()
        } else {
            quetionText = theContext.getText(R.string.questions).toString()
        }
        val numQuestionString = "$numQuestions $quetionText"
        questionAmount.setText(numQuestionString)
    }

     class SELQuestionSetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
