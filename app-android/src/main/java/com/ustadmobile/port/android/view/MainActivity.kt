package com.ustadmobile.port.android.view

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityMainBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.DbPreloadWorker
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.*
import com.ustadmobile.port.android.impl.nav.NavHostTempFileRegistrar
import com.ustadmobile.port.android.util.DeleteTempFilesNavigationListener
import com.ustadmobile.port.android.util.ext.registerDestinationTempFile
import com.ustadmobile.port.android.view.util.UstadActivityWithBottomNavigation
import com.ustadmobile.port.android.view.util.UstadActivityWithProgressBar
import kotlinx.coroutines.*
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.*
import com.ustadmobile.core.R as CR

class MainActivity : UstadBaseActivity(), UstadActivityWithFab,
        UstadActivityWithProgressBar,
        UstadActivityWithBottomNavigation,
        NavController.OnDestinationChangedListener,
        NavHostTempFileRegistrar,
        DIAware{


    private lateinit var appBarConfiguration: AppBarConfiguration

    override val activityFloatingActionButton: ExtendedFloatingActionButton?
        //Note: do not use mBinding here because it might not be ready for the first fragment
        get() = findViewById(R.id.activity_main_extendedfab)


    override val activityProgressBar: ProgressBar?
        //Note: do not use mBinding here because it might not be ready for the first fragment
        get() = findViewById(R.id.main_progress_bar)


    override val bottomNavigationView: BottomNavigationView
        get() = mBinding.bottomNavView


    private lateinit var mBinding: ActivityMainBinding

    private val impl : UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private lateinit var navController: NavController

    private lateinit var ustadNavController: UstadNavController

    private var mAccountIconVisible: Boolean? = null
        set(value) {
            if(field == value)
                return

            field = value
            invalidateOptionsMenu()
        }

    private val userProfileDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getDrawable(this, R.drawable.ic_account_circle_black_24dp)?.also {
            it.setTint(ContextCompat.getColor(this, R.color.onPrimaryColor))
        }
    }

    //This is actually managed by the underlying fragments.
    override var loading: Boolean
        get() = false
        set(@Suppress("UNUSED_PARAMETER") value) {}

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri = intent?.data?.toString() ?: intent?.getStringExtra(UstadView.ARG_OPEN_LINK)
        val argAccountName = intent?.getStringExtra(UstadView.ARG_ACCOUNT_NAME)

        if(uri != null){
            lifecycleScope.launchWhenResumed {
                ustadNavController.navigateToLink(uri, accountManager, { _, _ -> },
                    forceAccountSelection = true,
                    accountName = argAccountName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.mainCollapsingToolbar.toolbar)

        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.activity_main_navhost_fragment) as NavHostFragment? ?: return
        navController = host.navController

        navController.addOnDestinationChangedListener(this)
        navController.addOnDestinationChangedListener(DeleteTempFilesNavigationListener(this))
        appBarConfiguration = AppBarConfiguration(navController.graph)
        mBinding.bottomNavView.setupWithNavController(navController)



        setupActionBarWithNavController(navController,
            AppBarConfiguration(mBinding.bottomNavView.menu))

        DbPreloadWorker.queuePreloadWorker(applicationContext)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination,
                                      arguments: Bundle?) {

    }

    fun hideSoftKeyboard(){
        //Hide the soft keyboard if showing when moving to the next screen
        val currentFocusView = currentFocus
        if(currentFocusView != null) {
            ContextCompat.getSystemService(this, InputMethodManager::class.java)
                ?.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

    override fun registerNavDestinationTemporaryFile(file: File) {
        navController.registerDestinationTempFile(this, file)
    }

    fun onAppBarExpand(expand: Boolean){
        mBinding.mainCollapsingToolbar.appbar.setExpanded(expand)
    }

    fun slideBottomNavigation(visible: Boolean) {
        val layoutParams = (mBinding.bottomNavView.layoutParams as? CoordinatorLayout.LayoutParams)
        val bottomNavBehavior = layoutParams?.behavior as? HideBottomViewOnScrollBehavior

        if (visible)
            bottomNavBehavior?.slideUp(mBinding.bottomNavView)
        else
            bottomNavBehavior?.slideDown(mBinding.bottomNavView)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.activity_main_navhost_fragment).navigateUp()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_settings -> handleClickSettings()
            R.id.menu_share_offline -> {
                handleConfirmShareApp()
            }
        }
        return item.onNavDestinationSelected(findNavController(R.id.activity_main_navhost_fragment))
                || super.onOptionsItemSelected(item)
    }


    private fun handleConfirmShareApp() {
        GlobalScope.launch(Dispatchers.IO) {
            val apkFile = File(applicationContext.applicationInfo.sourceDir)
            val appName = applicationContext.getString(CR.string.app_name).replace(" ", "_")
            val baseName = "$appName.apk"

            val outDir = File(applicationContext.filesDir, "external")
            if (!outDir.isDirectory)
                outDir.mkdirs()

            val outFile: File

            var fileOutputStreamToClose: OutputStream? = null
            var apkFileIn: InputStream? = null
            try {
                apkFileIn = FileInputStream(apkFile)

                outFile = File(outDir, baseName)
                fileOutputStreamToClose = FileOutputStream(outFile)
                apkFileIn.copyTo(fileOutputStreamToClose)

                fileOutputStreamToClose.flush()
                fileOutputStreamToClose.close()
                apkFileIn.close()


                val applicationId = applicationContext.packageName
                val sharedUri = FileProvider.getUriForFile(applicationContext, "$applicationId.provider",
                    outFile)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("application/vnd.android.package-archive")
                shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (shareIntent.resolveActivity(applicationContext.packageManager) != null) {
                    applicationContext.startActivity(shareIntent)
                }else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, CR.string.error_this_device_doesnt_support_bluetooth_sharing,
                            Toast.LENGTH_LONG).show()
                    }
                }
                
            }catch(e: Exception) {
                e.printStackTrace()
            }finally {
                fileOutputStreamToClose?.flush()
                fileOutputStreamToClose?.close()
            }


        }
    }

    /**
     * When settings gear clicked in the menu options - Goes to the settings activity.
     */
    private fun handleClickSettings() {

    }

    private fun setUserProfile(menuItem: MenuItem) {
        val accountManager: UstadAccountManager by instance()

    }

}
