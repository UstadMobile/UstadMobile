package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionSetDetailPresenter
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.lib.db.entities.SelQuestion

class SELQuestionRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<SelQuestion>,
        internal var theContext: Context,
        internal var theActivity: Activity,
        private val mPresenter: SELQuestionSetDetailPresenter)
    : PagedListAdapter<SelQuestion, SELQuestionRecyclerAdapter.SELQuestionViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SELQuestionViewHolder {

        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sel_question, parent, false)
        return SELQuestionViewHolder(list)

    }

    override fun onBindViewHolder(holder: SELQuestionViewHolder, position: Int) {

        val theQuestion = getItem(position)
        val questionTitle = holder.itemView.findViewById<TextView>(R.id.item_sel_question_title)
        val questionType = holder.itemView.findViewById<TextView>(R.id.item_sel_question_type)

        questionTitle.setText(theQuestion!!.questionText)

        when (theQuestion.questionType) {
            SelQuestionDao
                    .SEL_QUESTION_TYPE_NOMINATION -> questionType.setText(theActivity.getText(R.string.sel_question_type_nomination))
            SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE -> questionType.setText(theActivity.getText(R.string.sel_question_type_multiple_choise))
            SelQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT -> questionType.setText(theActivity.getText(R.string.sel_question_type_free_text))
            else -> {
            }
        }

        val theWholeThang = holder.itemView.findViewById<ConstraintLayout>(R.id.item_sel_question_cl)
        theWholeThang.setOnClickListener(View.OnClickListener{ view ->
            if (theQuestion != null)
                mPresenter.goToQuestionDetail(theQuestion)
        })

        //Options to Edit/Delete every schedule in the list
        val optionsImageView = holder.itemView.findViewById<AppCompatImageView>(R.id.item_sel_question_secondary_menu_imageview)
        optionsImageView.setOnClickListener(View.OnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleQuestionEdit(theQuestion)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleQuestionDelete(theQuestion.selQuestionUid)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            //displaying the popup
            popup.show()
        })

    }

    class SELQuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
