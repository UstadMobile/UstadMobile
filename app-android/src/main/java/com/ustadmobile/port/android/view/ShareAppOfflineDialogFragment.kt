package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.ACTION_SEND
import android.widget.Toast
import androidx.core.content.FileProvider


class ShareAppOfflineDialogFragment : UstadDialogFragment(){

    lateinit var rootView: ViewGroup

    lateinit var zipCheckbox: CheckBox

    var zipIt: Boolean = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            as LayoutInflater
        rootView = inflater.inflate(R.layout.fragment_share_app_dialog, null) as ViewGroup
        zipCheckbox = rootView.findViewById(R.id.fragment_share_app_zip_checkbox)
        zipCheckbox.setOnClickListener { zipIt = zipCheckbox.isChecked }

        val builder = AlertDialog.Builder(requireContext())
                .setView(rootView)
                .setTitle(R.string.share_application)
                .setPositiveButton(R.string.share) {dialog, which ->
                    handleConfirmShareApp()

                }
                .setNegativeButton(R.string.cancel) {dialog, which ->
                    dismiss()
                }


        return builder.create()
    }


    fun handleConfirmShareApp() {
        GlobalScope.launch(Dispatchers.IO) {
            val ctx = rootView.context
            val apkFile = File(ctx.applicationInfo.sourceDir)
            val baseName = "Ustad.apk"

            val outDir = File(ctx.filesDir, "external")
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


                val applicationId = ctx.packageName
                val sharedUri = FileProvider.getUriForFile(ctx, "$applicationId.provider",
                        outFile)
                val shareIntent = Intent(ACTION_SEND)
                shareIntent.setType("application/vnd.android.package-archive")
                shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (shareIntent.resolveActivity(ctx.packageManager) != null) {
                    ctx.startActivity(shareIntent)
                }else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, R.string.error_this_device_doesnt_support_bluetooth_sharing,
                                Toast.LENGTH_LONG).show()
                    }
                }

                withContext(Dispatchers.Main) {
                    dismiss()
                }
            }catch(e: Exception) {
                e.printStackTrace()
            }finally {
                fileOutputStreamToClose?.flush()
                fileOutputStreamToClose?.close()
            }


        }
    }

}