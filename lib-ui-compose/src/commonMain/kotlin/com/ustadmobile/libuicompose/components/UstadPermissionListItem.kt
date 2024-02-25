package com.ustadmobile.libuicompose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.ext.filterByFlags
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadPermissionListItem(
    value: Long,
    permissionLabels: List<Pair<StringResource, Long>>,
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    toPerson: Person? = null,
    toPersonPicture: PersonPicture? = null,
) {
    val permissionsStr = permissionLabels.filterByFlags(value).fold("") { acc, next ->
        "$acc,${stringResource(next.first)}"
    }

    ListItem(
        modifier = modifier,
        headlineContent = headlineContent,
        supportingContent = {
            Text(permissionsStr)
        },
        leadingContent = {
            if(toPerson != null) {
                UstadPersonAvatar(
                    personUid = toPerson.personUid,
                    pictureUri = toPersonPicture?.personPictureThumbnailUri,
                    personName = toPerson.fullName(),
                )
            }else {
                Icon(
                    Icons.Default.Group, contentDescription = null
                )
            }
        }
    )
}