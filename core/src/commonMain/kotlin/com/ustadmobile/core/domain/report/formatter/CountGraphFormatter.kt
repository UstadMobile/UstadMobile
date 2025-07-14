package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.impl.locale.UiText

/**
 * Base formatter for count values (simple numeric display)
 */
class CountGraphFormatter : GraphFormatter<Double> {
    override fun adjust(value: Double): Double = value

    override fun format(value: Double): UiText {
        return StringUiText(value.toInt().toString())
    }
}