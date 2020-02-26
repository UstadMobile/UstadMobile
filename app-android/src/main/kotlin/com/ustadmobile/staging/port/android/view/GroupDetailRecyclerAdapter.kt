package com.ustadmobile.staging.port.android.view


import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.GroupDetailPresenter
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

class GroupDetailRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>,
        internal var mPresenter: GroupDetailPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<PersonWithEnrollment, GroupDetailRecyclerAdapter.GroupDetailViewHolder>(diffCallback) {

    private var personPictureDaoRepo: PersonPictureDao?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupDetailViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_image_with_title_with_desc_and_dots, parent, false)
        return GroupDetailViewHolder(list)

    }

    override fun onBindViewHolder(holder: GroupDetailViewHolder, position: Int) {

        val personWithEnrollment = getItem(position)

        val studentNameTextView = holder.itemView.findViewById<TextView>(R.id
                .item_image_with_title_with_desc_and_dots_title)
        val lastActiveTextView = holder.itemView.findViewById<TextView>(R.id
                .item_image_with_title_with_desc_and_dots_desc)
        val personPicture = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_image_with_title_with_desc_and_dots_image)
        val menu = holder.itemView.findViewById<AppCompatImageView>(
                R.id.item_image_with_title_with_desc_and_dots_dots)


        if (personWithEnrollment == null) {
            return
        }

        val studentName = personWithEnrollment.fullName(
                UstadMobileSystemImpl.instance.getLocale(holder.itemView.context)
        )
        studentNameTextView.setText(studentName)
        val personUid = personWithEnrollment.personUid
        studentNameTextView.setOnClickListener({ v -> mPresenter.handleClickStudent(personUid) })

        var imgPath = ""
        GlobalScope.async(Dispatchers.Main) {

            personPictureDaoRepo =
                    UmAccountManager.getRepositoryForActiveAccount(theContext).personPictureDao
            val personPictureDao = UmAccountManager.getActiveDatabase(theContext).personPictureDao

            val personPictureLocal = personPictureDao.findByPersonUidAsync(personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureLocal!!)!!

            if (!imgPath!!.isEmpty())
                setPictureOnView(imgPath, personPicture!!)
            else
                personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)

            val personPictureEntity = personPictureDaoRepo!!.findByPersonUidAsync(personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureEntity!!)!!

            if(personPictureLocal != personPictureEntity) {
                if (!imgPath!!.isEmpty())
                    setPictureOnView(imgPath, personPicture!!)
                else
                    personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)
            }
        }

        //Last Seen
        //TODO: Get the last active value from the database.
        lastActiveTextView.setText("")

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity, v)
            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteMember(personUid)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            popup.menu.findItem(R.id.edit).isVisible = false

            //displaying the popup
            popup.show()
        }

    }

    private fun setPictureOnView(imagePath: String, theImage: ImageView) {

        val imageUri = Uri.fromFile(File(imagePath))

        Picasso
                .get()
                .load(imageUri)
                .resize(dpToPxImagePerson(), dpToPxImagePerson())
                .noFade()
                .into(theImage)
    }


    inner class GroupDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {

        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 26

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_PERSON_THUMBNAIL_WIDTH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
