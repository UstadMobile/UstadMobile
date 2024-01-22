package com.ustadmobile.libuicompose.util.phonenum


//As per
// https://github.com/jump-sdk/jetpack_compose_country_code_picker_emoji/blob/master/ccp/src/main/java/com/togitech/ccp/transformation/PhoneNumberTransformation.kt

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.ustadmobile.core.domain.phonenumber.IAsYouTypeFormatter
import kotlin.math.max

// https://medium.com/google-developer-experts/hands-on-jetpack-compose-visualtransformation-to-create-a-phone-number-formatter-99b0347fc4f6


/**
 * Slight modification on the above concept. The user selects a country code from a separate dropdown,
 * which is given as the countryPrefix.
 *
 * The AsYouTypeFormatter is then given the country code prefix so that the following numbers are
 * formatted according to the rules for the given country prefix, but the country code itself is not
 * included in the textfield value.
 *
 * The prefix is added in the reformat section to the formatter, and then removed in the
 * getFormattedNumber function.
 *
 * @param phoneNumberFormatter AsYouTypeFormatter (using IAsYouTypeFormatter interface)
 * @param countryPrefix the country prefix to use with the formatter
 * @param onGetSelectionEnd the original version used Selection.getSelectionEnd(text), which really
 *        just cast it from a Span (which is not part of compose multiplatform). The parent component
 *        should pass this using textFieldValue e.g.
 *        onGetSelectionEnd = { textFieldValue.selection.end }
 */
class PhoneNumberTransformation(
    private val phoneNumberFormatter: IAsYouTypeFormatter,
    private val countryPrefix: String,
    private val onGetSelectionEnd: (CharSequence) -> Int
) : VisualTransformation {

    fun preFilter(text: String): String = text.filter { isReallyDialable(it) }

    fun preFilter(textValue: TextFieldValue): String = preFilter(textValue.text)


    override fun filter(text: AnnotatedString): TransformedText {

        val transformation = reformat(text, onGetSelectionEnd(text))

        return TransformedText(
            AnnotatedString(transformation.formatted.orEmpty()),
            object : OffsetMapping {

                /**
                 * Modified vs the original: this was returning -1 when clicking around and selecting
                 * different ranges
                 */
                @Suppress("TooGenericExceptionCaught", "SwallowedException")
                override fun originalToTransformed(offset: Int): Int {
                    return max(
                        a = try {
                            transformation.originalToTransformed[offset]
                        } catch (ex: IndexOutOfBoundsException) {
                            transformation.transformedToOriginal.lastIndex
                        },
                        b = 0
                    )
                }

                /**
                 * Modified vs the original: this was returning -1 when clicking around and selecting
                 * different ranges
                 */
                override fun transformedToOriginal(offset: Int): Int =
                    max(transformation.transformedToOriginal[offset], 0)
            },
        )
    }

    @Suppress("AvoidMutableCollections", "AvoidVarsExceptWithDelegate")
    private fun reformat(s: CharSequence, cursor: Int): Transformation {
        phoneNumberFormatter.clear()
        countryPrefix.forEach { phoneNumberFormatter.inputDigitAndRememberPosition(it) }

        val curIndex = cursor - 1
        var formatted: String? = null
        var lastNonSeparator = 0.toChar()
        var hasCursor = false

        s.forEachIndexed { index, char ->
            if (isNonSeparator(char)) {
                if (lastNonSeparator.code != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor)
                    hasCursor = false
                }
                lastNonSeparator = char
            }
            if (index == curIndex) {
                hasCursor = true
            }
        }

        if (lastNonSeparator.code != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor)
        }
        val originalToTransformed = mutableListOf<Int>()
        val transformedToOriginal = mutableListOf<Int>()
        var specialCharsCount = 0
        formatted?.forEachIndexed { index, char ->
            if (!isNonSeparator(char)) {
                specialCharsCount++
            } else {
                originalToTransformed.add(index)
            }
            transformedToOriginal.add(index - specialCharsCount)
        }
        originalToTransformed.add(originalToTransformed.maxOrNull()?.plus(1) ?: 0)
        transformedToOriginal.add(transformedToOriginal.maxOrNull()?.plus(1) ?: 0)

        return Transformation(formatted, originalToTransformed, transformedToOriginal)
    }

    /**
     * This has been modified from the original concept. The countryprefix is removed.
     */
    private fun getFormattedNumber(lastNonSeparator: Char, hasCursor: Boolean): String? {
        return if (hasCursor) {
            phoneNumberFormatter.inputDigitAndRememberPosition(lastNonSeparator)
                .removePrefix(countryPrefix)
        } else {
            phoneNumberFormatter.inputDigit(lastNonSeparator).removePrefix(countryPrefix)
        }
    }

    private data class Transformation(
        val formatted: String?,
        val originalToTransformed: List<Int>,
        val transformedToOriginal: List<Int>,
    )
}