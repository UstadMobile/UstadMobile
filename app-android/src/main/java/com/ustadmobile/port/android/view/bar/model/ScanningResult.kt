package com.ustadmobile.port.android.view.bar.model

import android.graphics.RectF
import com.google.mlkit.vision.barcode.common.Barcode

data class BarCodeResult(
    val barCode: Barcode? = null,
    val globalPosition: RectF = RectF(), // transformed position of the barcode rectangle
    val message: String = "", // transformed position of the barcode rectangle
)
