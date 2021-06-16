package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.DateRangeFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.lib.db.entities.DateRangeMoment

object DateRangeScreen : KScreen<DateRangeScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = DateRangeFragment::class.java

    val fromFixedRadio = KView { withId(R.id.date_range_fromRadio_fixedDate) }

    val fromRelRadio = KView { withId(R.id.date_range_fromRadio_relativeDate) }

    val toFixedRadio = KView { withId(R.id.date_range_toRadio_fixedDate) }

    val toRelRadio = KView { withId(R.id.date_range_toRadio_relativeDate) }

    val fromFixedDateTextInput = KTextInputLayout { withId(R.id.fragment_date_range_fixed_date_fromDate_textInputLayout) }

    val toFixedDateTextInput = KTextInputLayout { withId(R.id.fragment_date_range_fixed_date_toDate_textInputLayout) }

    val fromRelDateRelInput = KTextInputLayout { withId(R.id.fragment_date_range_relative_date_fromDateRel_textInputLayout) }
    val fromRelDateRelOffSetInput = KTextInputLayout { withId(R.id.fragment_date_range_fromDateRange_InputLayout) }
    val fromRelDateRelUnitInput = KTextInputLayout { withId(R.id.fragment_date_range_relative_date_fromDateRelUnit_textInputLayout) }

    val fromRelDateRelUnitTextView = KTextView { withId(R.id.fragment_date_range_relative_date_fromDateRelUnit_TextView)}

    val toRelDateRelInput = KTextInputLayout { withId(R.id.fragment_date_range_relative_date_toDateRel_textInputLayout) }
    val toRelDateRelOffSetInput = KTextInputLayout { withId(R.id.fragment_date_range_toDateRange_InputLayout) }
    val toRelDateRelUnitInput = KTextInputLayout { withId(R.id.fragment_date_range_relative_date_toDateRelUnit_textInputLayout) }



}