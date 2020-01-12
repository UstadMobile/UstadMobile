package com.ustadmobile.port.android.view


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.clans.fab.FloatingActionButton
import com.toughra.ustadmobile.R

/**
 * A simple [Fragment] subclass.
 */
class ReportDashboard : UstadBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view:View =  inflater.inflate(R.layout.fragment_report_dashboard, container, false)

        view.findViewById<FloatingActionButton>(R.id.create_report).setOnClickListener {
            startActivity(Intent(activity, XapiReportOptionsActivity::class.java))
        }
        return  view
    }


}
