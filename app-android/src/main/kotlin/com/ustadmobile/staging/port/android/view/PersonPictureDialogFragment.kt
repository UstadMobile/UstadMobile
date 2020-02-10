package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonPictureDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.PersonPictureDialogView
import com.ustadmobile.core.view.PersonPictureDialogView.Companion.ARG_PERSON_IMAGE_PATH
import com.ustadmobile.port.android.view.UstadDialogFragment
import io.reactivex.annotations.NonNull
import java.io.File
import java.util.*

class PersonPictureDialogFragment : UstadDialogFragment(), PersonPictureDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    internal lateinit var theImage: ImageView
    internal lateinit var updateImageButton: Button
    internal var imagePath: String? = ""

    internal lateinit var mPresenter: PersonPictureDialogPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = Objects.requireNonNull<Context>(context).getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_person_picture_dialog, null)
        theImage = rootView.findViewById(R.id.fragment_person_picture_dialog_imageview)
        updateImageButton = rootView.findViewById(R.id.fragment_person_picture_dialog_update_picture_button)


        theImage = rootView.findViewById(R.id.fragment_person_picture_dialog_imageview)
        theImage.setOnClickListener { view -> dialog.cancel() }

        mPresenter = PersonPictureDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))

        if (arguments!!.containsKey(ARG_PERSON_IMAGE_PATH)) {
            imagePath = arguments!!.getString(ARG_PERSON_IMAGE_PATH)
            //Update image on Dialog
            setPictureOnView(imagePath!!)

        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(context!!)

        builder.setView(rootView)
        dialog = builder.create()
        dialog.setOnShowListener(this)

        Objects.requireNonNull<Window>(dialog.window).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        return dialog
    }


    override fun onClick(dialogInterface: DialogInterface, i: Int) {}

    override fun onShow(dialogInterface: DialogInterface) {}

    override fun onClick(view: View) {}

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {}

    override fun onNothingSelected(adapterView: AdapterView<*>) {}

    override fun finish() {}

    override fun setPictureOnView(imagePath: String) {

        val profileImage = Uri.fromFile(File(imagePath))

        Picasso
                .get()
                .load(profileImage)
                .fit()
                .centerCrop()
                .into(theImage)

    }
}
