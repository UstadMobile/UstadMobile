package com.ustadmobile.libuicompose.view.respect.respectapp.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.entities.respect.RespectApp
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Composable
fun RespectAppCard(
    respectApp: RespectApp?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.Black),
        onClick = {

        },
        modifier = modifier,
    ) {
        Column {
            Text(
                text = respectApp?.raName ?: "",
                modifier = Modifier.defaultItemPadding(),
            )
        }
    }
}