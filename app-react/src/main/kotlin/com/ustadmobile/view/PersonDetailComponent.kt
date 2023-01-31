package com.ustadmobile.view

import com.ustadmobile.door.DoorMediatorLiveData
import com.ustadmobile.door.lifecycle.Observer
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.view.components.AttachmentImageLookupAdapter

class PersonDetailComponent() {


    companion object {

        val PERSON_PICTURE_LOOKUP_ADAPTER = AttachmentImageLookupAdapter { db, entityUid ->
            object: DoorMediatorLiveData<String?>(), Observer<PersonPicture?> {
                init {
                    addSource(db.personPictureDao.findByPersonUidLive(entityUid), this)
                }
                override fun onChanged(t: PersonPicture?) {
                    postValue(t?.personPictureUri)
                }
            }
        }

    }

}