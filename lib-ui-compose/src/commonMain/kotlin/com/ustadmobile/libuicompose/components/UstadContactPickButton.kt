package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable


@Composable
expect fun UstadContactPickButton(

      onContactPicked:(String?)->Unit,
      onContactError:( String?)->Unit

)