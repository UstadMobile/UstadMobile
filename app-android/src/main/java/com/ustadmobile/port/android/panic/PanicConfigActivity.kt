package com.ustadmobile.port.android.panic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityPanicConfigBinding

/**
 * Basic wrapper activity that will simply show the PanicButtonSettings fragment (only). This is
 * required for PanicKit apps to bring a user directly to the config screen. It needs to be its own
 * activity because PanicKit does not support singleTop
 */
class PanicConfigActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mBinding = DataBindingUtil.setContentView<ActivityPanicConfigBinding>(this,
            R.layout.activity_panic_config)

        setSupportActionBar(mBinding.mainCollapsingToolbar.toolbar)
    }

    override fun onResume() {
        super.onResume()

        findNavController(R.id.activity_main_navhost_fragment).navigate(
            R.id.panic_button_settings_dest, bundleOf(), navOptions {
                popUpTo(R.id.redirect_dest) {
                    inclusive = true
                }
            })
    }
}