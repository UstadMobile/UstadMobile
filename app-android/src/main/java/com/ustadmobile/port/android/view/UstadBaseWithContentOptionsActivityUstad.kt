package com.ustadmobile.port.android.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.UstadViewWithProgressDialog
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit


open class UstadBaseWithContentOptionsActivity : UstadBaseActivity(),
        ContentEntryEditFragment.EntryCreationActionListener, UstadViewWithProgressDialog {


    internal var coordinatorLayout: CoordinatorLayout? = null

    /**
     * TODO: the below is a leak
     */
    private var entryFragment: ContentEntryEditFragment? = null

    private var impl: UstadMobileSystemImpl? = null

    internal lateinit  var importDialog: ProgressDialog


    @SuppressLint("StaticFieldLeak") // this is a short lived task, so any leak would not be very short lived.
    inner class HandleFileSelectionAsyncTask(private val onDone: UmResultCallback<String>?) : AsyncTask<Uri, Void, String>() {

        override fun doInBackground(vararg fileUris: Uri): String? {
            var cursor: Cursor? = null
            var fileIn: InputStream? = null
            var tmpOut: OutputStream? = null
            var tmpFilePath: String? = null

            try {
                //As per https://developer.android.com/guide/topics/providers/document-provider
                cursor = contentResolver.query(fileUris[0], null, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val displayName = cursor.getString(cursor
                            .getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val extension = UMFileUtil.getExtension(displayName)

                    val tmpFile = File.createTempFile("SelectedFileTmp",
                            "-" + System.currentTimeMillis() + "." + extension)
                    fileIn = contentResolver.openInputStream(fileUris[0])
                    tmpOut = FileOutputStream(tmpFile)
                    UMIOUtils.readFully(fileIn!!, tmpOut)
                    tmpFilePath = tmpFile.absolutePath
                } else {
                    runOnUiThread { showSnackMessage(resources.getString(R.string.error_opening_file)) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { showSnackMessage(resources.getString(R.string.error_opening_file)) }
            } finally {
                cursor?.close()

                UMIOUtils.closeInputStream(fileIn)
                UMIOUtils.closeOutputStream(tmpOut)
            }

            return tmpFilePath
        }

        override fun onPostExecute(filePath: String) {
            super.onPostExecute(filePath)

            onDone?.onDone(filePath)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        importDialog = ProgressDialog(this)
        importDialog.setMessage(getString(R.string.content_entry_importing))
        importDialog.setCancelable(false)
        impl = UstadMobileSystemImpl.instance
    }

    override fun browseFiles(callback: UmResultCallback<String>?, vararg mimeType: String) {
        runAfterGrantingPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Runnable{
                    runAfterFileSection(Runnable{
                        runOnUiThread{importDialog.show()}
                        HandleFileSelectionAsyncTask(callback ?: object : UmResultCallback<String> {
                            override fun onDone(result: String?) {
                                entryFragment!!.checkIfIsSupportedFile(File(result as String))
                            }
                        }).execute(selectedFileUri)
                    }, *mimeType)
                },
                getString(R.string.download_storage_permission_title),
                getString(R.string.download_storage_permission_message))
    }


    fun showBaseMessage(message: String) {
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_LONG).show()
    }


    override fun showSnackMessage(message: String) {
        Handler().postDelayed({
            Snackbar.make(coordinatorLayout!!, message,
                    Snackbar.LENGTH_LONG).show()
        }, TimeUnit.SECONDS.toMillis(1))
    }

    override fun updateDocument(title: String, description: String) {}

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is ContentEntryEditFragment) {
            entryFragment = fragment
            entryFragment!!.setActionListener(this)
        }
    }

    override fun showProgressDialog(show: Boolean) {
        if(show){
            importDialog.show()
        }else{
            importDialog.dismiss()
        }
    }

}
