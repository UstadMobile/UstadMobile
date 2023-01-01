package com.ustadmobile.port.android.panic

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class NotepadActivity : AppCompatActivity(), DIAware{

    override val di: DI by closestDI()

    private val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val unhideCode = HidingManager().getUnhideCode(systemImpl, this)

        setContentView(R.layout.activity_panic_responder)
        findViewById<EditText>(R.id.activity_notepad_edittext).addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                if(text.toString().trim() ==unhideCode) {
                    HidingManager().unhide(this)
                }
            }
        )
    }


}