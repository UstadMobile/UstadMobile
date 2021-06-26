package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.mocks.DoorLiveDataJs

class PictureDaoJs: PersonPictureDao() {

    val profileImage = "https://littleconstruction.net/wp-content/uploads/2019/04/person-placeholder-male-5-768x768.jpg"
    override suspend fun findByPersonUidAsync(personUid: Long): PersonPicture? {
        return PersonPicture().apply {
            personPictureUri = profileImage
        }
    }

    override fun findByPersonUidLive(personUid: Long): DoorLiveData<PersonPicture?> {
        return DoorLiveDataJs(PersonPicture().apply {
            personPictureUri = profileImage
        }) as DoorLiveData<PersonPicture?>
    }

    override suspend fun updateAsync(personPicture: PersonPicture) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: PersonPicture): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: PersonPicture): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<PersonPicture>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<PersonPicture>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: PersonPicture) {
        TODO("Not yet implemented")
    }
}