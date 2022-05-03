package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseDetailHeaderOverviewBinding
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toTermMap
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class CourseHeaderDetailRecyclerAdapter(val listener: ClazzDetailOverviewEventListener?, di: DI, val context: Any)
    : SingleItemRecyclerViewAdapter<CourseHeaderDetailRecyclerAdapter.CourseDetailViewHolder>(true) {

    class CourseDetailViewHolder(var itemBinding: ItemCourseDetailHeaderOverviewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    val json: Json by di.instance()

    val systemImpl: UstadMobileSystemImpl by di.instance()

    private var viewHolder: CourseDetailViewHolder? = null

    var clazz: ClazzWithDisplayDetails? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazz = value
            val termMap = value?.terminology.toTermMap(json, systemImpl, context)
            teacherStudentCount = """${clazz?.numTeachers ?: 0} ${termMap[TerminologyKeys.TEACHERS_KEY]}, ${clazz?.numStudents ?: 0} ${termMap[TerminologyKeys.STUDENTS_KEY]}"""
        }

    var teacherStudentCount: String? = null
        set(value){
            field = value
            viewHolder?.itemBinding?.teacherStudentCount = value
        }


    var clazzCodeVisible: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazzCodeVisible = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDetailViewHolder {
        viewHolder =  CourseDetailViewHolder(
            ItemCourseDetailHeaderOverviewBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
                it.clazz = clazz
                it.fragmentEventHandler = listener
                it.clazzCodeVisible = clazzCodeVisible
                it.teacherStudentCount = teacherStudentCount
            })
        return viewHolder as CourseDetailViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.clazz = clazz
        holder.itemBinding.clazzCodeVisible = clazzCodeVisible
        holder.itemBinding.teacherStudentCount = teacherStudentCount
    }
}