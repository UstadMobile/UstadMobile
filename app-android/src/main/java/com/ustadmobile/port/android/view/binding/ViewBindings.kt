package com.ustadmobile.port.android.view.binding

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValue
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.port.android.view.util.SelectableViewHelper


@BindingAdapter("android:layout_marginTop")
fun View.setMarginTopValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { topMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginBottom")
fun View.setMarginBottomValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { bottomMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginStart")
fun View.setMarginStartValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { leftMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginEnd")
fun View.setMarginEndValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { rightMargin = marginValue.toInt() }

@BindingAdapter("onLongPress")
fun View.setOnLongPress(onLongClick: View.OnClickListener) {
    setOnLongClickListener {
        onLongClick.onClick(this)

        true
    }
}

@BindingAdapter("courseBlockIndent")
fun View.setIndentLevel(indentLevel: Int){
    setMarginStartValue(indentLevel * context.resources.getDimension(R.dimen.dimen_32dp))
    // required to update thew view
    requestLayout()
}

/**
 * Binder Adapter that will trigger dialing a number when clicked. Can be useful for binding on
 * detail views.
 */
@BindingAdapter("onClickDial")
fun View.setOnClickDial(numberToDial: String?) {
    if(numberToDial == null)
        return //can't do anything

    setOnClickListener {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            setData(Uri.parse("tel:$numberToDial"))
        }

        if(callIntent.resolveActivity(it.context.packageManager) != null)
            it.context.startActivity(callIntent)
    }
}

@BindingAdapter("onClickSms")
fun View.setOnClickSms(numberToSms: String?) {
    if(numberToSms == null)
        return

    setOnClickListener {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$numberToSms")
        }

        if(smsIntent.resolveActivity(it.context.packageManager) != null) {
            it.context.startActivity(smsIntent)
        }
    }
}

@BindingAdapter("onClickEmail")
fun View.setOnClickEmail(emailAddr: String?) {
    if(emailAddr == null)
        return

    setOnClickListener {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddr))
            data = Uri.parse("mailto:$emailAddr")
        }

        if(emailIntent.resolveActivity(it.context.packageManager) != null) {
            it.context.startActivity(emailIntent)
        }
    }

}


internal class CustomFieldOnClickListener(val customField: CustomField, val customFieldValue: CustomFieldValue?): View.OnClickListener {
    override fun onClick(v: View) {
        when(customField.actionOnClick) {
            CustomField.ACTION_CALL -> {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    setData(Uri.parse("tel:${customFieldValue?.customFieldValueValue}"))
                }
                v.context.startActivity(callIntent)

            }
            CustomField.ACTION_EMAIL -> {
                val emailAddr = customFieldValue?.customFieldValueValue ?: return
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddr))
                    data = Uri.parse("mailto:")
                }
                if (emailIntent.resolveActivity(v.context.packageManager) != null) {
                    v.context.startActivity(emailIntent)
                }
            }
        }
    }
}

@BindingAdapter(value = ["onClickCustomField", "onClickCustomFieldValue"])
fun View.setOnClickCustomFieldHandler(customField: CustomField?, customFieldValue: CustomFieldValue) {
    val actionOnClick = customField?.actionOnClick
    if(customField != null && actionOnClick != null) {
        setOnClickListener(CustomFieldOnClickListener(customField, customFieldValue))
    }
}

interface OnSelectionStateChangedListener {
    fun onSelectionStateChanged(view: View)
}

@BindingAdapter(value=["isActiveEnrolment"])
fun View.setAlphaIfActiveEnrolment(person: PersonWithClazzEnrolmentDetails){
    val currentTime = systemTimeInMillis()
    alpha = if(currentTime >= person.earliestJoinDate && currentTime <= person.latestDateLeft){
        1f
    }else{
        0.5f
    }
}


/**
 * Convenience binder for handling events with a selectable view (e.g. an item in a list).
 */
@BindingAdapter(value = ["selectableViewHelper", "onSelectableItemClicked", "onSelectedStateChanged"], requireAll = false)
fun <T> View.setSelectableViewHelper(selectableViewHelper: SelectableViewHelper?,
                                     onSelectableItemClicked: View.OnClickListener?,
                                     onSelectedStateChanged: OnSelectionStateChangedListener?) {

    setOnClickListener {
        if(selectableViewHelper?.isInSelectionMode != true) {
            onSelectableItemClicked?.onClick(it)
        }else {
            it.isSelected = !it.isSelected
            onSelectedStateChanged?.onSelectionStateChanged(it)
        }
    }

    setOnLongClickListener {
        it.isSelected = !it.isSelected
        onSelectedStateChanged?.onSelectionStateChanged(it)
        true
    }
}

@BindingAdapter("linearLayoutWeight")
fun View.setLinearLayoutWeight(weight: Float) {
    val layoutParamsVal = this.layoutParams
    if(layoutParamsVal is LinearLayout.LayoutParams) {
        layoutParams = layoutParamsVal.also { it.weight = weight }
    }
}

private var viewAnimationEnabled: Boolean? = null

/**
 * Unfortunately using selectableItemBackground on Espresso tests seems to cause a nullpointerexception
 * See:
 * https://stackoverflow.com/questions/56817825/android-graphics-drawable-drawable-isprojected-on-a-null-object-reference
 *
 * Previously we looked at settings to see if animations were enabled. This caused a super-crash
 * on some devices and emulators that appeared to be impossible to catch.
 */
@BindingAdapter("backgroundIfAnimationEnabled")
fun View.backgroundIfAnimated(drawable: Drawable) {
    if(viewAnimationEnabled == null) {
        try {
            Class.forName("com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule")
            viewAnimationEnabled = false
        }catch (e: ClassNotFoundException) {
            viewAnimationEnabled = true
        }
    }

    if(viewAnimationEnabled == true)
        background = drawable
}


@BindingAdapter("percentageHeight")
fun View.setPercentageHeight(percentHeight: Float) {
    val params = layoutParams as ConstraintLayout.LayoutParams
    params.matchConstraintPercentHeight = percentHeight
    layoutParams = params
}

@BindingAdapter("showScoreVisibility")
fun View.setScoreVisibility(score: ContentEntryStatementScoreProgress?){
    visibility = if(score == null || (score.resultScore == 0 && score.progress == 0)) View.GONE else View.VISIBLE
}
