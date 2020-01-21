package com.ustadmobile.port.android.view


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonWithSaleInfoProfilePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.PersonWithSaleInfoProfileView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.staging.port.android.view.CircleTransform
import java.io.File

class PersonWithSaleInfoProfileFragment : UstadBaseFragment(), PersonWithSaleInfoProfileView {


    override val viewContext: Any
        get() = context!!


    private lateinit var rootContainer: View
    private var mPresenter: PersonWithSaleInfoProfilePresenter? = null

    private lateinit var notes: TextView
    private lateinit var phone: TextView
    private lateinit var address: TextView
    private lateinit var picture: ImageView

    private var imagePathFromCamera: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_personwithsaleinfo_profile, container, false)
        setHasOptionsMenu(true)

        notes = rootContainer.findViewById(R.id.fragment_personwithsaleinfo_profile_notes)
        picture = rootContainer.findViewById(R.id.fragment_personwithsaleinfo_profile_picture)
        address = rootContainer.findViewById(R.id.fragment_personwithsaleinfo_profile_address)
        phone = rootContainer.findViewById(R.id.fragment_personwithsaleinfo_profile_phone)

        //set up Presenter
        mPresenter = PersonWithSaleInfoProfilePresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        phone.setOnClickListener(View.OnClickListener {
            //TODO: Fire call intent
        })

        return rootContainer
    }

    override fun finish() {

    }

    override fun updatePersonOnView(person: Person) {
        if(person.personNotes != null && person.personNotes!!.isNotEmpty()) {
            notes.setText(person.personNotes)
        }
        if(person.phoneNum != null && person.phoneNum!!.isNotEmpty()) {
            phone.setText(person.phoneNum)
        }
        if(person.personAddress != null && person.personAddress!!.isNotEmpty()) {
            address.setText(person.personAddress)
        }
    }

    override fun updateImageOnView(imagePath: String) {
        imagePathFromCamera = imagePath
        val output = File(imagePath)

        val iconDimen = UserProfileActivity.dpToPx(150)

        if (output.exists()) {
            val profileImage = Uri.fromFile(output)

            runOnUiThread(Runnable {
                Picasso
                        .get()
                        .load(profileImage)
                        .transform(CircleTransform())
                        .resize(iconDimen, iconDimen)
                        .centerCrop()
                        .into(picture)

               //Any listeners of picture, go here
            })


        }
    }


    companion object {

        fun newInstance(): PersonWithSaleInfoProfileFragment {
            val fragment = PersonWithSaleInfoProfileFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(args:Bundle): PersonWithSaleInfoProfileFragment {
            val fragment = PersonWithSaleInfoProfileFragment()
            fragment.arguments = args
            return fragment
        }

    }


}
