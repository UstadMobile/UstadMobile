package com.ustadmobile.mui.theme

external interface FontStyle {
    var fontFamily: String?
    var fontSize: Int
    var fontWeightLight: Int
    var fontWeightRegular: Int
    var fontWeightMedium: Int
    var fontWeightBold: Int
    var htmlFontSize: Int?
}

external interface TypographyStyle {
    var color: String?
    var fontFamily: String?
    var fontSize: Int
    var fontWeight: Int
    var letterSpacing: String?
    var lineHeight: String?
    var textTransform: String?
    var useNextVariants: Boolean?
}

external interface Typography : FontStyle, TypographyStyle {
    var h1: TypographyStyle
    var h2: TypographyStyle
    var h3: TypographyStyle
    var h4: TypographyStyle
    var h5: TypographyStyle
    var h6: TypographyStyle
    var subtitle1: TypographyStyle
    var subtitle2: TypographyStyle
    var body1: TypographyStyle
    var body2: TypographyStyle
    var caption: TypographyStyle
    var button: TypographyStyle
    var overline: TypographyStyle
}

external interface TypographyOptions : Typography
