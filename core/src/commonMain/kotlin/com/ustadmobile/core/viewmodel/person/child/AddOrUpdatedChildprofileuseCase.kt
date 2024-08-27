package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.lib.db.entities.Person

class AddOrUpdatedChildProfileUseCase {

    operator fun invoke(
        currentList: List<Person>,
        addOrUpdateChildProfile: Person,
    ): List<Person> {
        val currentIndex = currentList.indexOfFirst {
            it.personUid == addOrUpdateChildProfile.personUid
        }

        val childProfileMutableList = currentList.toMutableList()

        if (currentIndex >= 0) {
            // Existing person: update the list
            childProfileMutableList[currentIndex] = addOrUpdateChildProfile
        } else {
            // New person: add to the list
            childProfileMutableList.add(addOrUpdateChildProfile)
        }

        // Return the updated list
        return childProfileMutableList.toList()
    }

}