package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionDetail2Presenter
import com.ustadmobile.lib.db.entities.SelQuestionOption

class SELQuestionOptionRecyclerAdapter
//The presenter mPresenter
(
        diffCallback: DiffUtil.ItemCallback<SelQuestionOption>,
        internal var theContext: Context, internal var theActivity: Activity, internal var mPresenter: SELQuestionDetail2Presenter
) : PagedListAdapter<SelQuestionOption, SELQuestionOptionRecyclerAdapter.SELQuestionOptionViewHolder>(diffCallback) {

    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): SELQuestionOptionViewHolder {
        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sel_question_option, parent, false)
        return SELQuestionOptionViewHolder(list)

    }

    override fun onBindViewHolder(holder: SELQuestionOptionViewHolder, position: Int) {
        val questionOption = getItem(position)
        val questionOptionTitle = holder.itemView.findViewById<TextView>(R.id
                .item_sel_question_option_text)
        questionOptionTitle.setText(questionOption!!.optionText)

        val optionsImageView = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_sel_question_option_secondary_menu_imageview)
        optionsImageView.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleQuestionOptionEdit(
                            questionOption.selQuestionOptionUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleQuestionOptionDelete(
                            questionOption.selQuestionOptionUid)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            //displaying the popup
            popup.show()
        }

    }

    class SELQuestionOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
