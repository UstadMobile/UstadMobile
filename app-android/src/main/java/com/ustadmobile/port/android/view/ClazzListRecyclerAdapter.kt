package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzListCardBinding
import com.ustadmobile.core.controller.ClazzListItemListener
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class ClazzListRecyclerAdapter(var itemListener: ClazzListItemListener?, di: DI)
    : SelectablePagedListAdapter<ClazzWithListDisplayDetails,
        ClazzListRecyclerAdapter.ClazzList2ViewHolder>(DIFF_CALLBACK) {

    val json: Json by di.instance()

    class ClazzList2ViewHolder(val itemBinding: ItemClazzListCardBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzList2ViewHolder {
        val itemBinding = ItemClazzListCardBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)
        return ClazzList2ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzList2ViewHolder, position: Int) {
        val clazz = getItem(position)
        holder.itemBinding.clazz = clazz
        holder.itemView.tag = holder.itemBinding.clazz?.clazzUid
        holder.itemBinding.itemListener = itemListener

        val termMap =  clazz?.terminology?.ctTerminology?.let {
            json.decodeFromString(
                MapSerializer(String.serializer(), String.serializer()),
                it
            )
        } ?: mapOf()
        holder.itemBinding.teacherStudentCount = """${clazz?.numTeachers ?: 0} ${termMap[TerminologyKeys.TEACHERS_KEY]}, ${clazz?.numStudents ?: 0} ${termMap[TerminologyKeys.STUDENTS_KEY]}"""
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithListDisplayDetails> = object
            : DiffUtil.ItemCallback<ClazzWithListDisplayDetails>() {
            override fun areItemsTheSame(oldItem: ClazzWithListDisplayDetails,
                                         newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithListDisplayDetails,
                                            newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem.clazzName == newItem.clazzName &&
                        oldItem.numStudents == newItem.numStudents &&
                        oldItem.numTeachers == newItem.numTeachers &&
                        oldItem.clazzDesc == newItem.clazzDesc &&
                        oldItem.clazzActiveEnrolment?.clazzEnrolmentRole == newItem.clazzActiveEnrolment?.clazzEnrolmentRole &&
                        oldItem.attendanceAverage == newItem.attendanceAverage
            }
        }
    }

}