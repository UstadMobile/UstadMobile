package com.ustadmobile.port.android.view
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonHandlerPresenter
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

/**
 * A Simple recycler adapter for a dead simple list of Students.
 */

class SimplePeopleListRecyclerAdapter : PagedListAdapter<Person,
        SimplePeopleListRecyclerAdapter.ClazzStudentViewHolder> {

    internal var theContext: Context
    internal lateinit var theFragment: Fragment
    internal lateinit var mPresenter: CommonHandlerPresenter<*>
    private var personPictureDaoRepo: PersonPictureDao?=null

    class ClazzStudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    protected constructor(diffCallback: DiffUtil.ItemCallback<Person>,
                          context: Context, fragment: Fragment) : super(diffCallback) {
        theContext = context
        theFragment = fragment
    }

    constructor(diffCallback: DiffUtil.ItemCallback<Person>,
                          context: Context, fragment: Fragment,
                          presenter: CommonHandlerPresenter<*>) : super(diffCallback) {
        theContext = context
        theFragment = fragment
        mPresenter = presenter
    }

    protected constructor(diffCallback: DiffUtil.ItemCallback<Person>,
                          context: Context) : super(diffCallback) {
        theContext = context
    }

    constructor(diffCallback: DiffUtil.ItemCallback<Person>,
                          context: Context, presenter: CommonHandlerPresenter<*>)
            : super(diffCallback) {
        theContext = context
        mPresenter = presenter
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzStudentViewHolder {

        val clazzStudentListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_peoplelist, parent, false)
        return ClazzStudentViewHolder(clazzStudentListItem)
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    override fun onBindViewHolder(
            holder: ClazzStudentViewHolder, position: Int) {


        val personPicture = holder.itemView.findViewById<ImageView>(R.id.item_peoplelist_imageview)
        val thisPerson = getItem(position)
        val studentName: String
        if (thisPerson == null) {
            studentName = "Student"
        } else {
            studentName = thisPerson.firstNames + " " + thisPerson.lastName
        }

        val studentEntry = holder.itemView
                .findViewById(R.id.item_peoplelist_name) as TextView
        studentEntry.text = studentName

        studentEntry.setOnClickListener {
            v -> mPresenter.handleCommonPressed(thisPerson!!.personUid) }

        var imgPath = ""
        GlobalScope.async(Dispatchers.Main) {

            personPictureDaoRepo =
                    UmAccountManager.getRepositoryForActiveAccount(theContext).personPictureDao
            val personPictureDao = UmAccountManager.getActiveDatabase(theContext).personPictureDao

            val personPictureLocal = personPictureDao.findByPersonUidAsync(thisPerson!!.personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureLocal!!)!!

            if (!imgPath.isEmpty())
                setPictureOnView(imgPath, personPicture!!)
            else
                personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)

            val personPictureEntity = personPictureDaoRepo!!.findByPersonUidAsync(
                    thisPerson.personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureEntity!!)!!

            if(personPictureLocal != personPictureEntity) {
                if (!imgPath.isEmpty())
                    setPictureOnView(imgPath, personPicture!!)
                else
                    personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)
            }
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

    companion object {

        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 26

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_PERSON_THUMBNAIL_WIDTH *
                    Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
