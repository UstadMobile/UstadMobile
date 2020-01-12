package com.ustadmobile.port.android.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonHandlerPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.util.*

/**
 * The Recycler adapter for blobs of people list used in SEL's recognition, selection, nomination
 * lists. A blob here is basically a square with the image of the person and an optional text.
 * Click handlers can be implemented via a common CommonHandlerPresenter.
 *
 */
class PeopleBlobListRecyclerAdapter : PagedListAdapter<PersonWithPersonPicture,
        PeopleBlobListRecyclerAdapter.PeopleViewHolder> {

    internal var theContext: Context
    internal var mPresenter: CommonHandlerPresenter<*>
    private var hideNames = false
    private var personPictureDaoRepo: PersonPictureDao?=null

    private val colorMap = Hashtable<Int, String>()

    class PeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal constructor(diffCallback: DiffUtil.ItemCallback<PersonWithPersonPicture>,
                         context: Context, presenter: CommonHandlerPresenter<*>) : super(diffCallback) {
        theContext = context
        mPresenter = presenter
    }

    internal constructor(diffCallback: DiffUtil.ItemCallback<PersonWithPersonPicture>,
                         context: Context, presenter: CommonHandlerPresenter<*>,
                         namesHidden: Boolean) : super(diffCallback) {
        theContext = context
        mPresenter = presenter
        hideNames = namesHidden
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     *
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {

        val clazzStudentListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_peopleblob, parent, false)
        return PeopleViewHolder(clazzStudentListItem)
    }

    /**
     * Transform a bitmap ?
     */
    inner class CropSquareTransformation : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            val size = Math.min(source.width, source.height)
            val x = (source.width - size) / 2
            val y = (source.height - size) / 2
            val result = Bitmap.createBitmap(source, x, y, size, size)
            if (result != source) {
                source.recycle()
            }
            return result
        }

        override fun key(): String {
            return "square()"
        }
    }

    /**
     * Updates image from the path given to it to the view.
     *
     * @param imagePath         The path of the image (local to the device)
     * @param personImageView   The View where to set the image.
     */
    private fun updateImageOnView(imagePath: String, personImageView: ImageView) {

        val profilePic = File(imagePath)
        Picasso.get()
                .load(profilePic)
                .transform(CropSquareTransformation())
                .resize(90, 90)
                .centerCrop()
                .into(personImageView)
    }

    /**
     * In Order:
     * 1.  Gets student name for every item, gets image and sets it to the card.
     * 2.  Depending on constructor, hides or shows the name
     * 3.  Adds an onClick listener on the item Card so that it changes color depending on the
     * selection.
     * 4.  Also calls presenter's primary action method (ie: for nominations)
     *
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {

        val thisPerson = getItem(position)

        val studentName: String
        if (thisPerson == null) {
            studentName = "Student"
        } else {
            studentName = thisPerson.firstNames + " " +
                    thisPerson.lastName
        }

        val studentImage = holder.itemView
                .findViewById<ImageView>(R.id.item_peopleblob_image)

        assert(thisPerson != null)

        var imgPath = ""
        GlobalScope.async(Dispatchers.Main) {

            personPictureDaoRepo =
                    UmAccountManager.getRepositoryForActiveAccount(theContext).personPictureDao
            val personPictureDao = UmAccountManager.getActiveDatabase(theContext).personPictureDao

            val personPictureLocal = personPictureDao.findByPersonUidAsync(thisPerson!!.personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureLocal!!)!!

            if (!imgPath.isEmpty())
                updateImageOnView(imgPath, studentImage!!)
            else
                studentImage.setImageResource(R.drawable.ic_person_black_new_24dp)

            val personPictureEntity = personPictureDaoRepo!!.findByPersonUidAsync(thisPerson.personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureEntity!!)!!

            if(personPictureLocal != personPictureEntity) {
                if (!imgPath.isEmpty())
                    updateImageOnView(imgPath, studentImage!!)
                else
                    studentImage.setImageResource(R.drawable.ic_person_black_new_24dp)
            }
        }

        val studentEntry = holder.itemView
                .findViewById<TextView>(R.id.item_peopleblob_name)

        if (!hideNames) {
            studentEntry.text = studentName
        } else {
            studentEntry.text = ""
        }

        val personCard = holder.itemView.findViewById<View>(R.id.item_peoplblob_card)

        if (colorMap != null && !colorMap.isEmpty &&
                colorMap.containsKey(position) && colorMap[position] == "selected") {
            if (hideNames) {
                studentEntry.text = studentName
            } else {
                personCard.setBackgroundColor(Color.parseColor("#FF6666"))
            }

        } else {
            if (hideNames) {
                studentEntry.text = ""
            } else {
                personCard.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }

        personCard.setOnClickListener { v ->

            if (colorMap.containsKey(position)) {
                if (colorMap[position] == "selected") {
                    if (hideNames) {
                        studentEntry.text = ""
                    } else {
                        personCard.setBackgroundColor(Color.parseColor("#FFFFFF"))

                    }
                    colorMap[position] = "unselected"
                } else {
                    if (hideNames) {
                        studentEntry.text = studentName
                    } else {
                        personCard.setBackgroundColor(Color.parseColor("#FF6666"))

                    }
                    colorMap[position] = "selected"
                }
            } else {
                colorMap[position] = "selected"
                if (hideNames) {
                    studentEntry.text = studentName
                } else {
                    personCard.setBackgroundColor(Color.parseColor("#FF6666"))
                }
            }


            mPresenter.handleCommonPressed(thisPerson!!.personUid)
        }


    }
}
