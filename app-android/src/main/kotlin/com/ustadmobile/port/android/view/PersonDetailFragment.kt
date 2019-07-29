package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.Person

class PersonDetailFragment: UstadBaseFragment(), PersonDetailView {
    override fun finish() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateToolbarTitle(personName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateImageOnView(imagePath: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updatePersonOnView(person: Person) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val viewContext: Any
        get() =context!!

}