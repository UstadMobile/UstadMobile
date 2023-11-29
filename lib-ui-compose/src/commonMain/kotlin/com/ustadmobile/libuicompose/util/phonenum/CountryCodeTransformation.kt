package com.ustadmobile.libuicompose.util.phonenum

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.min

/**
 * Very simple transformation that will simply add a "+"
 */
class CountryCodeTransformation: VisualTransformation {

    private val transformedMaxLength = COUNTRY_CODE_MAX_LEN + 1

    private val offsetMapping = object: OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return min(offset + 1, transformedMaxLength)
        }

        override fun transformedToOriginal(offset: Int): Int {
            return min(
                if(offset > 0) offset - 1 else 0,
                COUNTRY_CODE_MAX_LEN
            )
        }
    }
    override fun filter(text: AnnotatedString): TransformedText {
        val output = buildAnnotatedString {
            append("+")
            append(text)
        }

        return TransformedText(output, offsetMapping)
    }

    companion object {
        val COUNTRY_CODE_MAX_LEN = 3

    }
}