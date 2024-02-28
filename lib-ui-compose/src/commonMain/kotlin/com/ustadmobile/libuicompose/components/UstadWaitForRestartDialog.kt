package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.process.CloseProcessUseCase
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import org.kodein.di.compose.localDI
import org.kodein.di.instance

@Composable
fun UstadWaitForRestartDialog(){
    var showManualRestart: Boolean by remember {
        mutableStateOf(false)
    }
    val di = localDI()
    val closeProcessUseCase: CloseProcessUseCase by di.instance()

    Dialog(
        onDismissRequest = {
            //Do nothing - this dialog goes away once the restart happens
        }
    ) {
        LaunchedEffect(Unit) {
            delay(5_000)
            showManualRestart = true
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if(!showManualRestart) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(MR.strings.restarting)
                )
            }else {
                Column {
                    Text(stringResource(MR.strings.could_not_restart), modifier = Modifier.padding(16.dp))

                    OutlinedButton(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        onClick = {
                            closeProcessUseCase()
                        }
                    ) {
                        Text(stringResource(MR.strings.close_now))
                    }
                }
            }


        }

    }
}